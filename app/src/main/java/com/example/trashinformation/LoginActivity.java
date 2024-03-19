package com.example.trashinformation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {
    protected FirebaseAuth mAuth; //he is protected so i can use in register

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

    }

    public void onStart(){ //if register no need o login again
        super.onStart();
        //check if user sign in need also to check if he pressed remember me
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null)
            startActivity(new Intent(LoginActivity.this, MlActivity.class));

    }

    public void moveToRegisterActivity(View v){
        startActivity(new Intent(this, RegisterActivity.class));

    }
    public void login(View v) {
        EditText emailText = findViewById(R.id. edittext_email_login);
        EditText passwordText = findViewById(R.id. edittext_password_login);

        // Authenticate user with Firebase Authentication
        mAuth.signInWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {//sign in succses, update UI with the signed in user information
                            startActivity(new Intent(LoginActivity.this, MlActivity.class));
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "login failed",Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
    }
