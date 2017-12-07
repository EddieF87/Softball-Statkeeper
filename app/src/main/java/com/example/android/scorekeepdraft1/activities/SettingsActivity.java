package com.example.android.scorekeepdraft1.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.MainPageAdapter;
import com.example.android.scorekeepdraft1.fragments.UserFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.StatKeepUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter.LEAGUE_COLLECTION;
import static com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter.USERS;

public class SettingsActivity extends AppCompatActivity implements UserFragment.OnListFragmentInteractionListener {

    private FirebaseAuth mAuth;

    private static final String TAG = "SettingsActivity";
    private static final String SAVED_MAP = "map";
    private static final String SAVED_USERLIST = "userlist";
    private static final String SAVED_REQUESTLIST = "requestlist";
    private static final String SAVED_CREATOR = "creator";

    private static final int REMOVE_USER = 0;
    private static final int ACCESS_REQUEST = 1;
    private static final int VIEW_ONLY = 2;
    private static final int VIEW_WRITE = 3;
    private static final int ADMIN = 4;
    private static final int CREATOR = 5;

    private ArrayList<StatKeepUser> mUserList;
    private ArrayList<StatKeepUser> mRequestList;
    private HashMap<String, Integer> levelChanges;
    private StatKeepUser creator;

    private UserFragment userFragment;
    private UserFragment requestFragment;
    private ViewPager mViewPager;
    private ProgressBar mProgressBar;
    private String leagueID;

    private EditText addUserText;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.d(TAG, "hoppy start!");

        MyApp myApp = (MyApp) getApplicationContext();
        mAuth = FirebaseAuth.getInstance();

        if (myApp.getCurrentSelection() == null || mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        leagueID = mainPageSelection.getId();

        //todo
        FirebaseUser currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        String userID = currentUser.getEmail();

        mViewPager = findViewById(R.id.user_view_pager);
        mProgressBar = findViewById(R.id.progressBar2);

        addUserText = findViewById(R.id.add_player_text);
        final Button submitUserBtn = findViewById(R.id.add_player_submit);
        submitUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser();
            }
        });

        final FloatingActionButton startAdderBtn = findViewById(R.id.btn_start_adder);
        startAdderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAdderBtn.setVisibility(View.GONE);
                addUserText.setVisibility(View.VISIBLE);
                submitUserBtn.setVisibility(View.VISIBLE);
            }
        });

        if (savedInstanceState != null) {
            levelChanges = (HashMap<String, Integer>) savedInstanceState.getSerializable(SAVED_MAP);
            mUserList = savedInstanceState.getParcelableArrayList(SAVED_USERLIST);
            mRequestList = savedInstanceState.getParcelableArrayList(SAVED_REQUESTLIST);
            creator = savedInstanceState.getParcelable(SAVED_CREATOR);
            setCreator(creator);
            createPager();
            return;
        }

        mViewPager.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "hoppy begin firestore creator retrieval!");
        firestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(USERS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "hoppy creator retrieval completed");
                            mUserList = new ArrayList<>();
                            mRequestList = new ArrayList<>();

                            for (DocumentSnapshot document : task.getResult()) {

                                StatKeepUser statKeepUser = document.toObject(StatKeepUser.class);
                                statKeepUser.setId(document.getId());

                                int level = statKeepUser.getLevel();
                                switch (level) {
                                    case CREATOR:
                                        creator = statKeepUser;
                                        setCreator(statKeepUser);
                                        break;
                                    case ACCESS_REQUEST:
                                        mRequestList.add(statKeepUser);
                                        break;
                                    default:
                                        mUserList.add(statKeepUser);
                                }
                            }
                            createPager();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void addUser() {
        InputMethodManager inputManager = (InputMethodManager)
                this.getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        final String userEmail = addUserText.getText().toString();
        firestore.collection(USERS).whereEqualTo("email", userEmail)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                    if (!documentSnapshots.isEmpty()){
                        DocumentSnapshot documentSnapshot = documentSnapshots.get(0);
                        String userID = documentSnapshot.getId();
                        //todo level
                        int level = 3;

                        Map<String, Object> data = new HashMap<>();
                        data.put("email", userEmail);
                        data.put("name", null);
                        data.put("level", level);
                        firestore.collection(LEAGUE_COLLECTION).document(leagueID)
                                .collection(USERS).document(userID).set(data);

                        Map<String, Integer> data2 = new HashMap<>();
                        data2.put(userID, level);
                        firestore.collection(LEAGUE_COLLECTION).document(leagueID)
                                .set(data2, SetOptions.merge());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        addUserText.setText("");
    }

    private void setCreator(StatKeepUser statKeepUser) {
        TextView nameView = findViewById(R.id.admin_name_view);
        TextView emailView = findViewById(R.id.admin_email_view);
        nameView.setText(statKeepUser.getName());
        emailView.setText(statKeepUser.getEmail());
    }

    private void createPager() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        userFragment = UserFragment.newInstance(mUserList);
                        return userFragment;
                    case 1:
                        requestFragment = UserFragment.newInstance(mRequestList);
                        return requestFragment;
                    default:
                        return null;
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Users";
                    case 1:
                        return "Requests";
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        mProgressBar.setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
        TabLayout tabLayout = findViewById(R.id.league_tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }


    public void saveChanges(View v) {
        if (levelChanges == null) {
            return;
        }
        for (Map.Entry<String, Integer> entry : levelChanges.entrySet()) {
            String id = entry.getKey();
            int level = entry.getValue();
            if (level == 0) {
                firestore.collection(LEAGUE_COLLECTION).document(leagueID)
                        .collection(USERS).document(id).delete();
            } else {
                firestore.collection(LEAGUE_COLLECTION).document(leagueID)
                        .collection(USERS).document(id).update("level", level);
            }
        }
        onBackPressed();
    }

    public void resetChanges(View v) {

        if (levelChanges == null) {
            return;
        }
        levelChanges.clear();
        userFragment.swapList(mUserList);
        requestFragment.swapList(mRequestList);
    }

    @Override
    public void onListFragmentInteraction(String name, int level) {
        if (levelChanges == null) {
            levelChanges = new HashMap<>();
        }
        levelChanges.put(name, level);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (levelChanges != null) {
            outState.putSerializable(SAVED_MAP, levelChanges);
        }
        outState.putParcelableArrayList(SAVED_USERLIST, mUserList);
        outState.putParcelableArrayList(SAVED_REQUESTLIST, mRequestList);
        outState.putParcelable(SAVED_CREATOR, creator);
    }
}
