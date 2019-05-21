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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.team.abc.CreatePostActivity;
import com.team.abc.EndLessScrollListener;
import com.team.abc.R;
import com.team.abc.model.GeoResponse;
import com.team.abc.model.State;
import com.team.abc.service.ABCApi;
import com.team.abc.ui.profile.SharePrefUtil;
import com.team.abc.model.Post;
import com.team.abc.model.User;
import com.team.abc.ui.register.LoginActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostFragment extends Fragment {
    private static final String IS_ACC_VIP = "is_acc_vip";
    private RecyclerView recyclerView;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    private List<Post> posts = new ArrayList<>();
    private FloatingActionButton fabPost;
    private User user;
    private String oldestId = "0";
    private List<String> states = new ArrayList<>();
    private String state = "";
    private EndLessScrollListener endLessScrollListener;
    String nodeName;
    private RelativeLayout llSpinner;
    private TextView tvTitle;
    private TextView tvSearch;
    private ImageView imgDelete;

    public static PostFragment newInstance(boolean isAdmin, boolean check) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_ACC_VIP, isAdmin);
        bundle.putBoolean("check", check);
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
        llSpinner = view.findViewById(R.id.llSpinner);
        tvTitle = view.findViewById(R.id.tvTitle);
        database = FirebaseDatabase.getInstance();
        tvSearch = view.findViewById(R.id.tvSearch);
        imgDelete = view.findViewById(R.id.imgDelete);


        if (getArguments().getBoolean(IS_ACC_VIP)) {
            nodeName = "suggestion";
            tvSearch.setText("");
            tvTitle.setText("Post Suggestion");
            llSpinner.setVisibility(View.GONE);
            fabPost.hide();
        } else {
            nodeName = "posts";
        }
        user = SharePrefUtil.getUserLogged(getActivity());
        initListener();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new PostAdapter(posts, getActivity(), getArguments().getBoolean("check")));

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

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.placeAutocompleteFragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS));
        autocompleteFragment.setOnPlaceSelectedListener(new com.google.android.libraries.places.widget.listener.PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                if (place.getLatLng() == null) {
                    return;
                }

                ABCApi.getInstance().googleService().getGeoResponse(String.valueOf(place.getLatLng().latitude) + "," + String.valueOf(place.getLatLng().longitude), false, getString(R.string.ma_api)).enqueue(new Callback<GeoResponse>() {
                    @Override
                    public void onResponse(Call<GeoResponse> call, Response<GeoResponse> response) {
                        List<GeoResponse.MyPlace> places = response.body().getResults();
                        for (GeoResponse.MyPlace place : places) {
                            for (GeoResponse.AddressComponents addressComponent : place.getAddress_components()) {
                                for (String type : addressComponent.getTypes()) {
                                    if (type.equals("administrative_area_level_1")) {
                                        posts.clear();
                                        state = addressComponent.getShort_name().toLowerCase();
                                        tvSearch.setText(state);
                                        progressDialog.show();
                                        queryPostByKey();
                                        return;
                                    }
                                }

                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GeoResponse> call, Throwable t) {
                        Log.d("xxx", "onFailure: ");

                    }
                });
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }


    private void initListener() {
        fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharePrefUtil.getUserLogged(getActivity()) == null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), CreatePostActivity.class);
                    startActivity(intent);
                }
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

        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvSearch.setText("All");
                posts.clear();
                progressDialog.show();
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
