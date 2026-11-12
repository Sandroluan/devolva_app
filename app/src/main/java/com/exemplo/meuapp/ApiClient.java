package com.exemplo.meuapp;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Emulador do Android Studio: 10.0.2.2 aponta para o localhost do seu PC.
    // Celular real: troque pelo IP do PC na rede, ex.: "http://192.168.18.5:3000/"
    public static final String BASE_URL = "http://192.168.18.92:3000/";

    private static ApiService service;

    public static ApiService get(Context context) {
        if (service == null) {
            final Context app = context.getApplicationContext();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    // criar livro faz a API consultar o Google Books (pode demorar)
                    .readTimeout(40, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();

                        Request.Builder builder = original.newBuilder();
                        String token = Session.token(app);
                        if (token != null) {
                            builder.header("Authorization", "Bearer " + token);
                        }

                        Response response = chain.proceed(builder.build());

                        // 401 = token vencido/inválido -> volta para o login.
                        // (o próprio login devolve 401 quando a senha está errada: não conta)
                        boolean ehLogin = original.url().encodedPath().endsWith("/usuarios/login");
                        if (response.code() == 401 && !ehLogin) {
                            Session.expire(app);
                        }
                        return response;
                    })
                    .build();

            service = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService.class);
        }
        return service;
    }
}
