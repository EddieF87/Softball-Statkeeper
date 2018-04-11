package xyz.sleekstats.softball.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.MainPageAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.dialogs.AcceptInviteDialog;
import xyz.sleekstats.softball.dialogs.DeleteSelectionDialog;
import xyz.sleekstats.softball.dialogs.EditNameDialog;
import xyz.sleekstats.softball.dialogs.ContinueLoadDialog;
import xyz.sleekstats.softball.dialogs.SelectionInfoDialog;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.StatKeepUser;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static xyz.sleekstats.softball.data.FirestoreUpdateService.BOXSCORE_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreUpdateService.DELETION_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreUpdateService.LEAGUE_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreUpdateService.PLAYERS_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreUpdateService.PLAYER_LOGS;
import static xyz.sleekstats.softball.data.FirestoreUpdateService.REQUESTS;
import static xyz.sleekstats.softball.data.FirestoreUpdateService.TEAMS_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreUpdateService.TEAM_LOGS;
import static xyz.sleekstats.softball.data.FirestoreUpdateService.USERS;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        SelectionInfoDialog.OnFragmentInteractionListener,
        DeleteSelectionDialog.OnFragmentInteractionListener,
        ContinueLoadDialog.OnFragmentInteractionListener,
        EditNameDialog.OnFragmentInteractionListener,
        AcceptInviteDialog.OnFragmentInteractionListener {

    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 0;
    private ArrayList<MainPageSelection> mSelectionList;
    private String userID;
    private boolean visible;
    private RecyclerView mRecyclerView;
    private TextView mMsgView;
    private ProgressBar mProgressBar;
    private MainPageAdapter mainPageAdapter;
    private FirebaseFirestore mFirestore;
    private ContinueLoadDialog mContinueLoadDialogFragment;
    private AcceptInviteDialog mAcceptInviteDialog;
    private final MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-5443559095909539~1574171209");

        mRecyclerView = findViewById(R.id.rv_main);
        mMsgView = findViewById(R.id.error_rv_main);
        mProgressBar = findViewById(R.id.progressBarMain);

        mAuth = FirebaseAuth.getInstance();

        View playerV = findViewById(R.id.player_sk_card);
        View teamV = findViewById(R.id.team_sk_card);
        View leagueV = findViewById(R.id.lg_sk_card);
        playerV.setOnClickListener(this);
        teamV.setOnClickListener(this);
        leagueV.setOnClickListener(this);
        TextView createSK = findViewById(R.id.textview_join_or_create);
        createSK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleCreateStatKeeperViewsVisibility();
            }
        });

        if(savedInstanceState != null) {
            mSelectionList = savedInstanceState.getParcelableArrayList("mSelectionList");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mSelectionList != null) {
            checkInvite();
            setViews();
            return;
        }
        authenticateUser();
    }

    protected void authenticateUser() {

        if (mAuth.getCurrentUser() != null) {

            setProgressBarVisible();
            startFirestoreLoad();
            invalidateOptionsMenu();
            checkInvite();

        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(true)
                    .setAvailableProviders(
                            Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()
                            )).build(), RC_SIGN_IN);
        }
    }

    private void checkInvite() {
        if (mAcceptInviteDialog != null) {
            return;
        }
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)

                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        } else {
                            return;
                        }

                        String path = deepLink.getPath();
                        String fullText = deepLink.getQueryParameter("key");
                        String[] splitCode = fullText.split("-");
                        final String id = splitCode[0];
                        final String name = splitCode[1];
                        final int type;

                        switch (path) {
                            case "/" + StatsEntry.COLUMN_TEAM:
                                type = MainPageSelection.TYPE_TEAM;
                                break;
                            case "/" + StatsEntry.COLUMN_LEAGUE:
                                type = MainPageSelection.TYPE_LEAGUE;
                                break;
                            default:
                                return;
                        }

                        final MyApp myApp = (MyApp) getApplicationContext();

                        if (mFirestore == null) {
                            mFirestore = FirebaseFirestore.getInstance();
                        }
                        mFirestore.collection(LEAGUE_COLLECTION).document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    Object levelObject = documentSnapshot.get(userID);
                                    if (levelObject == null) {
                                        openAcceptInviteDialog(id, name, type, 1);
                                        return;
                                    }
                                    int level = ((Long) levelObject).intValue();
                                    if (level < UsersActivity.LEVEL_REMOVE_USER && -level < UsersActivity.LEVEL_CREATOR) {
                                        level = -level;
                                        openAcceptInviteDialog(id, name, type, level);
                                        return;
                                    }
                                    myApp.setCurrentSelection(new MainPageSelection(id, name, type, level));
                                    final Intent intent;
                                    intent = new Intent(MainActivity.this, LoadingActivity.class);
                                    intent.putExtra(StatsEntry.ADD, true);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    openAcceptInviteDialog(id, name, type, 1);
                                }
                            }
                        });
                    }
                });
    }

    private void openAcceptInviteDialog(String id, String name, int type, int level) {

        if (mContinueLoadDialogFragment != null) {
            mContinueLoadDialogFragment.dismissIfShowing();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mAcceptInviteDialog = AcceptInviteDialog.newInstance(id, name, type, level);
        mAcceptInviteDialog.show(fragmentTransaction, "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {

                setProgressBarVisible();

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    final String email = currentUser.getEmail();
                    final String id = currentUser.getUid();

                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put(StatsEntry.EMAIL, email);

                    if (mFirestore == null) {
                        mFirestore = FirebaseFirestore.getInstance();
                    }

                    mFirestore.collection(USERS).document(id).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (email == null) {
                                return;
                            }
                            mFirestore.collection(USERS).document(email).collection(REQUESTS).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                QuerySnapshot querySnapshot = task.getResult();
                                                for (final DocumentSnapshot emailReqSnapshot : querySnapshot) {

                                                    final String statKeeper = emailReqSnapshot.getId();
                                                    final long level = emailReqSnapshot.getLong(StatsEntry.LEVEL);

                                                    final DocumentReference requestRef = mFirestore.collection(LEAGUE_COLLECTION).document(statKeeper).collection(REQUESTS).document(email);
                                                    requestRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {

                                                                DocumentReference statKeeperRef = mFirestore.collection(LEAGUE_COLLECTION).document(statKeeper);
                                                                DocumentReference userRef = statKeeperRef.collection(USERS).document(id);
                                                                DocumentReference emailRequestRef = emailReqSnapshot.getReference();

                                                                Map<String, Object> updateUser = new HashMap<>();
                                                                updateUser.put(StatsEntry.LEVEL, level);
                                                                updateUser.put(StatsEntry.EMAIL, email);

                                                                Map<String, Object> updateStatKeeper = new HashMap<>();
                                                                updateStatKeeper.put(id, level);


                                                                WriteBatch writeBatch = mFirestore.batch();
                                                                writeBatch.set(userRef, updateUser, SetOptions.merge());
                                                                writeBatch.set(statKeeperRef, updateStatKeeper, SetOptions.merge());
                                                                writeBatch.delete(requestRef);
                                                                writeBatch.delete(emailRequestRef);
                                                                writeBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        startFirestoreLoad();
                                                                        if (mAcceptInviteDialog == null) {
                                                                            checkInvite();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                            startFirestoreLoad();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        startFirestoreLoad();
                        if (mAcceptInviteDialog == null) {
                            checkInvite();
                        }
                    }
                });
            }
        } else {
            mMsgView.setText(R.string.sign_in_to_start_text);
            setMessageViewVisible();
        }
    }

    invalidateOptionsMenu();
}

    private void setProgressBarVisible() {
        mMsgView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setMessageViewVisible() {
        mProgressBar.setVisibility(View.GONE);
        mMsgView.setVisibility(View.VISIBLE);
    }

    private void setViews() {
        mProgressBar.setVisibility(View.GONE);
        if (mSelectionList.isEmpty()) {
            mMsgView.setText(R.string.create_statkeeper);

            if (loadFromCache()) {
                String text = "Unable to connect to SleekStats database.\nPlease check your connection and try again." +
                            "\nAlternatively, try loading statkeepers from your local database.";
                mMsgView.setText(text);
                final Button sqlButton = findViewById(R.id.btn_sql_load);
                final Button retryButton = findViewById(R.id.btn_retry_load);
                sqlButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sqlButton.setVisibility(View.GONE);
                        retryButton.setVisibility(View.GONE);
                        setProgressBarVisible();
//                           loadFromCache();
                        setViews();
                        }
                    });
                    retryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sqlButton.setVisibility(View.GONE);
                            retryButton.setVisibility(View.GONE);
                            setProgressBarVisible();
//                            mLoadStarted = false;
                            startFirestoreLoad();
                        }
                    });
                    sqlButton.setVisibility(View.VISIBLE);
                    retryButton.setVisibility(View.VISIBLE);
                } else {
                    mainPageAdapter = null;
                    if (!visible) {
                        shuffleCreateStatKeeperViewsVisibility();
                    }
                }
                setMessageViewVisible();
        } else {
            Collections.sort(mSelectionList, MainPageSelection.nameComparator());
            Collections.sort(mSelectionList, MainPageSelection.typeComparator());
            mainPageAdapter = new MainPageAdapter(mSelectionList, MainActivity.this);
            mRecyclerView = findViewById(R.id.rv_main);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
            mRecyclerView.setAdapter(mainPageAdapter);
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void shuffleCreateStatKeeperViewsVisibility() {
        TextView textView = findViewById(R.id.textview_join_or_create);
        View playerV = findViewById(R.id.player_sk_card);
        View teamV = findViewById(R.id.team_sk_card);
        View leagueV = findViewById(R.id.lg_sk_card);
        int visibilitySetting;
        if (visible) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reorder_white_500_24dp, 0, 0, 0);
            visibilitySetting = View.GONE;
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_clear_white_18dp, 0, 0, 0);
            visibilitySetting = View.VISIBLE;
        }
        playerV.setVisibility(visibilitySetting);
        teamV.setVisibility(visibilitySetting);
        leagueV.setVisibility(visibilitySetting);
        visible = !visible;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem signInItem = menu.findItem(R.id.action_sign_in);
        MenuItem signOutItem = menu.findItem(R.id.action_sign_out);
        if (mAuth.getCurrentUser() != null) {
            signOutItem.setVisible(true);
            signInItem.setVisible(false);
        } else {
            signOutItem.setVisible(false);
            signInItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_in:
                mMsgView.setVisibility(View.GONE);
                authenticateUser();
                break;
            case R.id.action_sign_out:
                mMsgView.setText(R.string.sign_in_to_start_text);
                mMsgView.setVisibility(View.VISIBLE);
                mSelectionList = null;
                if (mRecyclerView != null) {
                    mRecyclerView.setAdapter(null);
                }
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
//                                finish();
                            }
                        });
                break;
        }
        invalidateOptionsMenu();
        return true;
    }


    @Override
    public void onClick(View view) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(MainActivity.this, "Please sign in first!", Toast.LENGTH_LONG).show();
            return;
        }
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(MainActivity.this, "Please connect to a network first!", Toast.LENGTH_LONG).show();
            return;
        }
        int type;
        switch (view.getId()) {
            case R.id.player_sk_card:
                type = MainPageSelection.TYPE_PLAYER;
                break;

            case R.id.team_sk_card:
                type = MainPageSelection.TYPE_TEAM;
                break;

            case R.id.lg_sk_card:
                type = MainPageSelection.TYPE_LEAGUE;
                break;

            default:
                return;
        }
        enterNameDialog(type);
    }

    private void enterNameDialog(int type) {
        String titleString = "Enter %1$s name";
        String selection;
        switch (type) {
            case MainPageSelection.TYPE_PLAYER:
                selection = getString(R.string.player);
                break;

            case MainPageSelection.TYPE_TEAM:
                selection = getString(R.string.team);
                break;

            case MainPageSelection.TYPE_LEAGUE:
                selection = getString(R.string.league);
                break;

            default:
                return;
        }
        String title = String.format(titleString, selection);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = EditNameDialog.newInstance(title, type);
        newFragment.show(fragmentTransaction, "");
    }

    private void updateRV() {
        if (mSelectionList != null && !mSelectionList.isEmpty()) {
            mMsgView.setVisibility(View.GONE);
        }
        if (mainPageAdapter == null) {
            mainPageAdapter = new MainPageAdapter(mSelectionList, this);
            if (mRecyclerView == null) {
                mRecyclerView = findViewById(R.id.rv_main);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                mRecyclerView.setVisibility(View.VISIBLE);
                mMsgView.setVisibility(View.INVISIBLE);
            }
            mRecyclerView.setAdapter(mainPageAdapter);
        } else {
            mainPageAdapter.notifyDataSetChanged();
        }
    }

    private void insertSelectionToSQL(MainPageSelection mainPageSelection) {
        mSelectionList.add(mainPageSelection);
        ContentValues selectionValues = new ContentValues();
        selectionValues.put(StatsEntry.COLUMN_FIRESTORE_ID, userID);
        selectionValues.put(StatsEntry.COLUMN_LEAGUE_ID, mainPageSelection.getId());
        selectionValues.put(StatsEntry.COLUMN_NAME, mainPageSelection.getName());
        selectionValues.put(StatsEntry.TYPE, mainPageSelection.getType());
        selectionValues.put(StatsEntry.LEVEL, mainPageSelection.getLevel());
        getContentResolver().insert(StatsEntry.CONTENT_URI_SELECTIONS, selectionValues);
    }

    @Override
    public void onDelete(MainPageSelection selection) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = DeleteSelectionDialog.newInstance(selection);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onDeleteConfirmed(final MainPageSelection mainPageSelection) {
        if (userID == null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            userID = currentUser.getUid();
        }
        mSelectionList.remove(mainPageSelection);
        updateRV();
        final String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        final String selectionID = mainPageSelection.getId();
        final String[] selectionArgs = new String[]{selectionID};
        getContentResolver().delete(StatsEntry.CONTENT_URI_SELECTIONS, selection, selectionArgs);

        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        final DocumentReference leagueDoc = mFirestore.collection(LEAGUE_COLLECTION).document(selectionID);
        final WriteBatch batch = mFirestore.batch();

        batch.delete(leagueDoc.collection(USERS).document(userID));

        Map<String, Object> updates = new HashMap<>();
        updates.put(userID, FieldValue.delete());
        batch.update(leagueDoc, updates);

        leagueDoc.collection(USERS)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if (querySnapshot.size() <= 1) {
                    batch.delete(leagueDoc);
                    leagueDoc.collection(PLAYERS_COLLECTION).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                            DocumentReference documentReference = documentSnapshot.getReference();
                                            documentReference.collection(PLAYER_LOGS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    deleteCollection(batch, task);

                                                }
                                            });
                                            batch.delete(documentReference);
                                        }
                                    }
                                    leagueDoc.collection(TEAMS_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                QuerySnapshot querySnapshot = task.getResult();
                                                for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                                    DocumentReference documentReference = documentSnapshot.getReference();
                                                    documentReference.collection(TEAM_LOGS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            deleteCollection(batch, task);

                                                        }
                                                    });
                                                    batch.delete(documentReference);
                                                }
                                            }
                                            leagueDoc.collection(DELETION_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    deleteCollection(batch, task);

                                                    leagueDoc.collection(REQUESTS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            deleteCollection(batch, task);

                                                            leagueDoc.collection(BOXSCORE_COLLECTION).get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    deleteCollection(batch, task);
                                                                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            deleteStatKeeper(selection, selectionArgs);
                                                                            startFirestoreLoad();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                } else {
                    if (mainPageSelection.getLevel() == UsersActivity.LEVEL_CREATOR) {
                        List<StatKeepUser> userList = new ArrayList<>();
                        for (DocumentSnapshot userDoc : querySnapshot) {
                            StatKeepUser statKeepUser = userDoc.toObject(StatKeepUser.class);
                            statKeepUser.setId(userDoc.getId());
                            userList.add(statKeepUser);
                        }
                        StatKeepUser newCreator = Collections.max(userList, StatKeepUser.levelComparator());
                        String creatorID = newCreator.getId();

                        Map<String, Object> userCreatorUpdate = new HashMap<>();
                        userCreatorUpdate.put(StatsEntry.LEVEL, UsersActivity.LEVEL_CREATOR);
                        Map<String, Object> leagueCreatorUpdate = new HashMap<>();
                        leagueCreatorUpdate.put(creatorID, UsersActivity.LEVEL_CREATOR);

                        DocumentReference userDoc = leagueDoc.collection(USERS).document(creatorID);
                        batch.update(userDoc, userCreatorUpdate);
                        batch.update(leagueDoc, leagueCreatorUpdate);
                    }
                    batch.commit().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to delete StatKeeper. Please contact sleekstats@gmail.com", Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            deleteStatKeeper(selection, selectionArgs);
                        }
                    });
                }
                SharedPreferences updatePreferences = getSharedPreferences(selectionID + "_updateSettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = updatePreferences.edit();
                editor.clear();
                editor.apply();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "FAILLLL 1111111111", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteStatKeeper(String selection, String[] selectionArgs){
        getContentResolver().delete(StatsEntry.CONTENT_URI_BOXSCORE_OVERVIEWS, selection, selectionArgs);
        getContentResolver().delete(StatsEntry.CONTENT_URI_BOXSCORE_PLAYERS, selection, selectionArgs);
        getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, selection, selectionArgs);
        getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_TEAMS, selection, selectionArgs);
        getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, selection, selectionArgs);
        getContentResolver().delete(StatsEntry.CONTENT_URI_PLAYERS, selection, selectionArgs);
        getContentResolver().delete(StatsEntry.CONTENT_URI_SELECTIONS, selection, selectionArgs);
        getContentResolver().delete(StatsEntry.CONTENT_URI_TEAMS, selection, selectionArgs);
        getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, selection, selectionArgs);
    }

    private void deleteCollection(WriteBatch batch, Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            QuerySnapshot querySnapshot = task.getResult();
            for (final DocumentSnapshot documentSnapshot : querySnapshot) {
                DocumentReference documentReference = documentSnapshot.getReference();
                batch.delete(documentReference);
            }
        }
    }

    private void startFirestoreLoad () {
//        mLoadStarted = true;
        setProgressBarVisible();
        mHandler.postDelayed(continueLoadRunnable, 20000);

        if(mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        if(userID == null) {
            userID = mAuth.getCurrentUser().getUid();
        }
        mFirestore.collection(LEAGUE_COLLECTION)
                .whereLessThan(userID, 99)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
//                if(!mLoadStarted){
//                    return;
//                }
//                mLoadStarted = false;
                loadData(querySnapshot);
            }
        });
    }

    private void openContinueLoadDialog() {
        if (mAcceptInviteDialog != null) {
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        mContinueLoadDialogFragment = new ContinueLoadDialog();
        fragmentManager.beginTransaction().add(mContinueLoadDialogFragment, null).commitAllowingStateLoss();
    }


    private void loadData(QuerySnapshot querySnapshot){

        if (querySnapshot == null) {
            mMsgView.setText(R.string.error_with_loading);
            setMessageViewVisible();
            startFirestoreLoad();
            return;
        }

        if(mSelectionList != null) {
            return;
        }

        if (mContinueLoadDialogFragment != null) {
            mHandler.post(dismissContinueLoadRunnable);
        }
        mHandler.removeCallbacks(continueLoadRunnable);



        mSelectionList = new ArrayList<>();

        for (DocumentSnapshot documentSnapshot : querySnapshot) {
            int level = documentSnapshot.getLong(userID).intValue();
            String selectionID = documentSnapshot.getId();
            String name = documentSnapshot.getString(StatsEntry.COLUMN_NAME);
            int type;
            if(name == null || documentSnapshot.getLong(StatsEntry.TYPE) == null) {
                continue;
            } else {
                type = documentSnapshot.getLong(StatsEntry.TYPE).intValue();
            }
            MainPageSelection mainPageSelection = new MainPageSelection(
                    selectionID, name, type, level);
            if (level >= UsersActivity.LEVEL_VIEW_ONLY) {
                mSelectionList.add(mainPageSelection);
            }
        }
        setViews();
    }


    @Override
    public void loadChoice(boolean load) {
        mMsgView.setVisibility(View.GONE);
        if (load) {
            if (loadFromCache()) {
                setViews();
            } else {
                String errorText = "There are no statkeepers in your local database." +
                        "\nPlease retry loading from the central database." +
                        "\nIf you think there's an error, please contact me at sleekstats@gmail.com.";
                mMsgView.setText(errorText);
                setMessageViewVisible();
            }
        } else {
            mHandler.postDelayed(continueLoadRunnable, 20000);
        }
    }

    private boolean loadFromCache() {
        mSelectionList = new ArrayList<>();
        if (userID == null) {
            userID = mAuth.getCurrentUser().getUid();
        }
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{userID};

        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_SELECTIONS,
                null, selection, selectionArgs, null);
        while (cursor.moveToNext()) {
            String id = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_LEAGUE_ID);
            String name = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME);
            int type = StatsContract.getColumnInt(cursor, StatsEntry.TYPE);
            int level = StatsContract.getColumnInt(cursor, StatsEntry.LEVEL);
            mSelectionList.add(new MainPageSelection(id, name, type, level));
        }
        cursor.close();
        return !mSelectionList.isEmpty();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
            mRecyclerView.setLayoutManager(null);
        }
        if (mainPageAdapter != null) {
            mainPageAdapter = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mSelectionList != null) {
            outState.putParcelableArrayList("mSelectionList", mSelectionList);
        }
    }

    @Override
    public void onEdit(String name, int type) {
        if (type == -1) {
            return;
        }
        if (name.isEmpty()) {
            Toast.makeText(MainActivity.this, R.string.please_enter_name_first, Toast.LENGTH_LONG).show();
            return;
        }
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
        setProgressBarVisible();

        addSelection(name, type, UsersActivity.LEVEL_CREATOR, null);
    }

    private void addSelection(final String name, final int type, final int level, final String statKeeperID) {
        final Intent intent;
        if (statKeeperID == null) {
            switch (type) {
                case MainPageSelection.TYPE_PLAYER:
                    intent = new Intent(MainActivity.this, PlayerManagerActivity.class);
                    break;
                case MainPageSelection.TYPE_TEAM:
                    intent = new Intent(MainActivity.this, TeamManagerActivity.class);
                    break;
                case MainPageSelection.TYPE_LEAGUE:
                    intent = new Intent(MainActivity.this, LeagueManagerActivity.class);
                    break;
                default:
                    return;
            }
        } else {
            intent = new Intent(MainActivity.this, LoadingActivity.class);
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        final String userEmail = currentUser.getEmail();
        final String userDisplayName = currentUser.getDisplayName();

        final Map<String, Object> firestoreLeagueMap = new HashMap<>();

        firestoreLeagueMap.put(userID, level);

        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        final DocumentReference statKeeperDocument;

        if (statKeeperID == null) {
            statKeeperDocument = mFirestore.collection(LEAGUE_COLLECTION).document();
            firestoreLeagueMap.put(StatsEntry.COLUMN_NAME, name);
            firestoreLeagueMap.put(StatsEntry.TYPE, type);
            firestoreLeagueMap.put("creator", null);

            statKeeperDocument.set(firestoreLeagueMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    onSuccessfulStatKeeperUpdate(intent, statKeeperDocument, level, userEmail, userDisplayName,
                            statKeeperID, name, type);
                }
            });
        } else {
            statKeeperDocument = mFirestore.collection(LEAGUE_COLLECTION).document(statKeeperID);
            statKeeperDocument.update(firestoreLeagueMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    onSuccessfulStatKeeperUpdate(intent, statKeeperDocument, level, userEmail, userDisplayName,
                            statKeeperID, name, type);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "This league no longer exists! :(", Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    private void onSuccessfulStatKeeperUpdate(final Intent intent, final DocumentReference statKeeperDocument,
                                              final int level, String userEmail, String userDisplayName,
                                              final String statKeeperID, final String name, final int type) {
        Map<String, Object> firestoreUserMap = new HashMap<>();
        firestoreUserMap.put(StatsEntry.LEVEL, level);
        firestoreUserMap.put(StatsEntry.EMAIL, userEmail);
        firestoreUserMap.put(StatsEntry.COLUMN_NAME, userDisplayName);

        statKeeperDocument.collection(USERS).document(userID).set(firestoreUserMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        MyApp myApp = (MyApp) getApplicationContext();
                        String selectionID = statKeeperDocument.getId();
                        MainPageSelection mainPageSelection = new MainPageSelection(selectionID, name, type, level);
                        myApp.setCurrentSelection(mainPageSelection);
                        insertSelectionToSQL(mainPageSelection);

                        if (statKeeperID == null) {
                            Map<String, Object> creator = new HashMap<>();
                            creator.put("creator", userID);
                            statKeeperDocument.update(creator);

                            if (type == MainPageSelection.TYPE_TEAM) {
                                ContentValues values = new ContentValues();
                                values.put(StatsEntry.COLUMN_NAME, name);
                                values.put(StatsEntry.ADD, true);
                                values.put(StatsEntry.COLUMN_LEAGUE_ID, selectionID);
                                values.put(StatsEntry.TYPE, type);
                                values.put(StatsEntry.COLUMN_LEAGUE, name);
                                getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
                            } else if (type == MainPageSelection.TYPE_PLAYER) {
                                ContentValues values = new ContentValues();
                                values.put(StatsEntry.COLUMN_NAME, name);
                                values.put(StatsEntry.COLUMN_LEAGUE_ID, selectionID);
                                getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
                            }

                            DocumentReference requestDocument = mFirestore.collection(LEAGUE_COLLECTION)
                                    .document(selectionID).collection(REQUESTS).document();
                            StatKeepUser statKeepUser = new StatKeepUser(REQUESTS, name, String.valueOf(type), UsersActivity.LEVEL_VIEW_ONLY - 100);
                            requestDocument.set(statKeepUser, SetOptions.merge());
                        } else {
                            intent.putExtra(StatsEntry.ADD, true);
                        }
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Firebase error! Try again!", Toast.LENGTH_SHORT).show();
            }
        });
    }




    @Override
    public void onAcceptInvite(boolean accepted, String id, String name, int type, int level) {
        if (accepted) {
            MyApp myApp = (MyApp) getApplicationContext();
            myApp.setCurrentSelection(new MainPageSelection(id, name, type, level));
            addSelection(name, type, level, id);
        }
        mAcceptInviteDialog = null;
    }

private static class MyHandler extends Handler {
    private final WeakReference<MainActivity> mActivity;

    MyHandler(MainActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        MainActivity activity = mActivity.get();
        if (activity != null) {
            super.handleMessage(msg);
        }
    }

}

    private final Runnable continueLoadRunnable = new Runnable() {
        public void run() {
            openContinueLoadDialog();
        }
    };

    private final Runnable dismissContinueLoadRunnable = new Runnable() {
        public void run() {
            if (mContinueLoadDialogFragment != null) {
                mContinueLoadDialogFragment.dismissIfShowing();
                mContinueLoadDialogFragment = null;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(continueLoadRunnable);
        mHandler.removeCallbacks(dismissContinueLoadRunnable);
        mHandler.removeCallbacksAndMessages(null);
        if(mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
        }
    }
}