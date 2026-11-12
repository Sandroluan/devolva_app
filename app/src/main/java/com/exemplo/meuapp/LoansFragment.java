package com.exemplo.meuapp;

import android.content.Context;
import android.os.Bundle;
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

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lista de empréstimos com resumo de multas.
 * - cliente (admin = false): "Meus Empréstimos"; tocar abre os detalhes do livro.
 * - admin (admin = true): todos os empréstimos; tocar devolve; botão + cria empréstimo.
 * A API já filtra pelo token: o cliente só recebe os empréstimos dele.
 */
public class LoansFragment extends Fragment {

    private static final String ARG_ADMIN = "admin";

    public static LoansFragment newInstance(boolean admin) {
        LoansFragment f = new LoansFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARG_ADMIN, admin);
        f.setArguments(b);
        return f;
    }

    private boolean admin;
    private TextView txtMultaTotal, txtAtrasados, txtVazio;
    private ChipGroup chipGroup;
    private EmprestimoAdapter adapter;
    private final List<Emprestimo> todos = new ArrayList<>();
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        admin = getArguments() != null && getArguments().getBoolean(ARG_ADMIN, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_loans, container, false);

        TextView txtTitulo = v.findViewById(R.id.txtTituloTela);
        txtMultaTotal = v.findViewById(R.id.txtMultaTotal);
        txtAtrasados = v.findViewById(R.id.txtAtrasados);
        txtVazio = v.findViewById(R.id.txtVazio);
        chipGroup = v.findViewById(R.id.chipGroup);
        FloatingActionButton fab = v.findViewById(R.id.fab);
        RecyclerView recycler = v.findViewById(R.id.recycler);

        txtTitulo.setText(admin ? "Empréstimos" : "Meus Empréstimos");

        adapter = new EmprestimoAdapter(admin, this::aoClicar);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        chipGroup.setOnCheckedStateChangeListener((group, ids) -> aplicarFiltro());

        if (admin) {
            fab.setOnClickListener(x -> mostrarDialogNovoEmprestimo());
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

        PagedLoader.<Emprestimo>loadAll(
                page -> ApiClient.get(ctx).listarEmprestimos(page),
                new PagedLoader.Listener<Emprestimo>() {
                    @Override
                    public void onSuccess(List<Emprestimo> lista) {
                        if (!isAdded()) return;
                        todos.clear();
                        todos.addAll(lista);
                        atualizarResumo();
                        aplicarFiltro();
                    }

                    @Override
                    public void onError(boolean semConexao) {
                        toast(semConexao ? "Não foi possível conectar à API" : "Erro ao carregar empréstimos");
                    }
                });
    }

    // o resumo sempre considera todos os empréstimos, independente do filtro
    private void atualizarResumo() {
        double totalMulta = 0;
        int atrasados = 0;

        for (Emprestimo e : todos) {
            if (!e.estaEntregue() && e.diasDeAtraso > 0) {
                atrasados++;
                totalMulta += e.valorMulta;
            }
        }

        txtMultaTotal.setText(moeda.format(totalMulta));
        txtAtrasados.setText(atrasados + (atrasados == 1 ? " Livro\nAtrasado" : " Livros\nAtrasados"));
    }

    private void aplicarFiltro() {
        int chip = chipGroup.getCheckedChipId();
        List<Emprestimo> filtrada = new ArrayList<>();

        for (Emprestimo e : todos) {
            if (chip == R.id.chipAtrasados) {
                if (!e.estaEntregue() && e.diasDeAtraso > 0) filtrada.add(e);
            } else if (chip == R.id.chipEntregues) {
                if (e.estaEntregue()) filtrada.add(e);
            } else {
                filtrada.add(e);
            }
        }

        adapter.setLista(filtrada);
        txtVazio.setVisibility(filtrada.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ---------- clique no item ----------

    private void aoClicar(Emprestimo e) {
        if (admin) {
            confirmarDevolucao(e);
        } else {
            BookDetailActivity.start(requireContext(), e.livroId, e.tituloLivro,
                    e.autor, e.descricao, e.capaUrl, false);
        }
    }

    // ---------- devolver (admin) ----------

    private void confirmarDevolucao(Emprestimo e) {
        if (e.estaEntregue()) {
            toast("Este livro já foi entregue");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Devolver livro")
                .setMessage("Marcar \"" + e.tituloLivro + "\" como entregue?")
                .setPositiveButton("Sim", (d, w) -> devolver(e))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void devolver(Emprestimo e) {
        ApiClient.get(requireContext()).devolver(e.emprestimoId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (isAdded()) carregar();
                } else {
                    toast("Erro ao devolver");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                toast("Não foi possível conectar à API");
            }
        });
    }

    // ---------- novo empréstimo (admin) ----------

    private void mostrarDialogNovoEmprestimo() {
        View view = getLayoutInflater().inflate(R.layout.dialog_novo_emprestimo, null);
        EditText edtUserId = view.findViewById(R.id.edtUserId);
        EditText edtLivroId = view.findViewById(R.id.edtLivroId);

        new AlertDialog.Builder(requireContext())
                .setTitle("Novo empréstimo")
                .setView(view)
                .setPositiveButton("Emprestar", (d, w) -> {
                    try {
                        int userId = Integer.parseInt(edtUserId.getText().toString().trim());
                        int livroId = Integer.parseInt(edtLivroId.getText().toString().trim());
                        criarEmprestimo(userId, livroId);
                    } catch (NumberFormatException ex) {
                        toast("Informe IDs válidos");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void criarEmprestimo(int userId, int livroId) {
        ApiClient.get(requireContext()).criarEmprestimo(new NovoEmprestimoRequest(userId, livroId))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            toast("Empréstimo criado");
                            if (isAdded()) carregar();
                        } else {
                            toast("Erro ao criar empréstimo (confira os IDs)");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        toast("Não foi possível conectar à API");
                    }
                });
    }

    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
