package com.example.model;

public enum Permission {
    READ("read"),
    UPDATE("update"),
    WRITE("write");


    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
