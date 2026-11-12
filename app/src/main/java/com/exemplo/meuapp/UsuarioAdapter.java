package com.exemplo.meuapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.VH> {

    private final List<Usuario> lista = new ArrayList<>();

    public void setLista(List<Usuario> novaLista) {
        lista.clear();
        lista.addAll(novaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Usuario u = lista.get(position);

        h.txtNome.setText(u.name);
        h.txtEmail.setText(u.email);
        h.txtId.setText("ID " + u.userId);

        boolean admin = "admin".equalsIgnoreCase(u.tipo);
        h.txtTipo.setText(admin ? "ADMIN" : "CLIENTE");
        h.txtTipo.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor(admin ? "#6A1B9A" : "#1565C0")));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNome, txtEmail, txtTipo, txtId;

        VH(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtId = itemView.findViewById(R.id.txtId);
        }
    }
}
