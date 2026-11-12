package com.exemplo.meuapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/** Guarda quem está logado (token JWT + tipo) e cuida de sair da conta. */
public final class Session {

    private static final String PREFS = "devolva";

    private Session() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void save(Context c, LoginResponse r) {
        prefs(c).edit()
                .putString("token", r.token)
                .putInt("user_id", r.user.userId)
                .putString("tipo", r.user.tipo)
                .putString("email", r.user.email)
                .putString("name", r.user.name)
                .apply();
    }

    public static String token(Context c) {
        return prefs(c).getString("token", null);
    }

    public static String tipo(Context c) {
        return prefs(c).getString("tipo", null);
    }

    public static boolean isLogged(Context c) {
        return token(c) != null && tipo(c) != null;
    }

    public static boolean isAdmin(Context c) {
        return "admin".equalsIgnoreCase(tipo(c));
    }

    public static int userId(Context c) {
        return prefs(c).getInt("user_id", 0);
    }

    public static String name(Context c) {
        return prefs(c).getString("name", null);
    }

    public static String email(Context c) {
        return prefs(c).getString("email", null);
    }

    /** Sair pelo botão. */
    public static void logout(Context c) {
        prefs(c).edit().clear().apply();
        irParaLogin(c, false);
    }

    /** Token vencido/inválido (a API respondeu 401). Só age se ainda havia sessão. */
    public static void expire(Context c) {
        if (token(c) == null) return;
        prefs(c).edit().clear().apply();
        irParaLogin(c, true);
    }

    private static void irParaLogin(Context c, boolean expirou) {
        Context app = c.getApplicationContext();
        Intent i = new Intent(app, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra("expirou", expirou);
        app.startActivity(i);
    }
}
