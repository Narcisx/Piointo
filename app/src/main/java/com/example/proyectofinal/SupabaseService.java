package com.example.proyectofinal;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/*
 * Interfaz que define los endpoints de la API REST de Supabase.
 * Retrofit genera automáticamente la implementación.
 */
public interface SupabaseService {

    /*
     * Obtiene todos los pájaros de la tabla "pajaro" en Supabase.
     * El parámetro "select=*" indica que se traen todas las columnas.
     */
    @GET("pajaro?select=*")
    Call<List<Pajaro>> getPajaros();

    // Endpoint añadido para la funcionalidad de AnadirPajaro
    @retrofit2.http.Headers({
            "Prefer: return=minimal",
            "Content-Type: application/json"
    })
    @retrofit2.http.POST("pajaro")
    Call<Void> insertarPajaro(@retrofit2.http.Body Pajaro nuevoPajaro);
    //Endpoint para actualizar un pájaro existente
    @retrofit2.http.Headers({
            "Prefer: return=minimal",
            "Content-Type: application/json"
    })
    @retrofit2.http.PATCH("pajaro")
    Call<Void> updatePajaro(@retrofit2.http.Query("nombre") String eqNombre, @retrofit2.http.Body Pajaro pajaroActualizado);

    // Endpoint para eliminar un pájaro
    @retrofit2.http.DELETE("pajaro")
    Call<Void> deletePajaro(@retrofit2.http.Query("nombre") String eqNombre);
}
