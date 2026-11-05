package com.exemplo.meuapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int POR_PAGINA = 10; // mesmo limit usado na API

    private TextView txtMultaTotal, txtAtrasados;
    private EmprestimoAdapter adapter;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtMultaTotal = findViewById(R.id.txtMultaTotal);
        txtAtrasados = findViewById(R.id.txtAtrasados);
        TextView txtSair = findViewById(R.id.txtSair);
        FloatingActionButton fab = findViewById(R.id.fab);
        RecyclerView recycler = findViewById(R.id.recycler);

        adapter = new EmprestimoAdapter(this::confirmarDevolucao);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        txtSair.setOnClickListener(v -> sair());
        fab.setOnClickListener(v -> mostrarDialogNovoEmprestimo());
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarEmprestimos();
    }

    // ---------- listar ----------

    private void carregarEmprestimos() {
        buscarPagina(1, new ArrayList<>());
    }

    // a API devolve 10 por página, então buscamos até a página vir incompleta
    private void buscarPagina(int pagina, List<Emprestimo> acumulado) {
        ApiClient.get(this).listarEmprestimos(pagina).enqueue(new Callback<List<Emprestimo>>() {
            @Override
            public void onResponse(Call<List<Emprestimo>> call, Response<List<Emprestimo>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(MainActivity.this, "Erro ao carregar empréstimos", Toast.LENGTH_SHORT).show();
                    return;
                }

                acumulado.addAll(response.body());

                if (response.body().size() == POR_PAGINA) {
                    buscarPagina(pagina + 1, acumulado);
                } else {
                    atualizarTela(acumulado);
                }
            }

            @Override
            public void onFailure(Call<List<Emprestimo>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Não foi possível conectar à API", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void atualizarTela(List<Emprestimo> lista) {
        double totalMulta = 0;
        int atrasados = 0;

        for (Emprestimo e : lista) {
            if (!e.estaEntregue() && e.diasDeAtraso > 0) {
                atrasados++;
                totalMulta += e.valorMulta;
            }
        }

        txtMultaTotal.setText(moeda.format(totalMulta));
        txtAtrasados.setText(atrasados + (atrasados == 1 ? " Livro\nAtrasado" : " Livros\nAtrasados"));
        adapter.setLista(lista);
    }

    // ---------- devolver ----------

    private void confirmarDevolucao(Emprestimo e) {
        if (e.estaEntregue()) {
            Toast.makeText(this, "Este livro já foi entregue", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Devolver livro")
                .setMessage("Marcar \"" + e.tituloLivro + "\" como entregue?")
                .setPositiveButton("Sim", (d, w) -> devolver(e))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void devolver(Emprestimo e) {
        ApiClient.get(this).devolver(e.emprestimoId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    carregarEmprestimos();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao devolver", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Não foi possível conectar à API", Toast.LENGTH_LONG).show();
            }
        });
    }

    // ---------- novo empréstimo ----------

    private void mostrarDialogNovoEmprestimo() {
        View view = getLayoutInflater().inflate(R.layout.dialog_novo_emprestimo, null);
        EditText edtUserId = view.findViewById(R.id.edtUserId);
        EditText edtLivroId = view.findViewById(R.id.edtLivroId);

        int meuId = getSharedPreferences("devolva", MODE_PRIVATE).getInt("user_id", 0);
        if (meuId > 0) {
            edtUserId.setText(String.valueOf(meuId));
        }

        new AlertDialog.Builder(this)
                .setTitle("Novo empréstimo")
                .setView(view)
                .setPositiveButton("Emprestar", (d, w) -> {
                    try {
                        int userId = Integer.parseInt(edtUserId.getText().toString().trim());
                        int livroId = Integer.parseInt(edtLivroId.getText().toString().trim());
                        criarEmprestimo(userId, livroId);
                    } catch (NumberFormatException ex) {
                        Toast.makeText(this, "Informe IDs válidos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void criarEmprestimo(int userId, int livroId) {
        ApiClient.get(this).criarEmprestimo(new NovoEmprestimoRequest(userId, livroId)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Empréstimo criado", Toast.LENGTH_SHORT).show();
                    carregarEmprestimos();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao criar empréstimo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Não foi possível conectar à API", Toast.LENGTH_LONG).show();
            }
        });
    }

    // ---------- sair ----------

    private void sair() {
        SharedPreferences prefs = getSharedPreferences("devolva", MODE_PRIVATE);
        prefs.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
