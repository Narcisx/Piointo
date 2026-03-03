package com.example.proyectofinal;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
 * Cliente HTTP para conectarse a la API REST de Supabase.
 * Usa el patrón Singleton para reutilizar la misma instancia de Retrofit.
 * Cada petición incluye automáticamente los headers de autenticación.
 */
public class SupabaseClient {

    //URL base de la API REST de Supabase (apunta a la tabla del proyecto)
    private static final String BASE_URL = "https://kgogfcfbkanmibmbaqas.supabase.co/rest/v1/";

    //API Key de Supabase (rol "anon" = acceso público de solo lectura)
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtnb2dmY2Zia2FubWlibWJhcWFzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE4Njc2OTQsImV4cCI6MjA4NzQ0MzY5NH0.vdmcpKW1BNCJ-h__4gYDPiV8DvgVA87k2-UrcyG3WI8";

    //Instancia única de Retrofit (Singleton)
    private static Retrofit retrofit;

    /*
     * Devuelve la instancia de Retrofit configurada.
     * Añade automáticamente los headers necesarios para Supabase:
     * - apikey: clave de autenticación del proyecto
     * - Authorization: token Bearer para la autenticación
     * - Content-Type y Accept: formato JSON
     */
    public static Retrofit getClient() {

        if (retrofit == null) {

            //Configurar cliente HTTP con interceptor para añadir headers de autenticación
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("apikey", API_KEY)
                                .addHeader("Authorization", "Bearer " + API_KEY)
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Accept", "application/json")
                                .build();
                        return chain.proceed(newRequest);
                    })
                    .build();

            //Construir Retrofit con la URL base, cliente HTTP y convertidor Gson
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
