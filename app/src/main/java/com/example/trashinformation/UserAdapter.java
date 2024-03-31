package com.example.trashinformation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private ArrayList<User> users;

    public UserAdapter(ArrayList<User> users) {
        this.users = users;
    }


    @NonNull //  parameters and the returned value cannot be null.
    @Override // Indicates that the method is being overridden from a superclass.
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the layout for each item in the RecyclerView.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        // Returns a new instance of UserViewHolder with the inflated view.
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
        Log.d("YYYYYY", users.get(position).getName());
        holder.userNameTextView.setText(users.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameTextView;
        private TextView userScoreTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            userScoreTextView = itemView.findViewById(R.id.userScoreTextView);
        }

        public void bind(User user) {
            userNameTextView.setText(user.getName());
            userScoreTextView.setText(String.valueOf(user.getScore()));
        }
    }
}