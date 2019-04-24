package com.example.abc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.abc.model.DirectionApiResponse;
import com.example.abc.model.Post;
import com.example.abc.model.User;
import com.example.abc.service.ABCApi;
import com.example.abc.ui.profile.ProfileFragment;
import com.example.abc.ui.profile.SharePrefUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
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

public class MainFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1000;
    private static int REQUEST_POST_SCREEN = 2000;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageView imgLand;
    private TextView tvDes;
    private LatLng myLatlng;
    private LatLng destinationLatlng;
    private TextView tvDirect;
    private TextView tvLogin;
    private TextView tvPost;
    private TextView tvUserName;
    private ArrayList<LatLng> paths = new ArrayList<>();
    private Polyline polyline;
    private DrawerLayout drawerLayout;
    List<Post> posts = new ArrayList<>();
    FirebaseDatabase database;
    private GPSUtil gpsUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Places.initialize(getActivity(), getString(R.string.ma_api));
        database = FirebaseDatabase.getInstance();

        imgLand = view.findViewById(R.id.imgLand);
        tvDes = view.findViewById(R.id.tvDes);
        tvDirect = view.findViewById(R.id.tvDirect);
        drawerLayout = view.findViewById(R.id.drawerLayout);
        tvLogin = view.findViewById(R.id.tvLogin);
        tvPost = view.findViewById(R.id.tvPost);
        tvUserName = view.findViewById(R.id.tvUserName);

        gpsUtil = new GPSUtil(getActivity());
        gpsUtil.setOnListener(new GPSUtil.TurnOnGPS() {
            @Override
            public void onChangeLocation(Location location) {
                Log.d("xxxx", "onChangeLocation: " + location.toString());
                myLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatlng, 15F));
            }
        });

        User user = SharePrefUtil.getUserLogged(getActivity());
        if (user == null) {
            tvLogin.setText("Login");
            tvUserName.setText("No User");
            tvPost.setVisibility(View.GONE);
        } else {
            tvLogin.setText("Logout");
            tvUserName.setText(user.getName());
            tvPost.setVisibility(View.VISIBLE);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        LinearLayout llBottomSheet = view.findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.placeAutocompleteFragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new com.google.android.libraries.places.widget.listener.PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.d("xxx", "onError: " + status.getStatusMessage());

            }
        });
        listenerEvent();

        database.getReference("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        posts.add(child.getValue(Post.class));
                    }

                    for (int i = 0; i < posts.size(); i++) {
                        map.addMarker(new MarkerOptions().position(new LatLng(posts.get(i).getLat(), posts.get(i).getLng()))).setTag(i);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void initDataFOrBottomSheet(Post post) {
        tvDes.setText(post.getDes());
        int imgSize = getResources().getDimensionPixelSize(R.dimen.img_size);
        Glide.with(this).load(post.getUrl()).apply(new RequestOptions().override(imgSize, imgSize).centerCrop()).into(imgLand);
    }

    private void listenerEvent() {
        tvDirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paths.clear();
                if (polyline != null) {
                    polyline.remove();
                }
                ABCApi.getInstance()
                        .googleService()
                        .getDirectionResponse(myLatlng.latitude + "," + myLatlng.longitude, destinationLatlng.latitude + "," + destinationLatlng.longitude, getString(R.string.ma_api))
                        .enqueue(new Callback<DirectionApiResponse>() {
                            @Override
                            public void onResponse(Call<DirectionApiResponse> call, Response<DirectionApiResponse> response) {
                                DirectionApiResponse res = response.body();
                                DirectionApiResponse.Routes route = res.getRoutes().get(0);
                                if (route.getLegs() != null) {
                                    for (int i = 0; i < route.getLegs().size(); i++) {
                                        DirectionApiResponse.Routes.Legs leg = route.getLegs().get(i);
                                        if (leg.getSteps() != null) {
                                            for (int j = 0; j < leg.getSteps().size(); j++) {
                                                DirectionApiResponse.Routes.Legs.Steps step = leg.getSteps().get(j);
                                                if (step.getPolyline() != null) {
                                                    paths.addAll(decodePoly(step.getPolyline().getPoints()));
                                                }
                                            }
                                        }
                                    }
                                }
                                PolylineOptions opts = new PolylineOptions().addAll(paths).color(Color.BLUE).width(5);
                                polyline = map.addPolyline(opts);
                                map.getUiSettings().setZoomControlsEnabled(true);

                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatlng, 15));
                            }

                            @Override
                            public void onFailure(Call<DirectionApiResponse> call, Throwable t) {
                                Log.d("xxx", "onFailure: ");
                            }
                        });
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharePrefUtil.getUserLogged(getActivity()) != null) {
                    SharePrefUtil.clearUser(getActivity());
                    tvLogin.setText("Login");
                    tvUserName.setText("No User");
                    tvPost.setVisibility(View.GONE);
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(getActivity(), PostActivity.class);
                startActivityForResult(intent, REQUEST_POST_SCREEN);
            }
        });

        tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(getActivity(), ProfileFragment.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 1: World
     * 5: Landmass/continent
     * 10: City
     * 15: Streets
     * 20: Buildings
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("xxx", "onMapReady: ");
            gpsUtil.getCurrentLocation();
        } else {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int position = (int) (marker.getTag());
        initDataFOrBottomSheet(posts.get(position));
        destinationLatlng = new LatLng(posts.get(position).getLat(), posts.get(position).getLng());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        return false;
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            gpsUtil.getCurrentLocation();
        }
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_POST_SCREEN && resultCode == Activity.RESULT_OK) {
            Post post = data.getParcelableExtra(Post.class.getSimpleName());
            Log.d("xxx", "onActivityResult: " + post.getLat() + ":" + post.getLng());
            posts.add(post);
            map.addMarker(new MarkerOptions().position(new LatLng(post.getLat(), post.getLng()))).setTag(posts.indexOf(post));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(post.getLat(), post.getLng()), 15F));
            initDataFOrBottomSheet(post);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }*/
}
