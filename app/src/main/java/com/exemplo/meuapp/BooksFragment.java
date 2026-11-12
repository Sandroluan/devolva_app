package com.exemplo.meuapp;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lista de livros com capa e descrição.
 * - cliente (admin = false): "Catálogo"; tocar abre os detalhes com o botão "Pegar emprestado".
 * - admin (admin = true): "Livros"; botão + adiciona, e cada item tem Editar / Excluir.
 */
public class BooksFragment extends Fragment {

    private static final String ARG_ADMIN = "admin";

    public static BooksFragment newInstance(boolean admin) {
        BooksFragment f = new BooksFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARG_ADMIN, admin);
        f.setArguments(b);
        return f;
    }

    private boolean admin;
    private EditText edtBusca;
    private TextView txtVazio;
    private LivroAdapter adapter;
    private final List<Livro> todos = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        admin = getArguments() != null && getArguments().getBoolean(ARG_ADMIN, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_books, container, false);

        TextView txtTitulo = v.findViewById(R.id.txtTituloTela);
        edtBusca = v.findViewById(R.id.edtBusca);
        txtVazio = v.findViewById(R.id.txtVazio);
        FloatingActionButton fab = v.findViewById(R.id.fab);
        RecyclerView recycler = v.findViewById(R.id.recycler);

        txtTitulo.setText(admin ? "Livros" : "Catálogo");

        adapter = new LivroAdapter(admin, new LivroAdapter.Listener() {
            @Override
            public void onClick(Livro l) {
                BookDetailActivity.start(requireContext(), l.livroId, l.titulo, l.autor,
                        l.descricao, l.capaUrl, !admin);
            }

            @Override
            public void onEditar(Livro l) {
                mostrarDialogLivro(l);
            }

            @Override
            public void onExcluir(Livro l) {
                confirmarExclusao(l);
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltro();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (admin) {
            fab.setOnClickListener(x -> mostrarDialogLivro(null));
        } else {
            fab.setVisibility(View.GONE);
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregar();
    }

    // ---------- listar ----------

    private void carregar() {
        final Context ctx = requireContext();

        PagedLoader.<Livro>loadAll(
                page -> ApiClient.get(ctx).listarLivros(page),
                new PagedLoader.Listener<Livro>() {
                    @Override
                    public void onSuccess(List<Livro> lista) {
                        if (!isAdded()) return;
                        todos.clear();
                        todos.addAll(lista);
                        aplicarFiltro();
                    }

                    @Override
                    public void onError(boolean semConexao) {
                        toast(semConexao ? "Não foi possível conectar à API" : "Erro ao carregar livros");
                    }
                });
    }

    private void aplicarFiltro() {
        String termo = edtBusca.getText().toString().trim().toLowerCase(Locale.getDefault());
        List<Livro> filtrada = new ArrayList<>();

        for (Livro l : todos) {
            String titulo = l.titulo == null ? "" : l.titulo.toLowerCase(Locale.getDefault());
            String autor = l.autor == null ? "" : l.autor.toLowerCase(Locale.getDefault());
            if (termo.isEmpty() || titulo.contains(termo) || autor.contains(termo)) {
                filtrada.add(l);
            }
        }

        adapter.setLista(filtrada);
        txtVazio.setVisibility(filtrada.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ---------- adicionar / editar (admin) ----------

    // livro == null  -> novo livro (a API busca capa e descrição no Google Books)
    // livro != null  -> editar título, autor, descrição e URL da capa
    private void mostrarDialogLivro(@Nullable Livro livro) {
        final boolean editando = livro != null;

        View view = getLayoutInflater().inflate(R.layout.dialog_livro, null);
        TextView txtInfo = view.findViewById(R.id.txtInfo);
        EditText edtTitulo = view.findViewById(R.id.edtTitulo);
        EditText edtAutor = view.findViewById(R.id.edtAutor);
        EditText edtDescricao = view.findViewById(R.id.edtDescricao);
        EditText edtCapaUrl = view.findViewById(R.id.edtCapaUrl);

        if (editando) {
            txtInfo.setVisibility(View.GONE);
            edtTitulo.setText(livro.titulo);
            edtAutor.setText(livro.autor);
            edtDescricao.setText(livro.descricao);
            edtCapaUrl.setText(livro.capaUrl);
        } else {
            edtDescricao.setVisibility(View.GONE);
            edtCapaUrl.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(editando ? "Editar livro" : "Novo livro")
                .setView(view)
                .setPositiveButton("Salvar", (d, w) -> {
                    String titulo = edtTitulo.getText().toString().trim();
                    String autor = edtAutor.getText().toString().trim();

                    if (titulo.isEmpty() || autor.isEmpty()) {
                        toast("Informe título e autor");
                        return;
                    }

                    LivroRequest req = new LivroRequest();
                    req.titulo = titulo;
                    req.autor = autor;

                    if (editando) {
                        req.descricao = vazioParaNull(edtDescricao.getText().toString());
                        req.capaUrl = vazioParaNull(edtCapaUrl.getText().toString());
                        atualizarLivro(livro.livroId, req);
                    } else {
                        criarLivro(req);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void criarLivro(LivroRequest req) {
        toast("Salvando... buscando capa e descrição");

        ApiClient.get(requireContext()).criarLivro(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    toast("Livro criado");
                    if (isAdded()) carregar();
                } else {
                    toast("Erro ao criar livro (o título já existe?)");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                toast("Não foi possível conectar à API");
            }
        });
    }

    private void atualizarLivro(int livroId, LivroRequest req) {
        ApiClient.get(requireContext()).atualizarLivro(livroId, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    toast("Livro atualizado");
                    if (isAdded()) carregar();
                } else {
                    toast("Erro ao atualizar livro");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                toast("Não foi possível conectar à API");
            }
        });
    }

    // ---------- excluir (admin) ----------

    private void confirmarExclusao(Livro l) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir livro")
                .setMessage("Excluir \"" + l.titulo + "\"?")
                .setPositiveButton("Excluir", (d, w) -> excluirLivro(l))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirLivro(Livro l) {
        ApiClient.get(requireContext()).excluirLivro(l.livroId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    toast("Livro excluído");
                    if (isAdded()) carregar();
                } else {
                    // o banco recusa se o livro já tem empréstimos
                    toast("Não foi possível excluir (o livro pode ter empréstimos)");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                toast("Não foi possível conectar à API");
            }
        });
    }

    // ---------- util ----------

    private static String vazioParaNull(String s) {
        String t = s == null ? "" : s.trim();
        return t.isEmpty() ? null : t;
    }

    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
