package com.team.abc.ui.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team.abc.CreatePostActivity;
import com.team.abc.R;
import com.team.abc.model.Post;
import com.team.abc.model.User;
import com.team.abc.ui.register.LoginActivity;

import java.util.ArrayList;
import java.util.List;


public class NormalPostFragment extends Fragment {
    FirebaseDatabase database;
    RecyclerView recyclerView;
    List<Post> posts = new ArrayList<>();
    User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseDatabase.getInstance();
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

        user = SharePrefUtil.getUserLogged(getActivity());
        if (user == null) {
            return;
        }
        getData();
    }

    public void clearData() {
        posts.clear();
        recyclerView.getAdapter().notifyDataSetChanged();
    }


    public void getData() {
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
}
