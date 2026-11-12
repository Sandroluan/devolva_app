package com.exemplo.meuapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Tela do administrador: Empréstimos | Livros | Usuários | Perfil. */
public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Session.isLogged(this) || !Session.isAdmin(this)) {
            Session.logout(this);
            return;
        }

        setContentView(R.layout.activity_main_nav);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.inflateMenu(R.menu.menu_admin);
        nav.setOnItemSelectedListener(item -> {
            mostrar(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_emprestimos);
        }
    }

    private void mostrar(int id) {
        Fragment f;
        if (id == R.id.nav_livros) {
            f = BooksFragment.newInstance(true);
        } else if (id == R.id.nav_usuarios) {
            f = new UsersFragment();
        } else if (id == R.id.nav_perfil) {
            f = new ProfileFragment();
        } else {
            f = LoansFragment.newInstance(true);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
    }
}
