package com.exemplo.meuapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("usuarios/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @GET("emprestimos")
    Call<List<Emprestimo>> listarEmprestimos(@Query("page") int page);

    @POST("emprestimos")
    Call<Void> criarEmprestimo(@Body NovoEmprestimoRequest body);

    @PATCH("emprestimos/{id}")
    Call<Void> devolver(@Path("id") int emprestimoId);
}
