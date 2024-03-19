package com.example.trashinformation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    protected FirebaseAuth mAuth; //he is protected so i can use in register


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        mAuth= FirebaseAuth.getInstance();

    }

    public void registerAccount(View v) {
        EditText emailText = findViewById(R.id.edittext_email_login);
        EditText passwordText = findViewById(R.id. edittext_password_reg);

        mAuth.createUserWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { //if register work move to welcome
                            //notificationStart(); //ty for saving Eart notification
                            startActivity(new Intent(RegisterActivity.this, MlActivity.class));
                        } else
                        {
                            Toast.makeText(RegisterActivity.this, "register failed",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void moveToLoginActivity(View v){
        startActivity(new Intent(this, LoginActivity.class));


    }
    }
