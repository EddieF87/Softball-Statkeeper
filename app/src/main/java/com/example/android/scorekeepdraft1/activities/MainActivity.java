package com.example.android.scorekeepdraft1.activities;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.BuildConfig;
import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.MainPageAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter.LEAGUE_COLLECTION;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final String AUTH = "FirebaseAuth";
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 0;

    private ArrayList<MainPageSelection> mSelections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton addPlayer = findViewById(R.id.btn_create_plyr);
        addPlayer.setOnClickListener(this);
        FloatingActionButton addTeam = findViewById(R.id.btn_create_join_tm);
        addTeam.setOnClickListener(this);
        FloatingActionButton addLeague = findViewById(R.id.btn_create_join_lg);
        addLeague.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        authenticateUser();
    }

    protected void authenticateUser() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Log.v(TAG, "signed in already");
            loadLeaguesTeamsPlayers();
            invalidateOptionsMenu();
        } else {
            Log.d(AUTH, "else");
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
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
                loadLeaguesTeamsPlayers();
            } else {
                Log.d(AUTH, "USER NOT AUTHENTICATED");
            }
        }
        invalidateOptionsMenu();
    }

    private void loadLeaguesTeamsPlayers() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userID = currentUser.getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(LEAGUE_COLLECTION)
                .whereEqualTo(userID, true).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (mSelections == null) {
                            mSelections = new ArrayList<>();
                        } else {
                            mSelections.clear();
                        }
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                String selectionID = documentSnapshot.getId();
                                String name = documentSnapshot.getString("name");
                                int type = documentSnapshot.getLong("type").intValue();
                                MainPageSelection mainPageSelection = new MainPageSelection(selectionID, name, type);
                                mSelections.add(mainPageSelection);
                                Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());
                            }
                            if (mSelections.isEmpty()) {
                                TextView rvErrorView = findViewById(R.id.error_rv_main);
                                rvErrorView.setVisibility(View.VISIBLE);
                            } else {
                                MainPageAdapter mainPageAdapter = new MainPageAdapter(mSelections, MainActivity.this);
                                RecyclerView recyclerView = findViewById(R.id.rv_main);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                                recyclerView.setAdapter(mainPageAdapter);
                            }
                        } else {
                            TextView rvErrorView = findViewById(R.id.error_rv_main);
                            rvErrorView.setVisibility(View.VISIBLE);
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_league, menu);
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
                authenticateUser();
                break;
            case R.id.action_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                finish();
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
        String selection;
        switch (view.getId()) {
            case R.id.btn_create_plyr:
                type = MainPageSelection.TYPE_PLAYER;
                selection = "Player";
                break;

            case R.id.btn_create_join_tm:
                type = MainPageSelection.TYPE_TEAM;
                selection = "Team";
                break;

            case R.id.btn_create_join_lg:
                type = MainPageSelection.TYPE_LEAGUE;
                selection = "League";
                break;

            default:
                Log.e(TAG, "error with onclick");
                return;
        }
        joinCreateDialog(type, selection);
    }

    public void joinCreateDialog(final int type, String selection) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String dialogMessage = "Join or Create a " + selection;
        builder.setMessage(dialogMessage).
                setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        enterNameDialog(type);
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });
        if (type != MainPageSelection.TYPE_PLAYER) {
            builder.setNegativeButton("Join", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (dialogInterface != null) {
                        dialogInterface.dismiss();
                    }
                }
            });
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void enterNameDialog(final int type) {
        final Intent intent;
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_edit_name, null))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser == null) {
                            if (dialogInterface != null) {
                                dialogInterface.dismiss();
                            }
                            return;
                        }
                        String userID = currentUser.getUid();
                        Dialog dialog1 = (Dialog) dialogInterface;
                        EditText editText = dialog1.findViewById(R.id.username);
                        String name = editText.getText().toString();
                        Map<String, Object> firestoreMap = new HashMap<>();
                        firestoreMap.put("name",  name);
                        firestoreMap.put("type", type);
                        firestoreMap.put(userID, true);
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        DocumentReference documentReference = firestore.collection(LEAGUE_COLLECTION).document();
                        documentReference.set(firestoreMap);
                        MyApp myApp = (MyApp)getApplicationContext();
                        MainPageSelection mainPageSelection = new MainPageSelection(documentReference.getId(), name, type);
                        myApp.setCurrentSelection(mainPageSelection);
                        if(type == MainPageSelection.TYPE_TEAM) {
                            ContentValues values = new ContentValues();
                            values.put(StatsEntry.COLUMN_NAME, name);
                            getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
                        } else if(type == MainPageSelection.TYPE_PLAYER) {
                            ContentValues values = new ContentValues();
                            values.put(StatsEntry.COLUMN_NAME, name);
                            getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
                        }
                        startActivity(intent);
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}