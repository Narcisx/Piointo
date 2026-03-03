package com.example.proyectofinal;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/*
 * Interfaz que define los endpoints (URLs) de la API de iNaturalist.
 * Retrofit genera automáticamente la implementación de esta interfaz.
 * Cada método corresponde a una petición HTTP GET a un endpoint específico.
 */
public interface INaturalistService {

    /*
     * Busca especies (taxa) por nombre.
     * Se usa para obtener la foto principal de cada pájaro.
     */
    @GET("taxa")
    Call<INaturalistResponse> searchTaxa(
            @Query("q") String query,
            @Query("rank") String rank,
            @Query("per_page") int perPage
    );

    /*
     * Busca observaciones que tengan grabaciones de audio (sonidos).
     * Se usa para obtener el canto de cada pájaro.
     */
    @GET("observations")
    Call<INaturalistObservationResponse> searchObservationsWithSounds(
            @Query("taxon_name") String taxonName,
            @Query("sounds") boolean sounds,
            @Query("per_page") int perPage,
            @Query("quality_grade") String qualityGrade,
            @Query("order_by") String orderBy
    );

    /*
     * Busca observaciones que tengan fotos.
     * Se usa para la galería de imágenes de cada pájaro.
     */
    @GET("observations")
    Call<INaturalistObservationResponse> searchObservationsWithPhotos(
            @Query("taxon_name") String taxonName,
            @Query("photos") boolean photos,
            @Query("per_page") int perPage,
            @Query("quality_grade") String qualityGrade,
            @Query("order_by") String orderBy
    );
}
