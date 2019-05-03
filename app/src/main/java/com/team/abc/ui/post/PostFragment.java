package com.team.abc.ui.post;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.team.abc.CreatePostActivity;
import com.team.abc.EndLessScrollListener;
import com.team.abc.R;
import com.team.abc.model.State;
import com.team.abc.ui.profile.SharePrefUtil;
import com.team.abc.model.Post;
import com.team.abc.model.User;
import com.team.abc.ui.register.LoginActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostFragment extends Fragment {
    private static final String IS_ACC_VIP = "is_acc_vip";
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
    private EndLessScrollListener endLessScrollListener;
    String nodeName;
    private LinearLayout llSpinner;
    private TextView tvTitle;

    public static PostFragment newInstance(boolean isAdmin) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_ACC_VIP, isAdmin);
        PostFragment fragment = new PostFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

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
        llSpinner = view.findViewById(R.id.llSpinner);
        tvTitle = view.findViewById(R.id.tvTitle);
        database = FirebaseDatabase.getInstance();


        if (getArguments().getBoolean(IS_ACC_VIP)) {
            nodeName = "suggestion";
            tvTitle.setText("Post Suggestion");
            llSpinner.setVisibility(View.GONE);
            fabPost.hide();
        } else {
            nodeName = "posts";
        }
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
        database.getReference(nodeName).orderByKey().limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("xx", "onDataChange: " + child.getValue(Post.class).getId());
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
                    Intent intent = new Intent(getActivity(), CreatePostActivity.class);
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
                posts.clear();
                progressDialog.show();
                if (position == 0) {
                    endLessScrollListener.resetData();
                    state = "";
                    database.getReference(nodeName).orderByKey().limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            progressDialog.dismiss();
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                Log.d("xx", "onDataChange: " + child.getValue(Post.class).getId());
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
                } else {
                    state = states.get(position);
                    Log.d("xxxx", "onItemSelected: " + state);
                    queryPostByKey();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        endLessScrollListener = new EndLessScrollListener() {
            @Override
            public void seeMore() {
                Log.d("xxx", "seeMore: " + state);
                if (state.isEmpty()) {
                    if (posts.size() < 10) {
                        return;
                    }
                    queryPost();
                }
            }
        };

        recyclerView.addOnScrollListener(endLessScrollListener);
    }


    private void queryPostByKey() {
        database.getReference("posts").orderByChild("state").equalTo(state).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                progressDialog.dismiss();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("xx", "onDataChange: " + child.getValue(Post.class).getId());
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
        database.getReference(nodeName).orderByKey().startAt(String.valueOf(posts.get(posts.size() - 1).getId())).limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (oldestId.equals(String.valueOf(child.getValue(Post.class).getId()))) {
                        oldestId = "0";
                    } else {
                        Log.d("xx", "onDataChange: " + child.getValue(Post.class).getId());
                        posts.add(child.getValue(Post.class));
                    }
                }
                if (!posts.isEmpty()) {
                    oldestId = String.valueOf(posts.get(posts.size() - 1).getId());
                }
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
