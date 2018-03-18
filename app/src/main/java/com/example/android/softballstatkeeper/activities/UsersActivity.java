package com.example.android.softballstatkeeper.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.softballstatkeeper.MyApp;
import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.data.StatsContract;
import com.example.android.softballstatkeeper.dialogs.InviteUserDialog;
import com.example.android.softballstatkeeper.fragments.UserFragment;
import com.example.android.softballstatkeeper.models.MainPageSelection;
import com.example.android.softballstatkeeper.models.StatKeepUser;
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

import static com.example.android.softballstatkeeper.data.FirestoreHelper.LEAGUE_COLLECTION;
import static com.example.android.softballstatkeeper.data.FirestoreHelper.USERS;

public class UsersActivity extends AppCompatActivity
        implements UserFragment.OnListFragmentInteractionListener,
        InviteUserDialog.OnFragmentInteractionListener {

    private static final String TAG = "UsersActivity";
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
    private String creatorEmail;

    private UserFragment userFragment;
    private UserFragment requestFragment;
    private ViewPager mViewPager;

    private ProgressBar mProgressBar;
    private Button startAdderBtn;
    private Button saveBtn;
    private Button resetBtn;

    private String mSelectionID;
    private String mSelectionName;

    private int mLevel;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mSelectionID = mainPageSelection.getId();
            mSelectionName = mainPageSelection.getName();
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

        mViewPager = findViewById(R.id.user_view_pager);
        mProgressBar = findViewById(R.id.progressBar2);

        setButtons();

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

        mViewPager.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        firestore.collection(LEAGUE_COLLECTION).document(mSelectionID).collection(USERS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
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
        creatorEmail = statKeepUser.getEmail();

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
    }

    private void createPager() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        userFragment = UserFragment.newInstance(mUserList, mLevel);
                        return userFragment;
                    case 1:
                        requestFragment = UserFragment.newInstance(mRequestList, mLevel);
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

        if (!mRequestList.isEmpty()) {
            TextView textView = new TextView(this);
            String requests = "REQUESTS (" + mRequestList.size() + ")";
            textView.setText(requests);
            textView.setTextColor(Color.RED);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(Typeface.DEFAULT_BOLD);

            tabLayout = findViewById(R.id.league_tab_layout);
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            tab.setCustomView(textView);
        }
    }

    public void saveChanges(View v) {
        if (levelChanges == null) {
            return;
        }
        WriteBatch batch = firestore.batch();
        for (Map.Entry<String, Integer> entry : levelChanges.entrySet()) {
            String id = entry.getKey();
            int level = entry.getValue();
            DocumentReference league = firestore.collection(LEAGUE_COLLECTION).document(mSelectionID);
            DocumentReference leagueUser = firestore.collection(LEAGUE_COLLECTION).document(mSelectionID)
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

    public void sendEmails(View view) {
        getNameEmailDetails();
//        int userSize = mUserList.size();
//        List<String> users = new ArrayList<>();
//        for (int i = 0; i < userSize; i++) {
//            StatKeepUser statKeepUser = mUserList.get(i);
//            String user = statKeepUser.getEmail();
//            users.add(user);
//        }
//
//        String[] emailList = new String[userSize];
//        emailList = users.toArray(emailList);
//
//        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
//                "mailto", creatorEmail, null));
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mSelectionName + " Update");
//        emailIntent.putExtra(Intent.EXTRA_BCC, emailList);
//        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public void getNameEmailDetails(){
        ArrayList<String> names = new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor cur1 = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (cur1.moveToNext()) {
                    //to get the contact names
                    String name=cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    Log.e("Name :", name);
                    String email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    Log.e("Email", email);
                    if(email!=null){
                        names.add(name);
                        Log.d("zizi", name + " - " + email);
                    }
                }
                cur1.close();
            }
        }
    }

    @Override
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
                    if (!documentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = documentSnapshots.get(0);
                        String userID = documentSnapshot.getId();

                        Map<String, Object> data = new HashMap<>();
                        data.put(StatsContract.StatsEntry.EMAIL, email);
                        data.put(StatsContract.StatsEntry.COLUMN_NAME, null);
                        data.put(StatsContract.StatsEntry.LEVEL, level);
                        firestore.collection(LEAGUE_COLLECTION).document(mSelectionID)
                                .collection(USERS).document(userID).set(data, SetOptions.merge());

                        Map<String, Integer> data2 = new HashMap<>();
                        data2.put(userID, -level);
                        firestore.collection(LEAGUE_COLLECTION).document(mSelectionID)
                                .set(data2, SetOptions.merge());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        startAdderBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCancel() {
        startAdderBtn.setVisibility(View.VISIBLE);
    }

}
