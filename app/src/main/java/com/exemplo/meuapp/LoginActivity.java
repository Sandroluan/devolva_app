package com.exemplo.meuapp;

import android.content.Intent;
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

        // já logado? vai direto para a tela certa (admin ou cliente)
        if (Session.isLogged(this)) {
            abrirTelaInicial();
            return;
        }

        setContentView(R.layout.activity_login);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> fazerLogin());

        if (getIntent().getBooleanExtra("expirou", false)) {
            Toast.makeText(this, "Sessão expirada. Entre novamente.", Toast.LENGTH_LONG).show();
        }
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

                LoginResponse body = response.body();
                if (response.isSuccessful() && body != null && body.token != null && body.user != null) {
                    Session.save(LoginActivity.this, body);
                    abrirTelaInicial();
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

    // tipo "admin" -> tela do administrador; qualquer outro -> tela do cliente
    private void abrirTelaInicial() {
        Class<?> destino = Session.isAdmin(this) ? AdminMainActivity.class : ClientMainActivity.class;
        startActivity(new Intent(this, destino));
        finish();
    }
}
