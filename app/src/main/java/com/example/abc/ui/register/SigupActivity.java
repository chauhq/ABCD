package com.example.abc.ui.register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.abc.HomeActivity;
import com.example.abc.R;
import com.example.abc.model.User;
import com.example.abc.ui.profile.SharePrefUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SigupActivity extends AppCompatActivity {
    EditText edtUserName;
    EditText edtNumberPhone;
    EditText edtPassword;
    EditText edtConfirmPassword;
    Button btnSignup;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        database = FirebaseDatabase.getInstance();
        edtNumberPhone = findViewById(R.id.edtNumberPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtUserName = findViewById(R.id.edtUserName);
        btnSignup = findViewById(R.id.btnSignUp);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCanceledOnTouchOutside(false);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtNumberPhone.getText().toString().isEmpty() || edtPassword.getText().toString().isEmpty() || edtConfirmPassword.getText().toString().isEmpty() || edtUserName.getText().toString().isEmpty()) {
                    Toast.makeText(SigupActivity.this, "Please fill all information", Toast.LENGTH_LONG).show();
                    return;
                }
                progressDialog.show();
                database.getReference("users").child(edtNumberPhone.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            progressDialog.dismiss();
                            Toast.makeText(SigupActivity.this, "phone number is exists", Toast.LENGTH_LONG).show();
                        } else {
                            final User user = new User(edtNumberPhone.getText().toString(), edtUserName.getText().toString(), edtPassword.getText().toString());
                            DatabaseReference myRef = database.getReference("users").child(edtNumberPhone.getText().toString());
                            myRef.setValue(user, new DatabaseReference.CompletionListener() {

                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    progressDialog.dismiss();
                                    if (databaseError == null) {
                                        SharePrefUtil.setUser(getApplicationContext(), user);
                                        Intent intent = new Intent(SigupActivity.this, HomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(SigupActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(SigupActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
