package com.exemplo.meuapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Tela cheia do livro: capa grande, título, autor e descrição. */
public class BookDetailActivity extends AppCompatActivity {

    private static final String EXTRA_ID = "livro_id";
    private static final String EXTRA_TITULO = "titulo";
    private static final String EXTRA_AUTOR = "autor";
    private static final String EXTRA_DESCRICAO = "descricao";
    private static final String EXTRA_CAPA = "capa_url";
    private static final String EXTRA_PODE_EMPRESTAR = "pode_emprestar";

    public static void start(Context ctx, int livroId, String titulo, String autor,
                             String descricao, String capaUrl, boolean podeEmprestar) {
        Intent i = new Intent(ctx, BookDetailActivity.class);
        i.putExtra(EXTRA_ID, livroId);
        i.putExtra(EXTRA_TITULO, titulo);
        i.putExtra(EXTRA_AUTOR, autor);
        i.putExtra(EXTRA_DESCRICAO, descricao);
        i.putExtra(EXTRA_CAPA, capaUrl);
        i.putExtra(EXTRA_PODE_EMPRESTAR, podeEmprestar);
        ctx.startActivity(i);
    }

    private Button btnEmprestar;
    private int livroId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        Intent in = getIntent();
        livroId = in.getIntExtra(EXTRA_ID, 0);
        String descricao = in.getStringExtra(EXTRA_DESCRICAO);

        TextView txtVoltar = findViewById(R.id.txtVoltar);
        ImageView imgCapa = findViewById(R.id.imgCapa);
        TextView txtTitulo = findViewById(R.id.txtTitulo);
        TextView txtAutor = findViewById(R.id.txtAutor);
        TextView txtDescricao = findViewById(R.id.txtDescricao);
        btnEmprestar = findViewById(R.id.btnEmprestar);

        txtVoltar.setOnClickListener(v -> finish());

        txtTitulo.setText(in.getStringExtra(EXTRA_TITULO));
        txtAutor.setText(in.getStringExtra(EXTRA_AUTOR));
        txtDescricao.setText(descricao == null || descricao.trim().isEmpty()
                ? "Sem descrição disponível."
                : descricao);
        Capas.carregar(imgCapa, in.getStringExtra(EXTRA_CAPA));

        boolean podeEmprestar = in.getBooleanExtra(EXTRA_PODE_EMPRESTAR, false) && !Session.isAdmin(this);
        if (podeEmprestar) {
            btnEmprestar.setVisibility(android.view.View.VISIBLE);
            btnEmprestar.setOnClickListener(v -> emprestar());
        }
    }

    private void emprestar() {
        btnEmprestar.setEnabled(false);

        NovoEmprestimoRequest body = new NovoEmprestimoRequest(Session.userId(this), livroId);
        ApiClient.get(this).criarEmprestimo(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookDetailActivity.this,
                            "Livro emprestado! Prazo de 7 dias.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    btnEmprestar.setEnabled(true);
                    Toast.makeText(BookDetailActivity.this,
                            "Não foi possível pegar emprestado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnEmprestar.setEnabled(true);
                Toast.makeText(BookDetailActivity.this,
                        "Não foi possível conectar à API", Toast.LENGTH_LONG).show();
            }
        });
    }
}
