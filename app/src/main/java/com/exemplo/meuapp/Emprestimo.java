package com.exemplo.meuapp;

import com.google.gson.annotations.SerializedName;

public class Emprestimo {

    @SerializedName("emprestimo_id")
    public int emprestimoId;

    @SerializedName("livro_id")
    public int livroId;

    @SerializedName("nome_usuario")
    public String nomeUsuario;

    @SerializedName("titulo_livro")
    public String tituloLivro;

    public String autor;
    public String descricao;

    @SerializedName("capa_url")
    public String capaUrl;

    @SerializedName("criado_em")
    public String criadoEm;

    public String prazo;
    public String status;

    @SerializedName("dias_de_atraso")
    public int diasDeAtraso;

    @SerializedName("valor_multa")
    public double valorMulta;

    public boolean estaEntregue() {
        return "entregue".equalsIgnoreCase(status);
    }
}
