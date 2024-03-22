package com.example.trashinformation;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    protected FirebaseAuth mAuth; //he is protected so i can use in register

    //ActivityReadDataBinding a;

    EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); //for noti maybe need to check
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        name = findViewById (R.id.choseNickName_editText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    public void registerAccount(View v) {
        EditText emailText = findViewById(R.id.edittext_email_login);
        EditText passwordText = findViewById(R.id. edittext_password_reg);


        if(name.getText().toString().isEmpty()) {
            Toast.makeText(this, "failed to register need to enter name", Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.createUserWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) { //if register work move to welcome
                                notificationStart(); //ty for saving Eart notification
                                setUser();
                                startActivity(new Intent(RegisterActivity.this, MlActivity.class));
                            } else {
                                Toast.makeText(RegisterActivity.this, "register failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public void moveToLoginActivity(View v){
        startActivity(new Intent(this, LoginActivity.class));


    }
    public void notificationStart(){
        long futureTimeMillis = System.currentTimeMillis() + 10000; // 10 seconds from now

        NotificationCompat.Builder mbuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(getApplicationContext(),"dawg")
                        .setSmallIcon(R.drawable.baseline_notifications_none_24)
                        .setContentTitle("Notification")
                        .setContentText("Ty for saving Earth")
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setWhen(futureTimeMillis);
        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // move data to noti activity


        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0
                , intent, PendingIntent.FLAG_MUTABLE);
        mbuilder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel =
                    notificationManager.getNotificationChannel("dawg");
            if(notificationChannel ==null){
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel("dawg",
                        "some descripsion idc", importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);

            }

        }
        notificationManager.notify(0,mbuilder.build());
    }

    public void setUser(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef= database.getReference("user"+mAuth.getInstance().getUid());
        User user =new User(name.getText().toString());
            //myRef.setValue(name.getText().toString());
        myRef.setValue(user);


        // Write a message to the database
         database = FirebaseDatabase.getInstance();
         myRef = database.getReference("name");

        myRef.setValue(name.getText().toString());

        myRef = database.getReference("score");

        myRef.setValue(0);



    }
    }
