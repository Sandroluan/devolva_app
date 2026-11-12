package com.exemplo.meuapp;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** A API devolve 10 itens por página: busca página por página até vir uma incompleta. */
public final class PagedLoader {

    public static final int POR_PAGINA = 10;
    private static final int MAX_PAGINAS = 100; // trava de segurança

    public interface Fetcher<T> {
        Call<List<T>> fetch(int pagina);
    }

    public interface Listener<T> {
        void onSuccess(List<T> todos);

        void onError(boolean semConexao);
    }

    private PagedLoader() {}

    public static <T> void loadAll(Fetcher<T> fetcher, Listener<T> listener) {
        carregar(fetcher, listener, 1, new ArrayList<T>());
    }

    private static <T> void carregar(final Fetcher<T> fetcher, final Listener<T> listener,
                                     final int pagina, final List<T> acumulado) {
        fetcher.fetch(pagina).enqueue(new Callback<List<T>>() {
            @Override
            public void onResponse(Call<List<T>> call, Response<List<T>> response) {
                List<T> body = response.body();
                if (!response.isSuccessful() || body == null) {
                    listener.onError(false);
                    return;
                }

                acumulado.addAll(body);

                if (body.size() == POR_PAGINA && pagina < MAX_PAGINAS) {
                    carregar(fetcher, listener, pagina + 1, acumulado);
                } else {
                    listener.onSuccess(acumulado);
                }
            }

            @Override
            public void onFailure(Call<List<T>> call, Throwable t) {
                listener.onError(true);
            }
        });
    }
}
