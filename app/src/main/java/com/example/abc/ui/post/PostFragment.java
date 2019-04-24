package com.example.abc.ui.post;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.abc.EndLessScrollListener;
import com.example.abc.LoadMoreListener;
import com.example.abc.LoginActivity;
import com.example.abc.PostActivity;
import com.example.abc.R;
import com.example.abc.model.State;
import com.example.abc.ui.profile.SharePrefUtil;
import com.example.abc.model.Post;
import com.example.abc.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostFragment extends Fragment {
    private RecyclerView recyclerView;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    private List<Post> posts = new ArrayList<>();
    private FloatingActionButton fabPost;
    private User user;
    private String oldestId = "0";
    private AppCompatSpinner spnState;
    private List<String> states = new ArrayList<>();
    private String state = "";
    private boolean isFirst = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        fabPost = view.findViewById(R.id.fabPost);
        spnState = view.findViewById(R.id.spnState);
        database = FirebaseDatabase.getInstance();
        user = SharePrefUtil.getUserLogged(getActivity());
        initListener();

        database.getReference("city").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    states.add(child.getValue(State.class).getCity());
                }

                states.add(0, "all");
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, states);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnState.setAdapter(dataAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new PostAdapter(posts, getActivity()));

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading ...");
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();
        database.getReference("posts").orderByChild("timestamp").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("xxx", "onDataChange: 1");
                progressDialog.dismiss();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("xxx", "onDataChange: ");
                    posts.add(child.getValue(Post.class));
                }
                if (!posts.isEmpty()) {
                    oldestId = String.valueOf(posts.get(posts.size() - 1).getId());
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initListener() {
        fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user == null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), PostActivity.class);
                    startActivity(intent);
                }
            }
        });

        spnState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirst) {
                    isFirst = false;
                    return;
                }
                progressDialog.show();
                if (position == 0) {
                    state = "";
                    queryPost();
                } else {
                    state = states.get(position);
                    queryPostByKey();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        recyclerView.addOnScrollListener(new EndLessScrollListener() {
            @Override
            public void seeMore() {
                if (state.isEmpty()) {
                    queryPost();
                }
            }
        });
    }


    private void queryPostByKey() {
        database.getReference("posts").orderByChild("state").equalTo(state).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                progressDialog.dismiss();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    posts.add(child.getValue(Post.class));
                }
                if (!posts.isEmpty()) {
                    oldestId = String.valueOf(posts.get(posts.size() - 1).getId());
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();

            }
        });
    }

    private void queryPost() {
        database.getReference("posts").orderByKey().startAt(String.valueOf(posts.get(posts.size() - 1).getId())).limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                for (DataSnapshot childChild : dataSnapshot.getChildren()) {
                    if (oldestId.equals(String.valueOf(childChild.getValue(Post.class).getId()))) {
                        oldestId = "0";
                    } else {
                        Log.d("xx", "onDataChange: " + childChild.getValue(Post.class).getId());
                        posts.add(childChild.getValue(Post.class));
                    }
                }
                oldestId = String.valueOf(posts.get(posts.size() - 1).getId());
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Log.d("xx", "onDataChange: " + databaseError.getMessage());
            }
        });

    }
}
