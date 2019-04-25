package com.example.abc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.abc.model.DirectionApiResponse;
import com.example.abc.model.Post;
import com.example.abc.model.User;
import com.example.abc.service.ABCApi;
import com.example.abc.ui.profile.ProfileFragment;
import com.example.abc.ui.profile.SharePrefUtil;
import com.example.abc.ui.register.LoginActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.yayandroid.locationmanager.LocationManager;
import com.yayandroid.locationmanager.configuration.DefaultProviderConfiguration;
import com.yayandroid.locationmanager.configuration.GooglePlayServicesConfiguration;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.configuration.PermissionConfiguration;
import com.yayandroid.locationmanager.constants.ProviderType;
import com.yayandroid.locationmanager.listener.LocationListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1000;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageView imgLand;
    private TextView tvDes;
    private LatLng myLatlng;
    private LatLng destinationLatlng;
    private TextView tvDirect;
    private ArrayList<LatLng> paths = new ArrayList<>();
    private Polyline polyline;
    List<Post> posts = new ArrayList<>();
    FirebaseDatabase database;
    LocationManager awesomeLocationManager;
    private ProgressBar progressBar;
    private ImageView imgPhone;
    Post post;

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
        imgPhone = view.findViewById(R.id.imgPhone);
        progressBar = view.findViewById(R.id.progressBar);
        User user = SharePrefUtil.getUserLogged(getActivity());

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
                Toast.makeText(getActivity(), status.getStatusMessage(), Toast.LENGTH_LONG).show();

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
                        map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_post)).position(new LatLng(posts.get(i).getLat(), posts.get(i).getLng()))).setTag(i);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        LocationConfiguration awesomeConfiguration = new LocationConfiguration.Builder()
                .keepTracking(false)
                .useDefaultProviders(new DefaultProviderConfiguration.Builder()
                        .requiredTimeInterval(5 * 60 * 1000)
                        .requiredDistanceInterval(0)
                        .acceptableAccuracy(5.0f)
                        .acceptableTimePeriod(5 * 60 * 1000)
                        .setWaitPeriod(ProviderType.GPS, 20 * 1000)
                        .setWaitPeriod(ProviderType.NETWORK, 20 * 1000)
                        .build())
                .build();
        awesomeLocationManager = new LocationManager.Builder(getActivity().getApplicationContext())
                .activity(getActivity()) // Only required to ask permission and/or GoogleApi - SettingsApi
                .configuration(awesomeConfiguration)
                .notify(new LocationListener() {
                    @Override
                    public void onProcessTypeChanged(int processType) {

                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d("xxx", "onLocationChanged: ");
                        Log.d("xxx", "onLocationChanged: " + location);
                        myLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                        map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maker_user)).position(myLatlng)).setTag(-100);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatlng, 15F));
                    }

                    @Override
                    public void onLocationFailed(int type) {

                    }

                    @Override
                    public void onPermissionGranted(boolean alreadyHadPermission) {

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                })
                .build();

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
                if (myLatlng == null) {
                    Toast.makeText(getActivity(), "Not found your location", Toast.LENGTH_LONG).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
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
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(Call<DirectionApiResponse> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        imgPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + post.getPhoneNumber()));
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
            awesomeLocationManager.get();
        } else {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int position = (int) (marker.getTag());
        if (position == -100) {
            return false;
        }
        post = posts.get(position);
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
            awesomeLocationManager.get();
        }
    }

}
