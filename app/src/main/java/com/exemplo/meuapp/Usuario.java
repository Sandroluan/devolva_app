package com.exemplo.meuapp;

import com.google.gson.annotations.SerializedName;

public class Usuario {

    @SerializedName("user_id")
    public int userId;

    public String name;
    public String email;
    public String tipo;
}
