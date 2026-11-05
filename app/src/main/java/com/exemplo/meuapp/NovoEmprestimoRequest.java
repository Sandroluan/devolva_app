package com.exemplo.meuapp;

import com.google.gson.annotations.SerializedName;

public class NovoEmprestimoRequest {
    @SerializedName("user_id")
    public int userId;

    @SerializedName("livro_id")
    public int livroId;

    public NovoEmprestimoRequest(int userId, int livroId) {
        this.userId = userId;
        this.livroId = livroId;
    }
}
