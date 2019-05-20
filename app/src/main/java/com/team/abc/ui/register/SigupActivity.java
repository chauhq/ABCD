package com.team.abc.ui.register;

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

import com.team.abc.HomeActivity;
import com.team.abc.R;
import com.team.abc.model.User;
import com.team.abc.ui.profile.SharePrefUtil;
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
                if (edtNumberPhone.getText().toString().isEmpty()  ) {
                    Toast.makeText(SigupActivity.this, "Please fill all information", Toast.LENGTH_LONG).show();
                    return;
                }

                User user = new User();
                user.setMyPhone(edtNumberPhone.getText().toString());
                user.setPassword(edtPassword.getText().toString());
                String phone = edtNumberPhone.getText().toString();
                String mPhone = "+84" + phone.substring(1);
                Intent intent = new Intent(getApplicationContext(),VerifyAccount.class);
                intent.putExtra("phone",mPhone);

                intent.putExtra(User.class.getSimpleName(), user);
                startActivity(intent);
            }
        });
    }
}
