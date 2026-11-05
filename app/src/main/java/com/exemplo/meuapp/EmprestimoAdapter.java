package com.exemplo.meuapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmprestimoAdapter extends RecyclerView.Adapter<EmprestimoAdapter.VH> {

    public interface OnItemClick {
        void onClick(Emprestimo emprestimo);
    }

    private final List<Emprestimo> lista = new ArrayList<>();
    private final OnItemClick listener;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public EmprestimoAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void setLista(List<Emprestimo> novaLista) {
        lista.clear();
        lista.addAll(novaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emprestimo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Emprestimo e = lista.get(position);

        h.txtTitulo.setText(e.tituloLivro);
        h.txtUsuario.setText(e.nomeUsuario);
        h.txtData.setText("Data devolução: " + formatarData(e.prazo));
        h.txtMulta.setText("Multa: " + moeda.format(e.valorMulta));

        if (e.estaEntregue()) {
            h.txtBadge.setText("ENTREGUE");
            h.txtBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#757575")));
        } else if (e.diasDeAtraso > 0) {
            h.txtBadge.setText("ATRASADO (" + e.diasDeAtraso + (e.diasDeAtraso == 1 ? " dia)" : " dias)"));
            h.txtBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D32F2F")));
        } else {
            h.txtBadge.setText("NO PRAZO");
            h.txtBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#388E3C")));
        }

        h.itemView.setOnClickListener(v -> listener.onClick(e));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // A API devolve algo como "2026-10-10T03:00:00.000Z"; pegamos só a data.
    private String formatarData(String iso) {
        if (iso == null || iso.length() < 10) return "-";
        String[] p = iso.substring(0, 10).split("-");
        if (p.length != 3) return iso;
        return p[2] + "/" + p[1] + "/" + p[0];
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtUsuario, txtBadge, txtData, txtMulta;

        VH(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtUsuario = itemView.findViewById(R.id.txtUsuario);
            txtBadge = itemView.findViewById(R.id.txtBadge);
            txtData = itemView.findViewById(R.id.txtData);
            txtMulta = itemView.findViewById(R.id.txtMulta);
        }
    }
}
