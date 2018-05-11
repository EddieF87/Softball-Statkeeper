package xyz.sleekstats.softball.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.fragments.BoxScoreFragment;
import xyz.sleekstats.softball.fragments.PlayRecapFragment;
import xyz.sleekstats.softball.objects.MainPageSelection;

public class BoxScoreActivity extends AppCompatActivity {

    private String awayTeamID;
    private String homeTeamID;
    private String awayTeamName;
    private String homeTeamName;
    private String mStatKeeperName;
    private String mStatKeeperID;
    private int awayTeamRuns;
    private int homeTeamRuns;
    private int totalInnings;
    private int mStatKeeperType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mStatKeeperID = mainPageSelection.getId();
            mStatKeeperType = mainPageSelection.getType();
            mStatKeeperName = mainPageSelection.getName();
            setTitle(mStatKeeperName + " BoxScore");
        } catch (Exception e) {
            goToMain();
        }
        if(mStatKeeperID == null) {
            goToMain();
            return;
        }

        Bundle b;
        if (getIntent().getExtras() != null) {
            b = getIntent().getExtras();
        } else {
            finish();
            return;
        }
        awayTeamID = b.getString("awayTeamID", null);
        homeTeamID = b.getString("homeTeamID", null);
        awayTeamName = b.getString("awayTeamName");
        homeTeamName = b.getString("homeTeamName");
        totalInnings = b.getInt("totalInnings", 0);
        awayTeamRuns = b.getInt("awayTeamRuns", 0);
        homeTeamRuns = b.getInt("homeTeamRuns", 0);
        final int inningNumber = b.getInt("inningNumber", 0);

        ViewPager mViewPager = findViewById(R.id.my_view_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return BoxScoreFragment.newInstance(mStatKeeperID, mStatKeeperName, mStatKeeperType, awayTeamID, homeTeamID,
                                awayTeamName, homeTeamName, totalInnings, awayTeamRuns, homeTeamRuns);
                    case 1:
                        return PlayRecapFragment.newInstance(awayTeamID, homeTeamID, inningNumber, mStatKeeperID);
                }
                return null;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Stats";
                    case 1:
                        return "Plays";
                    default:
                        return null;
                }
            }

        });

        TabLayout tabLayout = findViewById(R.id.my_tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void goToMain(){
        Intent intent = new Intent(BoxScoreActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
