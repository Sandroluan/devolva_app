package com.exemplo.meuapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LivroAdapter extends RecyclerView.Adapter<LivroAdapter.VH> {

    public interface Listener {
        void onClick(Livro livro);

        void onEditar(Livro livro);

        void onExcluir(Livro livro);
    }

    private final List<Livro> lista = new ArrayList<>();
    private final boolean admin;
    private final Listener listener;

    public LivroAdapter(boolean admin, Listener listener) {
        this.admin = admin;
        this.listener = listener;
    }

    public void setLista(List<Livro> novaLista) {
        lista.clear();
        lista.addAll(novaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_livro, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Livro l = lista.get(position);

        h.txtTitulo.setText(l.titulo);
        h.txtAutor.setText(l.autor);

        if (l.descricao == null || l.descricao.trim().isEmpty()) {
            h.txtDescricao.setText("Sem descrição disponível.");
        } else {
            h.txtDescricao.setText(l.descricao);
        }

        Capas.carregar(h.imgCapa, l.capaUrl);

        if (admin) {
            h.txtId.setVisibility(View.VISIBLE);
            h.txtId.setText("ID " + l.livroId);
            h.layoutAcoes.setVisibility(View.VISIBLE);
            h.btnEditar.setOnClickListener(v -> listener.onEditar(l));
            h.btnExcluir.setOnClickListener(v -> listener.onExcluir(l));
        } else {
            h.txtId.setVisibility(View.GONE);
            h.layoutAcoes.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(l));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgCapa;
        TextView txtTitulo, txtAutor, txtDescricao, txtId, btnEditar, btnExcluir;
        View layoutAcoes;

        VH(@NonNull View itemView) {
            super(itemView);
            imgCapa = itemView.findViewById(R.id.imgCapa);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtAutor = itemView.findViewById(R.id.txtAutor);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtId = itemView.findViewById(R.id.txtId);
            layoutAcoes = itemView.findViewById(R.id.layoutAcoes);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}
