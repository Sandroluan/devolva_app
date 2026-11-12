package com.exemplo.meuapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Aba "Usuários" do admin: lista de todos os usuários. */
public class UsersFragment extends Fragment {

    private UsuarioAdapter adapter;
    private TextView txtVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_users, container, false);

        txtVazio = v.findViewById(R.id.txtVazio);
        RecyclerView recycler = v.findViewById(R.id.recycler);

        adapter = new UsuarioAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregar();
    }

    private void carregar() {
        final Context ctx = requireContext();

        PagedLoader.<Usuario>loadAll(
                page -> ApiClient.get(ctx).listarUsuarios(page),
                new PagedLoader.Listener<Usuario>() {
                    @Override
                    public void onSuccess(List<Usuario> lista) {
                        if (!isAdded()) return;
                        adapter.setLista(lista);
                        txtVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onError(boolean semConexao) {
                        if (!isAdded()) return;
                        Toast.makeText(ctx, semConexao
                                ? "Não foi possível conectar à API"
                                : "Erro ao carregar usuários", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
