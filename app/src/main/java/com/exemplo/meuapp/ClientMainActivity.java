package com.exemplo.meuapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Tela do cliente: Início (meus empréstimos) | Catálogo | Perfil. */
public class ClientMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Session.isLogged(this)) {
            Session.logout(this);
            return;
        }

        setContentView(R.layout.activity_main_nav);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.inflateMenu(R.menu.menu_client);
        nav.setOnItemSelectedListener(item -> {
            mostrar(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_inicio);
        }
    }

    private void mostrar(int id) {
        Fragment f;
        if (id == R.id.nav_catalogo) {
            f = BooksFragment.newInstance(false);
        } else if (id == R.id.nav_perfil) {
            f = new ProfileFragment();
        } else {
            f = LoansFragment.newInstance(false);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
    }
}
