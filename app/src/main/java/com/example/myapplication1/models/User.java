package com.example.myapplication1.models;

public class User {
    private String uid;
    private String email;
    private String name;
    private String role; // "student", "admin"
    private String studentId;
    private long createdAt;

    public User() {}

    public User(String uid, String email, String name, String role, String studentId) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
        this.studentId = studentId;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public boolean isStudent() {
        return "student".equals(role);
    }
}