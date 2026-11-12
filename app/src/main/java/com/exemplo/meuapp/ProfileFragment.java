package com.exemplo.meuapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/** Aba "Perfil": dados de quem está logado + botão Sair (admin e cliente). */
public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView txtNome = v.findViewById(R.id.txtNome);
        TextView txtEmail = v.findViewById(R.id.txtEmail);
        TextView txtTipo = v.findViewById(R.id.txtTipo);
        Button btnSair = v.findViewById(R.id.btnSair);

        String nome = Session.name(requireContext());
        String email = Session.email(requireContext());
        boolean admin = Session.isAdmin(requireContext());

        txtNome.setText(nome != null && !nome.isEmpty() ? nome : email);
        txtEmail.setText(email);
        txtTipo.setText(admin ? "ADMINISTRADOR" : "CLIENTE");
        txtTipo.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor(admin ? "#6A1B9A" : "#1565C0")));

        btnSair.setOnClickListener(x -> Session.logout(requireContext()));
        return v;
    }
}
