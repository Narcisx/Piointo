package com.example.proyectofinal;

import java.io.Serializable;

/*
 * Modelo de datos que representa un pájaro de la base de datos Supabase.
 * Implementa Serializable para poder enviarse entre Activities a través de Intent.
 * Los nombres de los campos coinciden con los nombres de las columnas en Supabase
 * para que Gson los mapee automáticamente.
 */
public class Pajaro implements Serializable {
    public void setNombre_cientifico(String nombre_cientifico) {
        this.nombre_cientifico = nombre_cientifico;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setOrden(String orden) {
        this.orden = orden;
    }

    public void setFamilia(String familia) {
        this.familia = familia;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public void setEnvergadura(String envergadura) {
        this.envergadura = envergadura;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public void setCanto(String canto) {
        this.canto = canto;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public void setAlimentacion(String alimentacion) {
        this.alimentacion = alimentacion;
    }
    private Integer id; // ID único del pájaro en Supabase (Integer para permitir null)
    private String imagen_url; // URL de la imagen (se obtiene de iNaturalist)
    private String nombre; // Nombre común (ej: "Carbonero común")
    private String nombre_cientifico; // Nombre científico (ej: "Parus major")
    private String orden; // Orden taxonómico (ej: "Passeriformes")
    private String familia; // Familia taxonómica (ej: "Paridae")
    private String longitud; // Longitud del ave (ej: "14 cm")
    private String envergadura; // Envergadura alar (ej: "22-25 cm")
    private String desc; // Descripción general del ave
    private String identificacion; // Claves para identificar al ave en campo
    private String canto; // Descripción textual del canto
    private String habitat; // Hábitat donde se encuentra
    private String alimentacion; // Tipo de alimentación

    // --- Getters & Setters ---
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    public String getImagen_url() {
        return imagen_url;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLongitud() {
        return longitud;
    }

    public String getFamilia() {
        return familia;
    }

    public String getEnvergadura() {
        return envergadura;
    }

    public String getDesc() {
        return desc;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getCanto() {
        return canto;
    }

    public String getHabitat() {
        return habitat;
    }

    public String getAlimentacion() {
        return alimentacion;
    }

    public String getNombre_cientifico() {
        return nombre_cientifico;
    }

    public String getOrden() {
        return orden;
    }

    // --- Setter para actualizar la URL de imagen tras obtenerla de iNaturalist ---
    public void setImagen_url(String imagen_url) {
        this.imagen_url = imagen_url;
    }

    // --- Setter para cambiar el nombre al traducir ---
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
