package com.example.android.softballstatkeeper.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.softballstatkeeper.MyApp;
import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.data.FireTaskLoader;
import com.example.android.softballstatkeeper.adapters.MainPageAdapter;
import com.example.android.softballstatkeeper.data.StatsContract;
import com.example.android.softballstatkeeper.data.StatsContract.StatsEntry;
import com.example.android.softballstatkeeper.dialogs.DeleteSelectionDialog;
import com.example.android.softballstatkeeper.dialogs.EditNameDialog;
import com.example.android.softballstatkeeper.dialogs.EnterCodeDialog;
import com.example.android.softballstatkeeper.dialogs.InviteListDialog;
import com.example.android.softballstatkeeper.dialogs.JoinOrCreateDialog;
import com.example.android.softballstatkeeper.dialogs.LoadErrorDialog;
import com.example.android.softballstatkeeper.dialogs.SelectionInfoDialog;
import com.example.android.softballstatkeeper.models.MainPageSelection;
import com.example.android.softballstatkeeper.models.StatKeepUser;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.android.softballstatkeeper.data.FirestoreHelper.DELETION_COLLECTION;
import static com.example.android.softballstatkeeper.data.FirestoreHelper.LEAGUE_COLLECTION;
import static com.example.android.softballstatkeeper.data.FirestoreHelper.PLAYERS_COLLECTION;
import static com.example.android.softballstatkeeper.data.FirestoreHelper.PLAYER_LOGS;
import static com.example.android.softballstatkeeper.data.FirestoreHelper.REQUESTS;
import static com.example.android.softballstatkeeper.data.FirestoreHelper.TEAMS_COLLECTION;
import static com.example.android.softballstatkeeper.data.FirestoreHelper.TEAM_LOGS;
import static com.example.android.softballstatkeeper.data.FirestoreHelper.USERS;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<QuerySnapshot>,
        View.OnClickListener,
        InviteListDialog.OnFragmentInteractionListener,
        SelectionInfoDialog.OnFragmentInteractionListener,
        DeleteSelectionDialog.OnFragmentInteractionListener,
        LoadErrorDialog.OnFragmentInteractionListener,
        JoinOrCreateDialog.OnFragmentInteractionListener,
        EditNameDialog.OnFragmentInteractionListener,
        EnterCodeDialog.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private static final String AUTH = "FirebaseAuth";
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 0;
    private ArrayList<MainPageSelection> mSelectionList;
    private List<MainPageSelection> mInviteList;
    private String userID;
    private boolean visible;
    private RecyclerView mRecyclerView;
    private MainPageAdapter mainPageAdapter;
    private static final int MAIN_LOADER = 22;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSelectionList = getIntent().getParcelableArrayListExtra("mSelectionList");

        MobileAds.initialize(this, "ca-app-pub-5443559095909539~1574171209");

        View playerV = findViewById(R.id.player_sk_card);
        View teamV = findViewById(R.id.team_sk_card);
        View leagueV = findViewById(R.id.lg_sk_card);
        playerV.setOnClickListener(this);
        teamV.setOnClickListener(this);
        leagueV.setOnClickListener(this);
        TextView joinOrCreate = findViewById(R.id.textview_join_or_create);
        joinOrCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleCreateStatKeeperViewsVisibility();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        authenticateUser();
    }

    protected void authenticateUser() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Log.v(AUTH, "signed in already");
            loadSelections();
            invalidateOptionsMenu();
        } else {
            Log.d(AUTH, "else");
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
//                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                    .setAvailableProviders(
                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                            )).build(), RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String email = currentUser.getEmail();
                    String id = currentUser.getUid();

                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put(StatsEntry.EMAIL, email);

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    firestore.collection(USERS).document(id).set(userInfo);
                    loadSelections();
                }
            }
        }
        invalidateOptionsMenu();
    }

    private void loadSelections() {
        Log.d(AUTH, "loadSelections");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userID = currentUser.getUid();
        getSupportLoaderManager().initLoader(MAIN_LOADER, null, this);
    }

    private void setViews() {
        Log.d("aaa", "setViews() MainActivity");
        ProgressBar progressBar = findViewById(R.id.progressBarMain);
        TextView rvErrorView = findViewById(R.id.error_rv_main);
        if (mSelectionList.isEmpty()) {
            rvErrorView.setText(R.string.create_statkeeper);
            progressBar.setVisibility(View.GONE);
            rvErrorView.setVisibility(View.VISIBLE);
            if (!visible) {
                shuffleCreateStatKeeperViewsVisibility();
            }
        } else {
            Collections.sort(mSelectionList, MainPageSelection.nameComparator());
            Collections.sort(mSelectionList, MainPageSelection.typeComparator());
            mainPageAdapter = new MainPageAdapter(mSelectionList, MainActivity.this);
            mRecyclerView = findViewById(R.id.rv_main);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
            mRecyclerView.setAdapter(mainPageAdapter);
            progressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        try {
            if (!mInviteList.isEmpty()) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DialogFragment newFragment = InviteListDialog.newInstance(mInviteList);
                newFragment.show(fragmentTransaction, "");
            }
        } catch (Exception ignored) {
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
                mSelectionList = null;
                authenticateUser();
                break;
            case R.id.action_sign_out:
                if (mSelectionList != null) {
                    mSelectionList.clear();
                }
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
                Log.e(TAG, "error with onclick");
                return;
        }
        joinCreateDialog(type);
    }

    private void joinCreateDialog(int type) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = JoinOrCreateDialog.newInstance(type);
        newFragment.show(fragmentTransaction, "");
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
                Log.e(TAG, "error with onclick");
                return;
        }
        String title = String.format(titleString, selection);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = EditNameDialog.newInstance(title, type);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onInvitesSorted(List<MainPageSelection> list, SparseIntArray changes) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if (userID == null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            userID = currentUser.getUid();
        }

        for (int i = 0; i < changes.size(); i++) {
            int key = changes.keyAt(i);
            int level = changes.get(key);

            if (level < 0) {
                continue;
            }

            MainPageSelection mainPageSelection;
            mainPageSelection = list.get(key);
            mainPageSelection.setLevel(level);
            String selectionID = mainPageSelection.getId();
            String name = mainPageSelection.getName();
            int type = mainPageSelection.getType();

            DocumentReference docRef = firestore.collection(LEAGUE_COLLECTION).document(selectionID);

            Map<String, Object> updates = new HashMap<>();
            if (level == 0) {
                updates.put(userID, FieldValue.delete());
            } else if (level > 0) {
                updates.put(userID, level);
                insertSelectionToSQL(selectionID, name, type, level);
            }
            docRef.update(updates);
        }
    }

    private void insertSelectionToSQL(String selectionID, String name, int type, int level) {
        ContentValues selectionValues = new ContentValues();
        selectionValues.put(StatsEntry.COLUMN_FIRESTORE_ID, selectionID);
        selectionValues.put(StatsEntry.COLUMN_NAME, name);
        selectionValues.put(StatsEntry.TYPE, type);
        selectionValues.put(StatsEntry.LEVEL, level);
        getContentResolver().insert(StatsEntry.CONTENT_URI_SELECTIONS, selectionValues);
    }

    @Override
    public void onDelete(MainPageSelection selection, int pos) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = DeleteSelectionDialog.newInstance(selection, pos);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onDeleteConfirmed(MainPageSelection mainPageSelection, int pos) {
        mSelectionList.remove(mainPageSelection);
        mainPageAdapter.notifyDataSetChanged();
        final String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        final String selectionID = mainPageSelection.getId();
        String[] selectionArgs = new String[]{selectionID};
        getContentResolver().delete(StatsEntry.CONTENT_URI_SELECTIONS, selection, selectionArgs);
        if (userID == null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            userID = currentUser.getUid();
        }
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final DocumentReference leagueDoc = firestore.collection(LEAGUE_COLLECTION).document(selectionID);
        leagueDoc.collection(USERS).document(userID)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                leagueDoc.collection(USERS)
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (querySnapshot.size() == 0) {
                            leagueDoc.collection(PLAYERS_COLLECTION).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot querySnapshot) {
                                    for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                        DocumentReference documentReference = documentSnapshot.getReference();
                                        documentReference.collection(PLAYER_LOGS).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot querySnapshot) {
                                                for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                                    DocumentReference documentReference = documentSnapshot.getReference();
                                                    documentReference.delete();
                                                }
                                            }
                                        });
                                        documentReference.delete();
                                    }
                                }
                            });
                            leagueDoc.collection(TEAMS_COLLECTION).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot querySnapshot) {
                                    for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                        DocumentReference documentReference = documentSnapshot.getReference();
                                        documentReference.collection(TEAM_LOGS).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot querySnapshot) {
                                                for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                                    DocumentReference documentReference = documentSnapshot.getReference();
                                                    documentReference.delete();
                                                }
                                            }
                                        });
                                        documentReference.delete();
                                    }
                                }
                            });
                            leagueDoc.collection(DELETION_COLLECTION).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot querySnapshot) {
                                    for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                        DocumentReference documentReference = documentSnapshot.getReference();
                                        documentReference.delete();
                                    }
                                }
                            });
                            leagueDoc.delete();
                        }
                    }
                });
            }
        });
    }

    @Override
    public Loader<QuerySnapshot> onCreateLoader(int id, Bundle args) {
        ProgressBar progressBar = findViewById(R.id.progressBarMain);
        progressBar.setVisibility(View.VISIBLE);
        final FireTaskLoader fireTaskLoader = new FireTaskLoader(this, userID);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fireTaskLoader.cancelLoadInBackground();
            }
        }, 20000);
        return fireTaskLoader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<QuerySnapshot> loader, QuerySnapshot querySnapshot) {
        if (mSelectionList != null) {
            setViews();
            Log.d("xyxy", "  setViews");
            return;
        }
        Log.d("xyxy", "  mSelectionList == null");

        if (querySnapshot == null) {
            ProgressBar progressBar = findViewById(R.id.progressBarMain);
            progressBar.setVisibility(View.GONE);
            Log.d("xyxy", "  querySnapshot == null");
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    DialogFragment newFragment = LoadErrorDialog.newInstance();
                    newFragment.show(fragmentTransaction, "");
                }
            });
            return;
        }

        if (mInviteList == null) {
            mInviteList = new ArrayList<>();
        } else {
            mInviteList.clear();
        }

        mSelectionList = new ArrayList<>();
        Log.d("xyxy", "mSelectionList = new ArrayList<>();");

        for (DocumentSnapshot documentSnapshot : querySnapshot) {
            int level = documentSnapshot.getLong(userID).intValue();
            String selectionID = documentSnapshot.getId();
            String name = documentSnapshot.getString(StatsEntry.COLUMN_NAME);
            int type = documentSnapshot.getLong(StatsEntry.TYPE).intValue();
            MainPageSelection mainPageSelection = new MainPageSelection(
                    selectionID, name, type, level);
            if (level < -1) {
                mInviteList.add(mainPageSelection);
            } else if (level >= UsersActivity.LEVEL_VIEW_ONLY) {
                mSelectionList.add(mainPageSelection);
            }
        }
        setViews();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<QuerySnapshot> loader) {

    }

    @Override
    public void loadChoice(boolean load) {
        TextView rvErrorView = findViewById(R.id.error_rv_main);
        rvErrorView.setVisibility(View.GONE);
        if (load) {
            mInviteList = new ArrayList<>();
            mSelectionList = new ArrayList<>();
            Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_SELECTIONS,
                    null, null, null, null);
            while (cursor.moveToNext()) {
                String id = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
                String name = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME);
                int type = StatsContract.getColumnInt(cursor, StatsEntry.TYPE);
                int level = StatsContract.getColumnInt(cursor, StatsEntry.LEVEL);
                mSelectionList.add(new MainPageSelection(id, name, type, level));
            }
            setViews();
        } else {
            getSupportLoaderManager().restartLoader(MAIN_LOADER, null, this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("xyxy", "onNewIntent");
    }

    @Override
    protected void onStop() {
        super.onStop();
        getIntent().putParcelableArrayListExtra("mSelectionList", mSelectionList);
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
            mRecyclerView.setLayoutManager(null);
        }
        if (mainPageAdapter != null) {
            mainPageAdapter = null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mSelectionList = getIntent().getParcelableArrayListExtra("mSelectionList");
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

        addSelection(name, type, UsersActivity.LEVEL_CREATOR, null);
    }

    private void addSelection(String name, int type, int level, String statKeeperID) {
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
        String userEmail = currentUser.getEmail();
        String userDisplayName = currentUser.getDisplayName();

        Map<String, Object> firestoreLeagueMap = new HashMap<>();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference statKeeperDocument;

        if (statKeeperID == null) {
            statKeeperDocument = firestore.collection(LEAGUE_COLLECTION).document();
            firestoreLeagueMap.put(StatsEntry.COLUMN_NAME, name);
            firestoreLeagueMap.put(StatsEntry.TYPE, type);
        } else {
            statKeeperDocument = firestore.collection(LEAGUE_COLLECTION).document(statKeeperID);
        }

        firestoreLeagueMap.put(userID, level);
        statKeeperDocument.set(firestoreLeagueMap, SetOptions.merge());



        Map<String, Object> firestoreUserMap = new HashMap<>();
        firestoreUserMap.put(StatsEntry.LEVEL, level);
        firestoreUserMap.put(StatsEntry.EMAIL, userEmail);
        firestoreUserMap.put(StatsEntry.COLUMN_NAME, userDisplayName);
        statKeeperDocument.collection(USERS).document(userID).set(firestoreUserMap);

        MyApp myApp = (MyApp) getApplicationContext();
        String selectionID = statKeeperDocument.getId();
        MainPageSelection mainPageSelection = new MainPageSelection(selectionID, name, type, level);
        myApp.setCurrentSelection(mainPageSelection);
        insertSelectionToSQL(selectionID, name, type, level);
        mSelectionList.add(mainPageSelection);

        if (statKeeperID == null) {
            DocumentReference requestDocument = firestore.collection(LEAGUE_COLLECTION)
                    .document(selectionID).collection(REQUESTS).document();
            StatKeepUser statKeepUser = new StatKeepUser(REQUESTS, name, String.valueOf(type), UsersActivity.LEVEL_VIEW_ONLY - 100);
            requestDocument.set(statKeepUser, SetOptions.merge());
        }

        if (type == MainPageSelection.TYPE_TEAM) {
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_NAME, name);
            values.put(StatsEntry.ADD, true);
            getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
        } else if (type == MainPageSelection.TYPE_PLAYER) {
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_NAME, name);
            getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
        }
        startActivity(intent);
    }

    private void enterCodeDialog(int type) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = EnterCodeDialog.newInstance(type);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onJoinOrCreate(boolean create, int type) {
        if (create) {
            enterNameDialog(type);
        } else {
            enterCodeDialog(type);
        }
    }

    private void postMessage(int msg) {
        String text;
        switch (msg) {
            case 0:
                text = "SUCCESS";
                break;

            case 1:
                text = "Incorrect code entered.";
                break;

            case 2:
                text = "You have not filled in the details.";
                break;

            case 3:
                text = "You are attempting to join a Team with a League Code!";
                break;

            case 4:
                text = "You are attempting to join a League with a Team Code!";
                break;

            case 5:
                text = "You already have access to this StatKeeper!";
                break;

            default:
                text = getString(R.string.error);
                break;
        }
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onSubmitCode(final String fullText, final int type) {
        if (fullText.isEmpty()) {
            postMessage(2);
            return;
        }
        String[] splitCode = fullText.split("-");
        final String idText = splitCode[0];
        final String codeText = splitCode[1];

        for(MainPageSelection mainPageSelection : mSelectionList) {
            if(idText.equals(mainPageSelection.getId())){
                postMessage(5);
                return;
            }
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(LEAGUE_COLLECTION)
                .document(idText).collection(REQUESTS).document(codeText).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        try {
                            StatKeepUser statKeepUser = documentSnapshot.toObject(StatKeepUser.class);
                            String code = documentSnapshot.getId();
                            String id = statKeepUser.getId();
                            String name = statKeepUser.getName();
                            String requestType = statKeepUser.getEmail();
                            int level = statKeepUser.getLevel() + 100;

                            if (!id.equals(REQUESTS)) {
                                postMessage(99);
                                return;
                            }
                            if (!codeText.equals(code)) {
                                postMessage(1);
                                return;
                            }

                            if (!String.valueOf(type).equals(requestType)) {
                                if (type == MainPageSelection.TYPE_TEAM) {
                                    postMessage(3);
                                } else {
                                    postMessage(4);
                                }
                                return;
                            }

                            if (level >= UsersActivity.LEVEL_VIEW_ONLY && level <= UsersActivity.LEVEL_ADMIN) {
                                postMessage(0);
                                addSelection(name, type, level, idText);
                            }

                        } catch (Exception e) {
                            postMessage(99);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        postMessage(99);
                    }
                });

    }
}