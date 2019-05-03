package com.team.abc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.team.abc.model.User;
import com.team.abc.ui.post.PostFragment;
import com.team.abc.ui.profile.ProfileFragment;
import com.team.abc.ui.profile.SharePrefUtil;

public class HomeActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Integer[] resIds = {R.drawable.ic_map, R.drawable.ic_post, R.drawable.ic_global, R.drawable.ic_account};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);


        viewPager.setAdapter(new HomeFragmentPager(getSupportFragmentManager(), SharePrefUtil.getUserLogged(this)));
        viewPager.setOffscreenPageLimit(2);
        viewPager.setCurrentItem(getIntent().getIntExtra("index", 0));
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(resIds[i]);
        }
    }


    static class HomeFragmentPager extends FragmentStatePagerAdapter {
        private User user;

        public HomeFragmentPager(FragmentManager fm, User user) {
            super(fm);
            this.user = user;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new MapFragment();
            } else if (position == 1) {
                return PostFragment.newInstance(false);
            } else if (position == 2) {
                return PostFragment.newInstance(true);
            } else {
                return new ProfileFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
