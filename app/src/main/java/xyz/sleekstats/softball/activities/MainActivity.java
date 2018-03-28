package xyz.sleekstats.softball.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.FireTaskLoader;
import xyz.sleekstats.softball.adapters.MainPageAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.dialogs.DeleteSelectionDialog;
import xyz.sleekstats.softball.dialogs.EditNameDialog;
import xyz.sleekstats.softball.dialogs.EnterCodeDialog;
import xyz.sleekstats.softball.dialogs.InviteListDialog;
import xyz.sleekstats.softball.dialogs.JoinOrCreateDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.sleekstats.softball.data.FirestoreHelper.DELETION_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreHelper.LEAGUE_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreHelper.PLAYERS_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreHelper.PLAYER_LOGS;
import static xyz.sleekstats.softball.data.FirestoreHelper.REQUESTS;
import static xyz.sleekstats.softball.data.FirestoreHelper.TEAMS_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreHelper.TEAM_LOGS;
import static xyz.sleekstats.softball.data.FirestoreHelper.USERS;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<QuerySnapshot>,
        View.OnClickListener,
        InviteListDialog.OnFragmentInteractionListener,
        SelectionInfoDialog.OnFragmentInteractionListener,
        DeleteSelectionDialog.OnFragmentInteractionListener,
        ContinueLoadDialog.OnFragmentInteractionListener,
        JoinOrCreateDialog.OnFragmentInteractionListener,
        EditNameDialog.OnFragmentInteractionListener,
        EnterCodeDialog.OnFragmentInteractionListener {

    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 0;
    private ArrayList<MainPageSelection> mSelectionList;
    private ArrayList<MainPageSelection> mInviteList;
    private String userID;
    private boolean visible;

    private RecyclerView mRecyclerView;
    private TextView mErrorView;
    private ProgressBar mProgressBar;

    private MainPageAdapter mainPageAdapter;
    private static final int MAIN_LOADER = 22;
    private FireTaskLoader mFireTaskLoader;
    private ContinueLoadDialog mContinueLoadDialogFragment;
    private InviteListDialog mInviteDialogFragment;
    private boolean loadingFinished;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-5443559095909539~1574171209");

        mSelectionList = getIntent().getParcelableArrayListExtra("mSelectionList");
        mInviteList = getIntent().getParcelableArrayListExtra("mInviteList");

        mRecyclerView = findViewById(R.id.rv_main);
        mErrorView = findViewById(R.id.error_rv_main);
        mProgressBar = findViewById(R.id.progressBarMain);

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
        mErrorView.setVisibility(View.GONE);
    }

    protected void authenticateUser() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            loadSelections();
            invalidateOptionsMenu();
        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(true)
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
                    final String email = currentUser.getEmail();
                    final String id = currentUser.getUid();

                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put(StatsEntry.EMAIL, email);

                    final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    firestore.collection(USERS).document(id).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if(email == null){return;}
                            firestore.collection(USERS).document(email).collection(REQUESTS).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        for (final DocumentSnapshot emailReqSnapshot : querySnapshot) {

                                            final String statKeeper = emailReqSnapshot.getId();
                                            final long level = emailReqSnapshot.getLong(StatsEntry.LEVEL);

                                            final DocumentReference requestRef = firestore.collection(LEAGUE_COLLECTION).document(statKeeper).collection(REQUESTS).document(email);
                                            requestRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()) {

                                                        DocumentReference statKeeperRef = firestore.collection(LEAGUE_COLLECTION).document(statKeeper);
                                                        DocumentReference userRef = statKeeperRef.collection(USERS).document(id);
                                                        DocumentReference emailRequestRef = emailReqSnapshot.getReference();

                                                        Map<String, Object> updateUser = new HashMap<>();
                                                        updateUser.put(StatsEntry.LEVEL, level);
                                                        updateUser.put(StatsEntry.EMAIL, email);

                                                        Map<String, Object> updateStatKeeper = new HashMap<>();
                                                        updateStatKeeper.put(id, level);


                                                        WriteBatch writeBatch = firestore.batch();
                                                        writeBatch.set(userRef, updateUser, SetOptions.merge());
                                                        writeBatch.set(statKeeperRef, updateStatKeeper, SetOptions.merge());
                                                        writeBatch.delete(requestRef);
                                                        writeBatch.delete(emailRequestRef);

                                                        writeBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                reloadSelections();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                            reloadSelections();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            reloadSelections();
                        }
                    });
                }
            }
        }
        invalidateOptionsMenu();
    }

    private void loadSelections() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userID = currentUser.getUid();
        getSupportLoaderManager().initLoader(MAIN_LOADER, null, this);
    }

    private void reloadSelections() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userID = currentUser.getUid();
        mSelectionList = null;
        mInviteList = null;
        getSupportLoaderManager().restartLoader(MAIN_LOADER, null, this);
    }

    private void setViews() {
        if (mSelectionList.isEmpty()) {
            mErrorView.setText(R.string.create_statkeeper);
            mProgressBar.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
            mainPageAdapter = null;
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
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        try {
            if (!mInviteList.isEmpty()) {
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        openInviteDialog();
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    private void openInviteDialog() {
        if(mInviteDialogFragment != null) {
            mInviteDialogFragment.isShowing();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        mInviteDialogFragment = InviteListDialog.newInstance(mInviteList);
        fragmentManager.beginTransaction().add(mInviteDialogFragment, null).commitAllowingStateLoss();
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
                mErrorView.setText(R.string.error_with_loading);
                mErrorView.setVisibility(View.GONE);
                mSelectionList = null;
                mInviteList = null;
                authenticateUser();
                break;
            case R.id.action_sign_out:
                mErrorView.setText(R.string.sign_in_to_start_text);
                mErrorView.setVisibility(View.VISIBLE);
                getSupportLoaderManager().destroyLoader(MAIN_LOADER);
                mSelectionList = null;
                mInviteList = null;
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

        final List<MainPageSelection> insertList = new ArrayList<>();
        WriteBatch writeBatch = firestore.batch();

        for (int i = 0; i < changes.size(); i++) {
            int key = changes.keyAt(i);
            final int level = changes.get(key);

            if (level < 0) {
                continue;
            }

            MainPageSelection mainPageSelection = list.get(key);
            mainPageSelection.setLevel(level);
            String selectionID = mainPageSelection.getId();
            insertList.add(mainPageSelection);

            DocumentReference leagueRef = firestore.collection(LEAGUE_COLLECTION).document(selectionID);
            DocumentReference userRef = firestore.collection(LEAGUE_COLLECTION).document(selectionID)
                    .collection(USERS).document(userID);

            Map<String, Object> leagueUpdate = new HashMap<>();
            if (level == UsersActivity.LEVEL_REMOVE_USER) {
                //todo FieldValue.delete()
                leagueUpdate.put(userID, FieldValue.delete());
                writeBatch.delete(userRef);
            } else {
                leagueUpdate.put(userID, level);
                Map<String, Object> userUpdate = new HashMap<>();
                userUpdate.put(StatsEntry.LEVEL, level);
                writeBatch.update(userRef, userUpdate);
            }
            writeBatch.update(leagueRef, leagueUpdate);
        }
        writeBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                insertSelectionListToSQL(insertList);
            }
        });
    }

    private void insertSelectionListToSQL(List<MainPageSelection> list) {
        for (MainPageSelection mainPageSelection : list) {
            insertSelectionToSQL(mainPageSelection);
            mInviteList.remove(mainPageSelection);
        }
        updateRV();
    }

    private void updateRV() {
        if (mSelectionList != null && !mSelectionList.isEmpty()) {
            mErrorView.setVisibility(View.GONE);
        }
        if (mainPageAdapter == null) {
            mainPageAdapter = new MainPageAdapter(mSelectionList, this);
            if (mRecyclerView == null) {
                mRecyclerView = findViewById(R.id.rv_main);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                mRecyclerView.setVisibility(View.VISIBLE);
                mErrorView.setVisibility(View.INVISIBLE);
            }
            mRecyclerView.setAdapter(mainPageAdapter);
        } else {
            mainPageAdapter.notifyDataSetChanged();
        }
    }

    private void insertSelectionToSQL(MainPageSelection mainPageSelection) {
        mSelectionList.add(mainPageSelection);
        ContentValues selectionValues = new ContentValues();
        selectionValues.put(StatsEntry.COLUMN_FIRESTORE_ID, mainPageSelection.getId());
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
        mSelectionList.remove(mainPageSelection);
        updateRV();
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
        final WriteBatch batch = firestore.batch();

        batch.delete(leagueDoc.collection(USERS).document(userID));

        Map<String, Object> updates = new HashMap<>();
        //todo FieldValue.delete()
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
                                            documentReference.collection(PLAYER_LOGS).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot querySnapshot) {
                                                    for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                                        DocumentReference documentReference = documentSnapshot.getReference();
                                                        batch.delete(documentReference);
                                                    }
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
                                                    documentReference.collection(TEAM_LOGS).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot querySnapshot) {
                                                            for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                                                DocumentReference documentReference = documentSnapshot.getReference();
                                                                batch.delete(documentReference);
                                                            }
                                                        }
                                                    });
                                                    batch.delete(documentReference);
                                                }
                                            }
                                            leagueDoc.collection(DELETION_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        QuerySnapshot querySnapshot = task.getResult();
                                                        for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                                            DocumentReference documentReference = documentSnapshot.getReference();
                                                            batch.delete(documentReference);
                                                        }
                                                    }
                                                    leagueDoc.collection(REQUESTS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                QuerySnapshot querySnapshot = task.getResult();
                                                                for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                                                    DocumentReference documentReference = documentSnapshot.getReference();
                                                                    batch.delete(documentReference);
                                                                }
                                                            }
                                                            batch.commit();
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
                    batch.commit();
                }
                SharedPreferences updatePreferences = getSharedPreferences(selectionID + "_updateSettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = updatePreferences.edit();
                editor.clear();
                editor.apply();
            }
        });
    }

    @Override
    public Loader<QuerySnapshot> onCreateLoader(int id, Bundle args) {
        mFireTaskLoader = new FireTaskLoader(this, userID);
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                continueLoadDialog();
            }
        }, 20000);
        return mFireTaskLoader;
    }



    private void continueLoadDialog() {
        if (loadingFinished) {
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        mContinueLoadDialogFragment = new ContinueLoadDialog();
        fragmentManager.beginTransaction().add(mContinueLoadDialogFragment, null).commitAllowingStateLoss();
    }


    @Override
    public void onLoadFinished(android.support.v4.content.Loader<QuerySnapshot> loader, QuerySnapshot querySnapshot) {
        loadingFinished = true;

        if (mContinueLoadDialogFragment != null) {
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mContinueLoadDialogFragment != null) {
                        mContinueLoadDialogFragment.dismiss();
                    }
                }
            });
        }

        if(mInviteList != null && mSelectionList != null) {
            setViews();
            return;
        }

        if (mSelectionList == null) {
            mSelectionList = new ArrayList<>();
        } else {
            mSelectionList.clear();
        }

        if (mInviteList == null) {
            mInviteList = new ArrayList<>();
        } else {
            mInviteList.clear();
        }

        if (querySnapshot == null) {
            mProgressBar.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
            return;
        }

        for (DocumentSnapshot documentSnapshot : querySnapshot) {
            int level = documentSnapshot.getLong(userID).intValue();
            String selectionID = documentSnapshot.getId();
            String name = documentSnapshot.getString(StatsEntry.COLUMN_NAME);
            int type = documentSnapshot.getLong(StatsEntry.TYPE).intValue();
            MainPageSelection mainPageSelection = new MainPageSelection(
                    selectionID, name, type, level);
            if (level < 0) {
                mInviteList.add(mainPageSelection);
            } else if (level >= UsersActivity.LEVEL_VIEW_ONLY) {
                mSelectionList.add(mainPageSelection);
            }
        }
        setViews();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<QuerySnapshot> loader) {
        mRecyclerView.setAdapter(null);

    }

    @Override
    public void loadChoice(boolean load) {
        mErrorView.setVisibility(View.GONE);
        if (load) {
            mFireTaskLoader.cancelLoadInBackground();
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
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        getIntent().putParcelableArrayListExtra("mSelectionList", mSelectionList);
        getIntent().putParcelableArrayListExtra("mInviteList", mInviteList);
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
        mInviteList = getIntent().getParcelableArrayListExtra("mInviteList");
        loadingFinished = true;
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
        if(mRecyclerView != null) {
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);

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
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final DocumentReference statKeeperDocument;

        if (statKeeperID == null) {
            statKeeperDocument = firestore.collection(LEAGUE_COLLECTION).document();
            firestoreLeagueMap.put(StatsEntry.COLUMN_NAME, name);
            firestoreLeagueMap.put(StatsEntry.TYPE, type);
            firestoreLeagueMap.put("creator", null);
        } else {
            statKeeperDocument = firestore.collection(LEAGUE_COLLECTION).document(statKeeperID);
        }

        firestoreLeagueMap.put(userID, level);
        Log.d("xyedd", userID + "   " + level + "  " + statKeeperID);
        statKeeperDocument.set(firestoreLeagueMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("xyedd", "first sucess");
                Map<String, Object> firestoreUserMap = new HashMap<>();
                firestoreUserMap.put(StatsEntry.LEVEL, level);
                firestoreUserMap.put(StatsEntry.EMAIL, userEmail);
                firestoreUserMap.put(StatsEntry.COLUMN_NAME, userDisplayName);
                statKeeperDocument.collection(USERS).document(userID).set(firestoreUserMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("xyedd", "second success");
                                Toast.makeText(MainActivity.this, "OK WORKING SO FAR....", Toast.LENGTH_SHORT).show();

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
                                        getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
                                    } else if (type == MainPageSelection.TYPE_PLAYER) {
                                        ContentValues values = new ContentValues();
                                        values.put(StatsEntry.COLUMN_NAME, name);
                                        getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
                                    }

                                    DocumentReference requestDocument = firestore.collection(LEAGUE_COLLECTION)
                                            .document(selectionID).collection(REQUESTS).document();
                                    StatKeepUser statKeepUser = new StatKeepUser(REQUESTS, name, String.valueOf(type), UsersActivity.LEVEL_VIEW_ONLY - 100);
                                    requestDocument.set(statKeepUser, SetOptions.merge());
                                }
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("xyedd", "second fail");
                        Toast.makeText(MainActivity.this, "Firebase error! Try again!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("xyedd", "first fail");
            }
        });
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

        for (MainPageSelection mainPageSelection : mSelectionList) {
            if (idText.equals(mainPageSelection.getId())) {
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

                            if (level == UsersActivity.LEVEL_VIEW_ONLY) {
                                postMessage(0);
                                addSelection(name, type, level, idText);
                            } else {
                                Log.d("xyedd", "???????? fail");

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