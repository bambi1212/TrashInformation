package com.example.trashinformation;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ScoreBoard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_score_board);

        SortAndDisplayUsers();

    }

    private void SortAndDisplayUsers() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // or GridLayoutManager, StaggeredGridLayoutManager
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Log.d("xxxxxx", "ARRIVE");

        final ArrayList<User> users = new ArrayList<>();
        final UserAdapter adapter = new UserAdapter(users);
        recyclerView.setAdapter(adapter);
        Query myQuery = database.getReference("users").orderByChild("score");//.equalTo(0);//.limitToLast(5);

        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot Usersnapshot : snapshot.getChildren()) {
                    User current = Usersnapshot.getValue(User.class);
                    users.add(0, current);
                    //if(current!=null)
                    //Log.d("xxxxxx", "User Name: " + current.getName() + ", User Score: " + current.getScore());

                }
                // Create an adapter and set it to the RecyclerView
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScoreBoard.this, "Error reading", Toast.LENGTH_SHORT).show();
            }
        });
    }
}