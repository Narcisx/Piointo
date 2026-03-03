package com.example.proyectofinal;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/*
 * Modelo de datos para la respuesta del endpoint /observations de iNaturalist.
 * Se usa tanto para obtener audios (cantos) como fotos de las aves.
 */
public class INaturalistObservationResponse {

    // Número total de observaciones encontradas
    @SerializedName("total_results")
    public int totalResults;

    // Lista de observaciones devueltas
    public List<Observation> results;

    /*
     * Cada observación es un avistamiento registrado por un usuario de iNaturalist.
     * Puede incluir sonidos (grabaciones de audio) y/o fotos.
     */
    public static class Observation {
        public int id;
        public List<Sound> sounds;  // Lista de grabaciones de audio (puede ser null)
        public List<Photo> photos;  // Lista de fotos asociadas (puede ser null)
        public Taxon taxon;         // Información taxonómica (especie)
    }

    /*
     * Grabación de audio (canto del pájaro).
     */
    public static class Sound {
        public int id;

        // URL del archivo de audio (ej: https://static.inaturalist.org/sounds/12345.mp3)
        @SerializedName("file_url")
        public String fileUrl;

        // Tipo MIME del archivo (ej: "audio/mpeg" para MP3, "audio/wav" para WAV)
        @SerializedName("file_content_type")
        public String fileContentType;

        // Créditos del autor de la grabación
        public String attribution;
    }

    /*
     * Foto de una observación.
     * La URL viene en tamaño "square" por defecto.
     */
    public static class Photo {
        public int id;

        // URL de la foto (por defecto en tamaño cuadrado/square)
        public String url;

        // Créditos del fotógrafo
        public String attribution;

        // Dimensiones originales de la foto
        @SerializedName("original_dimensions")
        public OriginalDimensions originalDimensions;
    }

    /*
     * Dimensiones originales de una foto (ancho x alto en píxeles).
     */
    public static class OriginalDimensions {
        public int height;
        public int width;
    }

    /*
     * Información taxonómica de la especie observada.
     */
    public static class Taxon {
        public int id;
        public String name;  // Nombre científico
        public String rank;  // Rango taxonómico (ej: "species")
    }
}
