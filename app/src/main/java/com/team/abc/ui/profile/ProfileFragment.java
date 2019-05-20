package com.team.abc.ui.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.team.abc.MapFragment;
import com.team.abc.ui.post.PostFragment;
import com.team.abc.ui.register.LoginActivity;
import com.team.abc.CreatePostActivity;
import com.team.abc.R;
import com.team.abc.model.Post;
import com.team.abc.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    TextView tvUserName;
    TextView tvLogin;
    TextView tvAcc;
    BillingClient billingClient;
    DrawerLayout drawerLayout;
    ImageView imgMenu;

    ViewPager viewPager;
    List<Post> posts = new ArrayList<>();
    User user;
    TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profie, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseDatabase.getInstance();

        tvUserName = view.findViewById(R.id.tvUserName);
        tvLogin = view.findViewById(R.id.tvLogin);
        tvAcc = view.findViewById(R.id.tvAcc);
        viewPager = view.findViewById(R.id.viewPager);
        drawerLayout = view.findViewById(R.id.drawerLayout);
        imgMenu = view.findViewById(R.id.imgMenu);
        tabLayout = view.findViewById(R.id.tabLayout);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading ...");
        progressDialog.setCanceledOnTouchOutside(false);

        viewPager.setAdapter(new ProfileFragmentPager(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        user = SharePrefUtil.getUserLogged(getActivity());
        if (user == null) {
            tvUserName.setText("No User");
            tvLogin.setText("LogIn");
            tvAcc.setVisibility(View.GONE);
        } else {
            tvUserName.setText(user.getName());
            tvLogin.setText("LogOut");
            if (user.isAccVip()) {
                tvAcc.setVisibility(View.GONE);

            } else {
                tvAcc.setVisibility(View.VISIBLE);
            }

            database.getReference("posts").orderByChild("userId").equalTo(user.getMyPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot childChild : dataSnapshot.getChildren()) {
                        posts.add(childChild.getValue(Post.class));
                    }
                    //recyclerView.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(Gravity.START);
                }
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                if (user != null) {
                    user = null;
                    SharePrefUtil.clearUser(getActivity());
                    tvUserName.setText("No User");
                    tvLogin.setText("ĐĂNG NHẬP");
                    for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
                        Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, i);
                        if (fragment instanceof NormalPostFragment) {
                            ((NormalPostFragment) fragment).clearData();
                        }
                        if (fragment instanceof VipPostFragment) {
                            ((VipPostFragment) fragment).clearData();
                        }
                    }
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        tvAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                progressDialog.show();
                billingClient.startConnection(new BillingClientStateListener() {
                    @Override
                    public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {

                        if (billingResponseCode == BillingClient.BillingResponse.OK) {
                            List<String> skuList = new ArrayList<>();
                             skuList.add("account1");

                            // dong skulist.add("android.test.purchase")
                           // skuList.add("android.test.purchased");
                            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                            billingClient.querySkuDetailsAsync(params.build(),
                                    new SkuDetailsResponseListener() {
                                        @Override
                                        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {

                                            // Process the result.
                                            progressDialog.dismiss();
                                            if (skuDetailsList != null && !skuDetailsList.isEmpty()) {
                                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                                        .setSkuDetails(skuDetailsList.get(0))
                                                        .build();

                                                billingClient.launchBillingFlow(getActivity(), flowParams);
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onBillingServiceDisconnected() {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "onBillingServiceDisconnected", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        billingClient = BillingClient.newBuilder(getActivity()).setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {

                if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                    database.getReference("users").child(user.getMyPhone()).child("accVip").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                tvAcc.setVisibility(View.GONE);
                                for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
                                    Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, i);
                                    if (fragment instanceof VipPostFragment) {
                                        ((VipPostFragment) fragment).getData();
                                        return;
                                    }
                                }
                            }
                        }
                    });
                } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                } else {
                    // Handle any other error codes.
                }

            }
        }).build();
    }

    static class ProfileFragmentPager extends FragmentStatePagerAdapter {

        public ProfileFragmentPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new NormalPostFragment();
            } else {
                return new VipPostFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return (position == 0) ? "Bài đăng bình thường" : "Bài đăng đề xuất";
        }
    }
}
