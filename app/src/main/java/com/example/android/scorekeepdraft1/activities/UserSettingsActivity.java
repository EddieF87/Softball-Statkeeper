package com.example.android.scorekeepdraft1.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.dialogs.InviteUserDialogFragment;
import com.example.android.scorekeepdraft1.fragments.UserFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.StatKeepUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.android.scorekeepdraft1.data.FirestoreHelper.LEAGUE_COLLECTION;
import static com.example.android.scorekeepdraft1.data.FirestoreHelper.USERS;

public class UserSettingsActivity extends AppCompatActivity
        implements UserFragment.OnListFragmentInteractionListener,
        InviteUserDialogFragment.OnFragmentInteractionListener{

    private static final String TAG = "UserSettingsActivity";
    private static final String SAVED_MAP = "map";
    private static final String SAVED_USERLIST = "userlist";
    private static final String SAVED_REQUESTLIST = "requestlist";
    private static final String SAVED_CREATOR = "creator";

    public static final int LEVEL_REMOVE_USER = 0;
    public static final int LEVEL_ACCESS_REQUEST = 1;
    public static final int LEVEL_VIEW_ONLY = 2;
    public static final int LEVEL_VIEW_WRITE = 3;
    public static final int LEVEL_ADMIN = 4;
    public static final int LEVEL_CREATOR = 5;

    private ArrayList<StatKeepUser> mUserList;
    private ArrayList<StatKeepUser> mRequestList;
    private HashMap<String, Integer> levelChanges;
    private StatKeepUser creator;

    private UserFragment userFragment;
    private UserFragment requestFragment;
    private ViewPager mViewPager;
    private ProgressBar mProgressBar;
    private FloatingActionButton startAdderBtn;

    private String leagueID;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        Log.d(TAG, "hoppy start!");

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            leagueID = mainPageSelection.getId();
        } catch (Exception e) {
            Intent intent = new Intent(UserSettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        firestore = FirebaseFirestore.getInstance();

        mViewPager = findViewById(R.id.user_view_pager);
        mProgressBar = findViewById(R.id.progressBar2);

        startAdderBtn = findViewById(R.id.btn_start_adder);
        startAdderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAdderBtn.setVisibility(View.GONE);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DialogFragment newFragment = new InviteUserDialogFragment();
                newFragment.show(fragmentTransaction, "");
            }
        });

        if (savedInstanceState != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            userFragment = (UserFragment) fragmentManager.getFragments().get(0);
            requestFragment = (UserFragment) fragmentManager.getFragments().get(1);
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
                                    case LEVEL_CREATOR:
                                        creator = statKeepUser;
                                        setCreator(statKeepUser);
                                        break;
                                    case LEVEL_ACCESS_REQUEST:
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
        WriteBatch batch = firestore.batch();
        for (Map.Entry<String, Integer> entry : levelChanges.entrySet()) {
            String id = entry.getKey();
            int level = entry.getValue();
            DocumentReference league = firestore.collection(LEAGUE_COLLECTION).document(leagueID);
            DocumentReference leagueUser = firestore.collection(LEAGUE_COLLECTION).document(leagueID)
                    .collection(USERS).document(id);
            if (level == 0) {
                batch.update(league, id, 0);
                batch.delete(leagueUser);
            } else {
                batch.update(league, id, level);
                batch.update(leagueUser, StatsContract.StatsEntry.LEVEL, level);
            }
        }
        batch.commit();
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
    public void onUserLevelChanged(String name, int level) {
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

    @Override
    public void onInviteUser(final String email, final int level) {
        firestore.collection(USERS).whereEqualTo(StatsContract.StatsEntry.EMAIL, email)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                    if (!documentSnapshots.isEmpty()){
                        DocumentSnapshot documentSnapshot = documentSnapshots.get(0);
                        String userID = documentSnapshot.getId();

                        Map<String, Object> data = new HashMap<>();
                        data.put(StatsContract.StatsEntry.EMAIL, email);
                        data.put(StatsContract.StatsEntry.COLUMN_NAME, null);
                        data.put(StatsContract.StatsEntry.LEVEL, level);
                        firestore.collection(LEAGUE_COLLECTION).document(leagueID)
                                .collection(USERS).document(userID).set(data, SetOptions.merge());

                        Map<String, Integer> data2 = new HashMap<>();
                        data2.put(userID, -level);
                        firestore.collection(LEAGUE_COLLECTION).document(leagueID)
                                .set(data2, SetOptions.merge());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        startAdderBtn.setVisibility(View.VISIBLE);
    }
}
