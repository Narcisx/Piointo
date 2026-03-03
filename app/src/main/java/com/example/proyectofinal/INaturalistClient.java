package com.example.proyectofinal;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
 * Cliente HTTP para conectarse a la API de iNaturalist.
 * Usa el patrón Singleton para reutilizar la misma instancia de Retrofit
 * en toda la aplicación (evita crear conexiones innecesarias).
 */
public class INaturalistClient {

    // URL base de la API de iNaturalist (versión 1)
    private static final String BASE_URL = "https://api.inaturalist.org/v1/";

    // Instancia única de Retrofit (Singleton)
    private static Retrofit retrofit;

    /*
     * Devuelve la instancia de Retrofit configurada.
     * Si no existe, la crea con:
     * - Un interceptor que añade el header "User-Agent" a cada petición
     *   (necesario para que la API identifique nuestra app)
     * - Gson como convertidor de JSON a objetos Java
     */
    public static Retrofit getClient() {
        if (retrofit == null) {

            // Configurar cliente HTTP con interceptor para añadir User-Agent
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("User-Agent", "AppPajaros/1.0 AndroidRetrofit")
                                .build();
                        return chain.proceed(newRequest);
                    })
                    .build();

            // Construir instancia de Retrofit con la URL base y el convertidor Gson
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
