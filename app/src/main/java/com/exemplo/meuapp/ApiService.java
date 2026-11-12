package com.exemplo.meuapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ---------- usuários ----------
    @POST("usuarios/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @GET("usuarios")
    Call<List<Usuario>> listarUsuarios(@Query("page") int page); // só admin

    // ---------- livros ----------
    @GET("livros")
    Call<List<Livro>> listarLivros(@Query("page") int page);

    @POST("livros")
    Call<Void> criarLivro(@Body LivroRequest body); // só admin

    @PATCH("livros/{id}")
    Call<Void> atualizarLivro(@Path("id") int livroId, @Body LivroRequest body); // só admin

    @DELETE("livros/{id}")
    Call<Void> excluirLivro(@Path("id") int livroId); // só admin

    // ---------- empréstimos ----------
    @GET("emprestimos")
    Call<List<Emprestimo>> listarEmprestimos(@Query("page") int page);

    @POST("emprestimos")
    Call<Void> criarEmprestimo(@Body NovoEmprestimoRequest body);

    @PATCH("emprestimos/{id}")
    Call<Void> devolver(@Path("id") int emprestimoId); // só admin
}
