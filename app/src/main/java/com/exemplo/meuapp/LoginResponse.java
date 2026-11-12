package com.exemplo.meuapp;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    public String message;
    public String token;
    public User user;

    public static class User {
        @SerializedName("user_id")
        public int userId;
        public String name;
        public String email;
        public String tipo;
    }
}
