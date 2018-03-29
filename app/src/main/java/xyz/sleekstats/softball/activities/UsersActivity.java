package xyz.sleekstats.softball.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.UserListAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.dialogs.CancelLoadDialog;
import xyz.sleekstats.softball.dialogs.EmailInviteDialog;
import xyz.sleekstats.softball.dialogs.InviteUserDialog;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.StatKeepUser;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.sleekstats.softball.data.FirestoreHelper.LEAGUE_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreHelper.REQUESTS;
import static xyz.sleekstats.softball.data.FirestoreHelper.USERS;

public class UsersActivity extends AppCompatActivity
        implements InviteUserDialog.OnFragmentInteractionListener,
        EmailInviteDialog.OnListFragmentInteractionListener,
CancelLoadDialog.OnListFragmentInteractionListener,
        UserListAdapter.AdapterListener {

    private static final String SAVED_MAP = "map";
    private static final String SAVED_USER_LEVELS = "userlevels";
    private static final String SAVED_CREATOR = "mCreator";

    public static final int LEVEL_REMOVE_USER = 0;
    public static final int LEVEL_VIEW_ONLY = 1;
    public static final int LEVEL_VIEW_WRITE = 2;
    public static final int LEVEL_ADMIN = 3;
    public static final int LEVEL_CREATOR = 4;

    private List<StatKeepUser> mUserList;
    private HashMap<String, Integer> mOriginalLevelsMap;
    private HashMap<String, Integer> levelChanges;
    private StatKeepUser mCreator;

    private RecyclerView mRecyclerView;
    private UserListAdapter mAdapter;

    private Button startAdderBtn;
    private Button saveBtn;
    private Button resetBtn;
    private ProgressBar mProgressBar;

    private String mSelectionID;
    private String mSelectionName;
    private int mSelectionType;
    private int mLevel;

    private CancelLoadDialog mCancelLoadDialog;
    boolean loadingUri;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        mRecyclerView = findViewById(R.id.rv_users);
        mProgressBar = findViewById(R.id.progress_users);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mSelectionID = mainPageSelection.getId();
            mSelectionName = mainPageSelection.getName();
            mSelectionType = mainPageSelection.getType();
            mLevel = mainPageSelection.getLevel();
            setTitle(mSelectionName);
            TextView leagueNameTextView = findViewById(R.id.league_name_display);
            String leagueNameDisplay = mSelectionName + " Users";
            leagueNameTextView.setText(leagueNameDisplay);
        } catch (Exception e) {
            Intent intent = new Intent(UsersActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        firestore = FirebaseFirestore.getInstance();

        if (savedInstanceState != null) {
            levelChanges = (HashMap<String, Integer>) savedInstanceState.getSerializable(SAVED_MAP);
            mOriginalLevelsMap = (HashMap<String, Integer>) savedInstanceState.getSerializable(SAVED_USER_LEVELS);
            mCreator = savedInstanceState.getParcelable(SAVED_CREATOR);
            setCreator(mCreator);
            return;
        }

        firestore.collection(LEAGUE_COLLECTION).document(mSelectionID).collection(USERS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mOriginalLevelsMap = new HashMap<>();
                            mUserList = new ArrayList<>();

                            for (DocumentSnapshot document : task.getResult()) {

                                StatKeepUser statKeepUser = document.toObject(StatKeepUser.class);
                                statKeepUser.setId(document.getId());
                                int level = statKeepUser.getLevel();

                                if (level == LEVEL_CREATOR) {
                                    mCreator = statKeepUser;
                                    setCreator(statKeepUser);
                                } else if (level > 0 && level < LEVEL_CREATOR) {
                                    mOriginalLevelsMap.put(statKeepUser.getEmail(), statKeepUser.getLevel());
                                    mUserList.add(statKeepUser);
                                }
                            }
                            Collections.sort(mUserList, StatKeepUser.levelComparator());
                            updateRV();
                            setButtons();
                        }
                    }
                });
    }

    private void updateRV() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new UserListAdapter(mUserList, this, mLevel);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setCreator(StatKeepUser statKeepUser) {
        String creatorEmail = statKeepUser.getEmail();

        TextView nameView = findViewById(R.id.admin_name_view);
        TextView emailView = findViewById(R.id.admin_email_view);
        nameView.setText(statKeepUser.getName());
        emailView.setText(creatorEmail);
    }

    private void openInviteUserDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = new InviteUserDialog();
        newFragment.show(fragmentTransaction, "");
    }

    private void openEmailInvitesDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = new EmailInviteDialog();
        newFragment.show(fragmentTransaction, "");
    }

    private void setButtons() {
        if (mLevel >= LEVEL_ADMIN) {
            startAdderBtn = findViewById(R.id.btn_start_adder);
            startAdderBtn.setVisibility(View.VISIBLE);
            startAdderBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startAdderBtn.setVisibility(View.INVISIBLE);
                    openInviteUserDialog();
                }
            });
            saveBtn = findViewById(R.id.btn_save_changes);
            resetBtn = findViewById(R.id.btn_reset);
            saveBtn.setVisibility(View.INVISIBLE);
            resetBtn.setVisibility(View.INVISIBLE);
        }
        Button emailButton = findViewById(R.id.btn_email);
        emailButton.setVisibility(View.VISIBLE);
    }

    public void saveChanges(View v) {
        if (levelChanges == null) {
            return;
        }

        mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        WriteBatch batch = firestore.batch();
        for (Map.Entry<String, Integer> entry : levelChanges.entrySet()) {
            String id = entry.getKey();
            int level = entry.getValue();
            DocumentReference league = firestore.collection(LEAGUE_COLLECTION).document(mSelectionID);
            DocumentReference leagueUser = firestore.collection(LEAGUE_COLLECTION).document(mSelectionID)
                    .collection(USERS).document(id);
            if (level == LEVEL_REMOVE_USER) {
                //todo FieldValue.delete()
                batch.update(league, id, 0);
                batch.delete(leagueUser);
            } else {
                batch.update(league, id, level);
                batch.update(leagueUser, StatsContract.StatsEntry.LEVEL, level);
            }
        }

        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Changes saved!", Toast.LENGTH_SHORT).show();
                onSaveSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Changes failed!", Toast.LENGTH_SHORT).show();
            }
        });
        resetChanges(null);
    }

    private void onSaveSuccess() {
        finish();
    }

    public void resetChanges(View v) {

        saveBtn.setVisibility(View.INVISIBLE);
        resetBtn.setVisibility(View.INVISIBLE);
        if (levelChanges == null) {
            return;
        }
        levelChanges.clear();
        revertUserList();
        updateRV();
    }

    private void revertUserList() {
        for (StatKeepUser user : mUserList) {
            String email = user.getEmail();

            int oldLevel = mOriginalLevelsMap.get(email);
            user.setLevel(oldLevel);
        }
    }

    public void sendEmailUpdate(View view) {
        int userSize = mUserList.size();
        List<String> users = new ArrayList<>();
        for (int i = 0; i < userSize; i++) {
            StatKeepUser statKeepUser = mUserList.get(i);
            String user = statKeepUser.getEmail();
            users.add(user);
        }

        String[] emailList = new String[userSize];
        emailList = users.toArray(emailList);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mSelectionName + " Update");
        emailIntent.putExtra(Intent.EXTRA_BCC, emailList);
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public void onUserLevelChanged(String name, int level) {
        if (levelChanges == null) {
            levelChanges = new HashMap<>();
        }
        levelChanges.put(name, level);
        saveBtn.setVisibility(View.VISIBLE);
        resetBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (levelChanges != null) {
            outState.putSerializable(SAVED_MAP, levelChanges);
        }
        outState.putSerializable(SAVED_USER_LEVELS, mOriginalLevelsMap);
        outState.putParcelable(SAVED_CREATOR, mCreator);
    }

    @Override
    public void onEmailInvites() {
        openEmailInvitesDialog();
    }

    @Override
    public void onInviteUsers() {

        firestore.collection(LEAGUE_COLLECTION)
                .document(mSelectionID).collection(REQUESTS).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                String codeText = documentSnapshot.getId();
                createMsgInvite(codeText);
            }
        });

    }

    private void createMsgInvite(final String codeText) {
        final String selectionType;
        if (mSelectionType == MainPageSelection.TYPE_LEAGUE) {
            selectionType = "League";
        } else {
            selectionType = "Team";
        }
        String linkString = "http://sleekstats.xyz/" + selectionType.toLowerCase()
                + "?key=" + mSelectionID + "-" + mSelectionName;


        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(linkString))
                .setDynamicLinkDomain("v4mcm.app.goo.gl")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setFallbackUrl(Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.apps.maps"))
                        .build())
                // Open links with com.example.ios on iOS
                .setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(dynamicLinkUri)
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (startAdderBtn != null) {
                            startAdderBtn.setVisibility(View.VISIBLE);
                        }
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(View.GONE);
                        }
                        loadingUri = false;
                        if (mCancelLoadDialog != null) {
                            mCancelLoadDialog.dismissIfShowing();
                        }
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            sendMsgInvite(shortLink, selectionType, codeText);
                        } else {
                            Toast.makeText(UsersActivity.this, "Error creating link. " +
                                    "\n Please try again.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        mProgressBar.setVisibility(View.VISIBLE);
        loadingUri = true;
//        Intent msgIntent = new Intent(Intent.ACTION_SEND);
//        msgIntent.setType("text/plain");
//        msgIntent.putExtra(Intent.EXTRA_TEXT, "You're invited to view the stats & standings for "
//                + mSelectionName + "!\n\nTo join follow these simple steps:" + dynamicLinkUri +
//                "\n\n    1. Log on to the StatKeeper app: " + "https://play.google.com/store/apps/details?id=xyz.sleekstats.softball" +
//                "\n\n    2. Click on \"Join " + selectionType + "\"" +
//                "\n\n    3. Enter the following code: " + mSelectionID + "-" + codeText);
//
//        startActivity(Intent.createChooser(msgIntent, "Message invite code to friends!"));
    }


    private void openCancelLoadDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mCancelLoadDialog = new CancelLoadDialog();
        mCancelLoadDialog.show(fragmentTransaction, "");
    }

    @Override
    public void onBackPressed() {
        if (loadingUri) {
            openCancelLoadDialog();
            return;
        }
        super.onBackPressed();
    }

    public void sendMsgInvite(Uri shortLink, String selectionType, String codeText) {
        Intent msgIntent = new Intent(Intent.ACTION_SEND);
        msgIntent.setType("text/plain");
        msgIntent.putExtra(Intent.EXTRA_TEXT, "You're invited to view the stats & standings for "
                + mSelectionName + "!\n\nTo join follow these simple steps:" + shortLink +
                "\n\n    1. Log on to the StatKeeper app: " + "https://play.google.com/store/apps/details?id=xyz.sleekstats.softball" +
                "\n\n    2. Click on \"Join " + selectionType + "\"" +
                "\n\n    3. Enter the following code: " + mSelectionID + "-" + codeText);

        startActivity(Intent.createChooser(msgIntent, "Send View-Link to friends!"));
    }


    @Override
    public void onCancel() {
        startAdderBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSubmitEmails(List<String> emails, List<Integer> levels) {
        int emailSize = emails.size();
        if (emailSize < 1) {
            return;
        }

        for (int i = 0; i < emailSize; i++) {
            String emailText = emails.get(i);
            if (emailText == null || emailText.isEmpty()) {
                continue;
            }
            final String email = emailText.toLowerCase();
            final int level = levels.get(i) + 1;
            final DocumentReference statKeeperRef = firestore.collection(LEAGUE_COLLECTION).document(mSelectionID);
            final CollectionReference usersCollection = firestore.collection(USERS);
            usersCollection.
                    whereEqualTo(StatsContract.StatsEntry.EMAIL, email)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put(StatsContract.StatsEntry.LEVEL, -level);

                        WriteBatch writeBatch = firestore.batch();

                        if (!documentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = documentSnapshots.get(0);
                            String userID = documentSnapshot.getId();

                            StatKeepUser compareUser = new StatKeepUser(userID, null, null, 0);
                            if (mUserList.contains(compareUser) || mCreator.equals(compareUser)) {
                                return;
                            }

                            userData.put(StatsContract.StatsEntry.EMAIL, email);
                            userData.put(StatsContract.StatsEntry.COLUMN_NAME, null);
                            writeBatch.set(statKeeperRef.collection(USERS)
                                    .document(userID), userData, SetOptions.merge());

                            Map<String, Integer> statKeeperData = new HashMap<>();
                            statKeeperData.put(userID, -level);
                            writeBatch.set(firestore.collection(LEAGUE_COLLECTION).document(mSelectionID),
                                    statKeeperData, SetOptions.merge());

                        } else {
                            writeBatch.set(usersCollection.document(email).collection(REQUESTS).document(mSelectionID), userData, SetOptions.merge());
                            writeBatch.set(statKeeperRef.collection(REQUESTS).document(email),
                                    userData, SetOptions.merge());
                        }
                        writeBatch.commit();
                    }
                }
            });
        }

        String[] emailList = new String[emailSize];
        emailList = emails.toArray(emailList);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null));
        emailIntent.putExtra(Intent.EXTRA_BCC, emailList);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "StatKeeper Invitation for " + mSelectionName + "!");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "You're invited to view, manage, and share stats and standings for " + mSelectionName + "." +
                "\n\nFollow this link to begin: https://play.google.com/store/apps/details?id=xyz.sleekstats.softball");
        //todo add link
        startActivity(Intent.createChooser(emailIntent, "Email friends about their invitation!"));

        startAdderBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCancelLoad() {
        loadingUri = false;
        onBackPressed();
    }
}
