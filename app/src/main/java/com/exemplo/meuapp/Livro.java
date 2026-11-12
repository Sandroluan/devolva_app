package com.exemplo.meuapp;

import com.google.gson.annotations.SerializedName;

public class Livro {

    @SerializedName("livro_id")
    public int livroId;

    public String titulo;
    public String autor;
    public String descricao;

    @SerializedName("capa_url")
    public String capaUrl;
}
