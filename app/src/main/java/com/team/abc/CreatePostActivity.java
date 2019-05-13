package com.team.abc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.team.abc.model.GeoResponse;
import com.team.abc.model.Post;
import com.team.abc.model.State;
import com.team.abc.model.User;
import com.team.abc.service.ABCApi;
import com.team.abc.ui.profile.SharePrefUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int GALLEY_RESULT_CODE = 1993;
    RelativeLayout rlPick;
    ImageView imageView;
    Button btnPost;
    FirebaseDatabase database;
    MapView mapView;
    GoogleMap map;
    FirebaseStorage storage;
    StorageReference storageRef;
    private Uri uri;
    EditText edtPhoneNumber;
    EditText edtDes;
    EditText edtPrice;
    LatLng latLng;
    String state = "Noname";
    ProgressDialog progressDialog;
    Post post;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        storageRef = storage.getReference();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        imageView = findViewById(R.id.imgLand);
        rlPick = findViewById(R.id.rlPick);
        btnPost = findViewById(R.id.btnPost);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtDes = findViewById(R.id.edtDes);
        edtPrice = findViewById(R.id.edtPrice);
        post = getIntent().getParcelableExtra(Post.class.getSimpleName());

        if (post != null) {
            edtPrice.setText(post.getPrice().toString());
            edtPhoneNumber.setText(post.getPhoneNumber());
            edtDes.setText(post.getDes());
            edtPrice.setText(String.valueOf(post.getPrice()));
            int width = getResources().getDimensionPixelOffset(R.dimen.img_size_big);
            imageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(post.getUrl())
                    .apply(new RequestOptions().override(width).centerCrop())
                    .into(imageView);
        }


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCanceledOnTouchOutside(false);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteFragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS));
        autocompleteFragment.setOnPlaceSelectedListener(new com.google.android.libraries.places.widget.listener.PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                if (place.getLatLng() == null) {
                    return;
                }

                ABCApi.getInstance()
                        .googleService().getGeoResponse(String.valueOf(place.getLatLng().latitude) + "," + String.valueOf(place.getLatLng().longitude), false, getString(R.string.ma_api)).enqueue(new Callback<GeoResponse>() {
                    @Override
                    public void onResponse(Call<GeoResponse> call, Response<GeoResponse> response) {
                        List<GeoResponse.MyPlace> places = response.body().getResults();
                        for (GeoResponse.MyPlace place : places) {
                            for (GeoResponse.AddressComponents addressComponent : place.getAddress_components()) {
                                for (String type : addressComponent.getTypes()) {
                                    if (type.equals("administrative_area_level_1")) {
                                        state = addressComponent.getShort_name().toLowerCase();
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

                latLng = place.getLatLng();
                map.addMarker(new MarkerOptions().position(place.getLatLng()));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15F));
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        listenerEvent();
    }

    private void listenerEvent() {
        rlPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLEY_RESULT_CODE);
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final User user = SharePrefUtil.getUserLogged(CreatePostActivity.this);
                final String nodeName;
                if (user.isAccVip()) {
                    nodeName = "suggestion";
                } else {
                    nodeName = "posts";
                }
                if (post == null) {
                    if (latLng == null || edtPhoneNumber.getText().toString().isEmpty() || edtDes.getText().toString().isEmpty() || edtPrice.getText().toString().isEmpty() || uri == null) {
                        Toast.makeText(CreatePostActivity.this, "Please fill all information", Toast.LENGTH_LONG).show();
                        return;
                    }
                    progressDialog.show();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bitmap == null) {
                        return;
                    }
                    final StorageReference imageRef = storageRef.child("posts").child("images/" + SystemClock.currentThreadTimeMillis() + ".jpg");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    UploadTask uploadTask = imageRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(CreatePostActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                    calendar.getTime();
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                                    long timeStamp = calendar.getTimeInMillis();
                                    Long a = new Long(timeStamp);
                                    final Post post = new Post(-1 * a.intValue(), edtPhoneNumber.getText().toString(), edtDes.getText().toString(), task.getResult().toString(), latLng.latitude, latLng.longitude, state, formatter.format(calendar.getTime()), Double.valueOf(edtPrice.getText().toString()), user.getMyPhone());
                                    DatabaseReference ref = database.getReference(nodeName).child(String.valueOf(post.getId()));
                                    ref.setValue(post, new DatabaseReference.CompletionListener() {

                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            progressDialog.dismiss();
                                            if (databaseError == null) {
                                                database.getReference("city").child(state).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (!dataSnapshot.exists()) {
                                                            database.getReference("city").child(state).setValue(new State((state))).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                    intent.putExtra("index", (user.isAccVip()) ? 2 : 1);
                                                                    startActivity(intent);
                                                                }
                                                            });
                                                        } else {
                                                            Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            intent.putExtra("index", (user.isAccVip()) ? 2 : 1);
                                                            startActivity(intent);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(CreatePostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(CreatePostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            });

                        }
                    });
                } else {
                    final Post postStamp = new Post(post.getId(), edtPhoneNumber.getText().toString(), edtDes.getText().toString(), post.getUrl(), latLng.latitude, latLng.longitude, state, post.getCreate(), Double.valueOf(edtPrice.getText().toString()), post.getUserId());
                    if (postStamp.getUrl().substring(0, 4).equals("http")) {
                        DatabaseReference ref = database.getReference(nodeName).child(String.valueOf(postStamp.getId()));
                        ref.setValue(postStamp, new DatabaseReference.CompletionListener() {

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                progressDialog.dismiss();
                                if (databaseError == null) {
                                    Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("index", 1);
                                    startActivity(intent);
                                    database.getReference("city").child(state).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.exists()) {
                                                database.getReference("city").child(state).setValue(new State((state)));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            progressDialog.dismiss();
                                            Toast.makeText(CreatePostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();

                                        }
                                    });
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(CreatePostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (bitmap == null) {
                            return;
                        }
                        final StorageReference imageRef = storageRef.child("posts").child("images/" + SystemClock.currentThreadTimeMillis() + ".jpg");

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();
                        UploadTask uploadTask = imageRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Toast.makeText(CreatePostActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        postStamp.setUrl(task.getResult().toString());
                                        DatabaseReference ref = database.getReference(nodeName).child(String.valueOf(postStamp.getId()));
                                        ref.setValue(postStamp, new DatabaseReference.CompletionListener() {

                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                progressDialog.dismiss();
                                                if (databaseError == null) {
                                                    Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    intent.putExtra("index", (user.isAccVip()) ? 2 : 1);
                                                    startActivity(intent);
                                                    database.getReference("city").child(state).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (!dataSnapshot.exists()) {
                                                                database.getReference("city").child(state).setValue(new State((state)));
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(CreatePostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();

                                                        }
                                                    });
                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(CreatePostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                });

                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mapView.onResume();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        if (post != null) {
            state = post.getState();
            latLng = new LatLng(post.getLat(), post.getLng());
            map.addMarker(new MarkerOptions().position(new LatLng(post.getLat(), post.getLng())));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(post.getLat(), post.getLng()), 15F));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLEY_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            int width = getResources().getDimensionPixelOffset(R.dimen.img_size_big);
            imageView.setVisibility(View.VISIBLE);
            uri = data.getData();
            if (post != null) {
                post.setUrl(uri.toString());
            }

            Glide.with(this)
                    .load(data.getData())
                    .apply(new RequestOptions().override(width).centerCrop())
                    .into(imageView);
        }
    }
}
