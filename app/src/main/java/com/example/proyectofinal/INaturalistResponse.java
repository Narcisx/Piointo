package com.example.proyectofinal;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/*
 * Modelo de datos para la respuesta del endpoint /taxa de iNaturalist.
 * Se usa para buscar especies y obtener sus fotos por defecto.
 */
public class INaturalistResponse {

    // Número total de resultados encontrados
    @SerializedName("total_results")
    public int totalResults;

    // Lista de resultados (taxa/especies encontradas)
    public List<Result> results;

    /*
     * Cada resultado representa una especie encontrada.
     */
    public static class Result {
        public int id;
        public String name;  // Nombre científico, ej: "Parus major"
        public String rank;  // Rango taxonómico, ej: "species"

        // Foto por defecto de la especie (la que aparece en iNaturalist)
        @SerializedName("default_photo")
        public DefaultPhoto defaultPhoto;

        // Nombre común preferido, ej: "Carbonero común"
        @SerializedName("preferred_common_name")
        public String preferredCommonName;
    }

    /*
     * Foto por defecto de una especie.
     * Incluye URLs en varios tamaños (square, medium, original).
     */
    public static class DefaultPhoto {
        public int id;

        // URL en tamaño medio (usada como imagen principal en la app)
        @SerializedName("medium_url")
        public String mediumUrl;

        // URL en tamaño cuadrado (miniatura)
        @SerializedName("square_url")
        public String squareUrl;

        // URL genérica
        public String url;

        // Créditos/atribución del fotógrafo
        public String attribution;
    }
}
