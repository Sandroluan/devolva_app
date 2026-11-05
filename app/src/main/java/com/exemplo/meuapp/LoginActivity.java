package com.exemplo.meuapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtSenha;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // já logado? vai direto para a lista
        SharedPreferences prefs = getSharedPreferences("devolva", MODE_PRIVATE);
        if (prefs.getString("token", null) != null) {
            abrirMain();
            return;
        }

        setContentView(R.layout.activity_login);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> fazerLogin());
    }

    private void fazerLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEntrar.setEnabled(false);

        ApiClient.get(this).login(new LoginRequest(email, senha)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnEntrar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    getSharedPreferences("devolva", MODE_PRIVATE).edit()
                            .putString("token", body.token)
                            .putInt("user_id", body.user.userId)
                            .apply();
                    abrirMain();
                } else {
                    Toast.makeText(LoginActivity.this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnEntrar.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Não foi possível conectar à API", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
