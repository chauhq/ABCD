package com.team.abc.ui.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    TextView btnLogin;
    TextView btnAcc;
    BillingClient billingClient;

    RecyclerView recyclerView;
    List<Post> posts = new ArrayList<>();
    User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profie, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseDatabase.getInstance();

        tvUserName = view.findViewById(R.id.tvUserName);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnAcc = view.findViewById(R.id.btnAcc);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new ProfilePostAdapter(posts, getActivity(), new OnPostListener() {
            @Override
            public void deletePost(final Post post) {
                database.getReference("posts").child(String.valueOf(post.getId())).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            posts.remove(post);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        } else {
                            Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void editPost(Post post) {
                Intent intent = new Intent(getActivity(), CreatePostActivity.class);
                intent.putExtra(Post.class.getSimpleName(), post);
                startActivity(intent);
            }
        }));

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading ...");
        progressDialog.setCanceledOnTouchOutside(false);

        user = SharePrefUtil.getUserLogged(getActivity());
        if (user == null) {
            tvUserName.setText("No User");
            btnLogin.setText("LogIn");

        } else {
            tvUserName.setText(user.getName());
            btnLogin.setText("LogOut");

            database.getReference("posts").orderByChild("userId").equalTo(user.getMyPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot childChild : dataSnapshot.getChildren()) {
                        posts.add(childChild.getValue(Post.class));
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    user = null;
                    SharePrefUtil.clearUser(getActivity());
                    tvUserName.setText("No User");
                    btnLogin.setText("LogIn");
                    posts.clear();
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                billingClient.startConnection(new BillingClientStateListener() {
                    @Override
                    public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                        if (billingResponseCode == BillingClient.BillingResponse.OK) {
                            // The BillingClient is ready. You can query purchases here.
                            List<String> skuList = new ArrayList<>();
                            // skuList.add("abcd1");
                            // test
                            skuList.add("android.test.purchased");
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
                            } else {
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

}
