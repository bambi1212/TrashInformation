package com.example.trashinformation;

public class User {
    private String name;
    private int score;

    public User(String name){
        this.name = name;
        this.score = 0;
    }

    public User(String name , int score){
        this.name = name;
        this.score = score;

    }

    public User(){
    }

    public int getScore(){  // Changed the return type to int
        return score;      // Corrected to return the score field
    }

    public void setScore(int score){  // Changed the parameter name to score
        this.score = score;           // Corrected to set the score field
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
    public void incrementScore() {
        score++;
    }
}