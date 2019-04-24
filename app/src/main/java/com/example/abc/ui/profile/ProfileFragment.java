package com.example.abc.ui.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abc.LoginActivity;
import com.example.abc.PostActivity;
import com.example.abc.R;
import com.example.abc.SigupActivity;
import com.example.abc.model.Post;
import com.example.abc.model.User;
import com.example.abc.ui.post.PostAdapter;
import com.example.abc.ui.profile.ProfileAdapter;
import com.example.abc.ui.profile.SharePrefUtil;
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
                Intent intent = new Intent(getActivity(), PostActivity.class);
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
    }
}
