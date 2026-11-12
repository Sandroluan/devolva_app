package com.exemplo.meuapp;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

/** Carrega a capa de um livro (ou o fundo cinza se não houver capa). */
public final class Capas {

    private Capas() {}

    public static void carregar(ImageView imageView, String url) {
        String modelo = (url == null || url.trim().isEmpty()) ? null : url.trim();

        Glide.with(imageView)
                .load(modelo)
                .placeholder(R.drawable.bg_cover_placeholder)
                .error(R.drawable.bg_cover_placeholder)
                .fallback(R.drawable.bg_cover_placeholder)
                .centerCrop()
                .into(imageView);
    }
}
