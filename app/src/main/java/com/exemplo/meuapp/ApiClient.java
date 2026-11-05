package com.exemplo.meuapp;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Emulador do Android Studio: 10.0.2.2 aponta para o localhost do seu PC.
    // Celular real: troque pelo IP do PC na rede, ex.: "http://192.168.18.5:3000/"
    public static final String BASE_URL = "http://192.168.18.5:3000/";

    private static ApiService service;

    public static ApiService get(Context context) {
        if (service == null) {
            Context app = context.getApplicationContext();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        String token = app.getSharedPreferences("devolva", Context.MODE_PRIVATE)
                                .getString("token", null);
                        Request.Builder builder = chain.request().newBuilder();
                        if (token != null) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
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
