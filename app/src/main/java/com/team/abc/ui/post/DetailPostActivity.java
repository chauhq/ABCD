package com.team.abc.ui.post;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.team.abc.R;
import com.team.abc.model.Post;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class DetailPostActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String POST_ID = "post_id";
    private ImageView imageView;
    private TextView tvDes;
    private ImageView imgPhone;
    private TextView tvAddress;
    private TextView tvCreate;
    private TextView tvPrice;
    private Post post;
    private MapView mapView;


    public static void startDetailPostActivity(Context context, String id) {
        Intent intent = new Intent(context, DetailPostActivity.class);
        intent.putExtra(POST_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_post);

        imageView = findViewById(R.id.imgLand);
        tvDes = findViewById(R.id.tvDes);
        imgPhone = findViewById(R.id.tvPhoneNumber);
        tvAddress = findViewById(R.id.tvAddress);
        tvCreate = findViewById(R.id.tvCreate);
        tvPrice = findViewById(R.id.tvPrice);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        database.getReference("posts").child(getIntent().getStringExtra(POST_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    new AlertDialog.Builder(DetailPostActivity.this).setMessage("The post deleted").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show().setCanceledOnTouchOutside(false);
                } else {
                    post = dataSnapshot.getValue(Post.class);
                    Glide.with(DetailPostActivity.this).load(post.getUrl()).centerCrop().into(imageView);
                    tvDes.setText(post.getDes());
                    tvAddress.setText("State: " + post.getState());
                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    tvPrice.setText("Price: " + formatter.format(post.getPrice()) + "vnđ");
                    tvCreate.setText(post.getCreate());
                    imgPhone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + post.getPhoneNumber()));
                            startActivity(intent);
                        }
                    });
                    mapView.getMapAsync(DetailPostActivity.this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView.onResume();
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (post != null) {
            googleMap.addMarker(new MarkerOptions().position(new LatLng(post.getLat(), post.getLng())));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(post.getLat(), post.getLng()), 15F));
        }
    }
}
