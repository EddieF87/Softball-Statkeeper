package xyz.sleekstats.softball.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
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
import xyz.sleekstats.softball.dialogs.AccessGuideDialog;
import xyz.sleekstats.softball.dialogs.CancelLoadDialog;
import xyz.sleekstats.softball.dialogs.RetryUserLoadDialog;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.StatKeepUser;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.sleekstats.softball.data.FirestoreUpdateService.LEAGUE_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreUpdateService.USERS;

public class UsersActivity extends AppCompatActivity
        implements CancelLoadDialog.OnListFragmentInteractionListener,
        RetryUserLoadDialog.OnFragmentInteractionListener,
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

    private Button startAdderBtn;
    private Button saveBtn;
    private Button resetBtn;
    private ProgressBar mProgressBar;

    private String mSelectionID;
    private String mSelectionName;
    private int mSelectionType;
    private int mLevel;

    private CancelLoadDialog mCancelLoadDialog;
    private boolean loadingUri;
    private RetryUserLoadDialog mRetryDialog;

    private final UsersActivity.MyHandler mHandler = new UsersActivity.MyHandler(this);

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
            goToMain();
        }
        if(mSelectionID == null) {
            goToMain();
            return;
        }

        firestore = FirebaseFirestore.getInstance();

        if (savedInstanceState != null) {
            levelChanges = (HashMap<String, Integer>) savedInstanceState.getSerializable(SAVED_MAP);
            mOriginalLevelsMap = (HashMap<String, Integer>) savedInstanceState.getSerializable(SAVED_USER_LEVELS);
            mCreator = savedInstanceState.getParcelable(SAVED_CREATOR);
            setCreator(mCreator);
            return;
        }

        TextView accessGuide = findViewById(R.id.set_access_levels);
        if(mLevel > LEVEL_VIEW_WRITE) {
            accessGuide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    DialogFragment newFragment = new AccessGuideDialog();
                    newFragment.show(fragmentTransaction, "");
                }
            });
        } else {
            accessGuide.setVisibility(View.GONE);
        }
        startQuery();
    }

    private void goToMain() {
        Intent intent = new Intent(UsersActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startQuery(){
        final String myEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Query userCollection;
        if (mLevel > LEVEL_VIEW_WRITE) {
            userCollection = firestore.collection(LEAGUE_COLLECTION).document(mSelectionID).collection(USERS);
        } else {
            Button emailButton = findViewById(R.id.btn_email);
            emailButton.setText(R.string.email_head_admin);
            userCollection = firestore.collection(LEAGUE_COLLECTION).document(mSelectionID).collection(USERS).whereGreaterThan(StatsContract.StatsEntry.LEVEL, LEVEL_ADMIN);
        }
        mHandler.postDelayed(retryLoadRunnable, 20000);
        userCollection.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mHandler.removeCallbacks(retryLoadRunnable);

                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            if(mRetryDialog != null) {
                                mRetryDialog.dismissIfShowing();
                            }
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mOriginalLevelsMap = new HashMap<>();
                            mUserList = new ArrayList<>();

                            boolean myEmailSet = false;
                            for (DocumentSnapshot document : task.getResult()) {

                                StatKeepUser statKeepUser = document.toObject(StatKeepUser.class);
                                statKeepUser.setId(document.getId());
                                int level = statKeepUser.getLevel();

                                if (level == LEVEL_CREATOR) {
                                    mCreator = statKeepUser;
                                    setCreator(statKeepUser);
                                } else if (level > 0 && level < LEVEL_CREATOR) {
                                    if (myEmail.equals(statKeepUser.getEmail())) {
                                        myEmailSet = true;
                                    } else {
                                        mOriginalLevelsMap.put(statKeepUser.getEmail(), statKeepUser.getLevel());
                                        mUserList.add(statKeepUser);
                                    }
                                }
                            }
                            if(mCreator == null && mUserList.isEmpty() && !myEmailSet) {
                                openRetryDialog();
                                return;
                            }
                            if(mLevel < LEVEL_ADMIN){
                                myEmailSet = true;
                            }
                            if(myEmailSet) {
                                String levelString;
                                switch (mLevel) {
                                    case 1:
                                        levelString = getString(R.string.view_only);
                                        break;
                                    case 2:
                                        levelString = getString(R.string.view_manage);
                                        break;
                                    case 3:
                                        levelString = getString(R.string.admin);
                                        break;
                                    default:
                                        levelString = getString(R.string.error);
                                }
                                TextView myView = findViewById(R.id.my_access_level);
                                String myAccess = myEmail + ":  " + levelString;
                                myView.setText(myAccess);
                                myView.setVisibility(View.VISIBLE);
                            }
                            Collections.sort(mUserList, StatKeepUser.levelComparator());
                            updateRV();
                            setButtons();
                        } else {
                            openRetryDialog();
                        }
                    }
                });
    }

    private void updateRV() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        UserListAdapter mAdapter = new UserListAdapter(mUserList, this, mLevel);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setCreator(StatKeepUser statKeepUser) {
        String creatorEmail = statKeepUser.getEmail();

        TextView nameView = findViewById(R.id.admin_name_view);
        TextView emailView = findViewById(R.id.admin_email_view);
        nameView.setText(statKeepUser.getName());
        emailView.setText(creatorEmail);
    }

    private void setButtons() {
        if (mLevel >= LEVEL_ADMIN) {
            startAdderBtn = findViewById(R.id.btn_start_adder);
            startAdderBtn.setVisibility(View.VISIBLE);
            startAdderBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startAdderBtn.setVisibility(View.INVISIBLE);
                    createMsgInvite();
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
                mProgressBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
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
        users.add(mCreator.getEmail());

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

    private void createMsgInvite() {
        final String selectionType;
        if (mSelectionType == MainPageSelection.TYPE_LEAGUE) {
            selectionType = "League";
        } else {
            selectionType = "Team";
        }
        String linkString = "http://sleekstats.xyz/" + selectionType.toLowerCase()
                + "?key=" + mSelectionID + "-" + mSelectionName;

//        String packageName = getApplicationContext().getPackageName();

        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(linkString))
                .setDynamicLinkDomain("v4mcm.app.goo.gl")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
//                        .setFallbackUrl(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName))
                        .build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();

        FirebaseDynamicLinks.getInstance().createDynamicLink()
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
                            sendMsgInvite(shortLink);
                        } else {
                            Toast.makeText(UsersActivity.this, "Error creating link. " +
                                    "\n Please try again.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        mProgressBar.setVisibility(View.VISIBLE);
        loadingUri = true;
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

    private void sendMsgInvite(Uri shortLink) {
        Intent msgIntent = new Intent(Intent.ACTION_SEND);
        msgIntent.setType("text/plain");
        msgIntent.putExtra(Intent.EXTRA_TEXT, "You're invited to view the scores, stats, & standings for "
                + mSelectionName + "!\n\nJoin here: " + shortLink);

        startActivity(Intent.createChooser(msgIntent, "Send View-Link to friends!"));
    }

    @Override
    public void onCancelLoad() {
        loadingUri = false;
        onBackPressed();
    }

    @Override
    public void onRetryChoice(boolean choice) {
        if(choice) {
            startQuery();
        } else {
            finish();
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<UsersActivity> mActivity;
        MyHandler(UsersActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            UsersActivity activity = mActivity.get();
            if (activity != null) {
                super.handleMessage(msg);
            }
        }
    }

    private final Runnable retryLoadRunnable = new Runnable() {
        public void run() {
            openRetryDialog();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(retryLoadRunnable);
        mHandler.removeCallbacksAndMessages(null);
    }

    private void openRetryDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mRetryDialog = new RetryUserLoadDialog();
        mRetryDialog.setCancelable(false);
        fragmentManager.beginTransaction().add(mRetryDialog, null).commitAllowingStateLoss();
    }
}
