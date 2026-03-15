package com.example.soen345;

public class User {
    private String uid;
    private String email;
    private String phoneNumber;
    private boolean isAdmin; // New field for role

    public User() {} // Required for Firestore

    public User(String uid, String email, String phoneNumber, boolean isAdmin) {
        this.uid = uid;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.isAdmin = isAdmin;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(boolean admin) { isAdmin = admin; }
}