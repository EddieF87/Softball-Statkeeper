package com.example.android.scorekeepdraft1.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.UserListAdapter;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.StatKeepUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter.LEAGUE_COLLECTION;
import static com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter.USERS;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private List<String> testList;
    private static final String TAG = "SettingsActivity";
    private int testint = 0;
    private Map<String, Integer> levelChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MyApp myApp = (MyApp) getApplicationContext();
        mAuth = FirebaseAuth.getInstance();

        if (myApp.getCurrentSelection() == null || mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        String leagueID = mainPageSelection.getId();
        String myTeamName = mainPageSelection.getName();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userID = currentUser.getUid();



        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(USERS)
                .whereEqualTo("level", 0)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<StatKeepUser> statKeepUserList = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                StatKeepUser statKeepUser = document.toObject(StatKeepUser.class);
                                statKeepUser.setId(document.getId());
                                statKeepUserList.add(statKeepUser);
                            }
                            statKeepUserList.add(new StatKeepUser("Charlees", "fvvweve", "vfvffvvfvf", 5));
                            statKeepUserList.add(new StatKeepUser("Charlees", "fvvweve", "vfvffvvfvf", 5));
                            statKeepUserList.add(new StatKeepUser("Charlees", "fvvweve", "vfvffvvfvf", 5));
                            statKeepUserList.add(new StatKeepUser("Charlees", "fvvweve", "vfvffvvfvf", 5));
                            statKeepUserList.add(new StatKeepUser("Charlees", "fvvweve", "vfvffvvfvf", 5));

                            RecyclerView recyclerView;
                            recyclerView = findViewById(R.id.rv_users);
                            setRecyclerViewAdapter(recyclerView, statKeepUserList);
                            recyclerView = findViewById(R.id.rv_requests);
                            setRecyclerViewAdapter(recyclerView, statKeepUserList);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        firestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(USERS)
                .whereEqualTo("level", 0)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<StatKeepUser> adminList = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                StatKeepUser statKeepUser = document.toObject(StatKeepUser.class);
                                adminList.add(statKeepUser);
                            }
                            StatKeepUser statKeepUser = adminList.get(0);
                            TextView nameView = findViewById(R.id.admin_name_view);
                            TextView emailView = findViewById(R.id.admin_email_view);
                            nameView.setText(statKeepUser.getName());
                            emailView.setText(statKeepUser.getEmail());
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void setRecyclerViewAdapter(RecyclerView recyclerView, List<StatKeepUser> statKeepUserList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        UserListAdapter userListAdapter = new UserListAdapter(this, statKeepUserList);
        recyclerView.setAdapter(userListAdapter);
    }

    public void testClick(String name) {
        if (levelChanges == null) {
            levelChanges = new HashMap<>();
            testint = 0;
        }
        testint++;
        levelChanges.put(name, testint);
    }

    public void saveChanges(View v) {
        if(levelChanges == null) {
            return;
        }
        StringBuilder printout = new StringBuilder();
        for (Map.Entry<String, Integer> entry : levelChanges.entrySet()) {
            String toast = entry.getKey() + ":" + entry.getValue().toString();
            Toast.makeText(SettingsActivity.this, toast, Toast.LENGTH_SHORT).show();
        }
    }
}
