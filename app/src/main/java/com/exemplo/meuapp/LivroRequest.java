package com.exemplo.meuapp;

import com.google.gson.annotations.SerializedName;

// Campos null NÃO são enviados (o Gson ignora), então serve para POST e PATCH.
public class LivroRequest {
    public String titulo;
    public String autor;
    public String descricao;

    @SerializedName("capa_url")
    public String capaUrl;
}
