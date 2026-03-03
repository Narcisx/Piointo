package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Activity que muestra una galería de fotos de un pájaro.
 * Las fotos se obtienen de la API de iNaturalist buscando observaciones
 * verificadas (research grade) del ave, ordenadas por votos.
 */
//Hereda de BaseActivity para aplicar el idioma
public class Galeria extends BaseActivity implements GaleriaAdapter.OnImageClickListener {

    // --- Vistas del layout ---
    private RecyclerView recyclerGaleria; // Grid de fotos
    private ProgressBar progressBar; // Indicador de carga
    private TextView tvSinFotos, tvNombrePajaro; // Texto si no hay fotos / nombre del ave
    private Toolbar toolbar;

    // --- Datos ---
    private List<String> imageUrls = new ArrayList<>(); // Lista de URLs de imágenes
    private GaleriaAdapter adapter; // Adaptador del RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galeria);

        // Vincular vistas
        toolbar = findViewById(R.id.toolbar);
        recyclerGaleria = findViewById(R.id.recyclerGaleria);
        progressBar = findViewById(R.id.progressBar);
        tvSinFotos = findViewById(R.id.tvSinFotos);
        tvNombrePajaro = findViewById(R.id.tvNombrePajaro);

        // Configurar toolbar con botón de retroceso
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Obtener datos del pájaro enviados desde FichaPajaro
        String nombreCientifico = getIntent().getStringExtra("nombre_cientifico");
        String nombre = getIntent().getStringExtra("nombre");

        tvNombrePajaro.setText(nombre != null ? nombre : "");

        // Configurar RecyclerView con grid de 2 columnas
        recyclerGaleria.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new GaleriaAdapter(this, imageUrls, this);
        recyclerGaleria.setAdapter(adapter);

        // Cargar fotos si tenemos nombre científico
        if (nombreCientifico != null && !nombreCientifico.trim().isEmpty()) {
            cargarFotos(nombreCientifico.trim());
        } else {
            progressBar.setVisibility(View.GONE);
            tvSinFotos.setVisibility(View.VISIBLE);
        }

        // Ajustar padding para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /*
     * Busca fotos del ave en iNaturalist.
     * Obtiene 12 observaciones verificadas ordenadas por votos,
     * extrae todas las fotos de cada observación y las añade a la lista.
     * Las URLs se convierten de tamaño "square" a "medium" para mejor calidad.
     */
    private void cargarFotos(String nombreCientifico) {
        INaturalistService service = INaturalistClient.getClient().create(INaturalistService.class);

        // Buscar 12 observaciones con fotos, calidad "research", ordenadas por votos
        service.searchObservationsWithPhotos(nombreCientifico, true, 12, "research", "votes")
                .enqueue(new Callback<INaturalistObservationResponse>() {
                    @Override
                    public void onResponse(Call<INaturalistObservationResponse> call,
                            Response<INaturalistObservationResponse> response) {
                        // Ocultar indicador de carga
                        runOnUiThread(() -> progressBar.setVisibility(View.GONE));

                        if (response.isSuccessful() && response.body() != null
                                && response.body().results != null) {

                            // Recorrer cada observación y extraer las URLs de sus fotos
                            for (INaturalistObservationResponse.Observation obs : response.body().results) {
                                if (obs.photos != null) {
                                    for (INaturalistObservationResponse.Photo photo : obs.photos) {
                                        if (photo.url != null && !photo.url.isEmpty()) {
                                            // Convertir URL de tamaño "square" a "medium" para mejor calidad
                                            String mediumUrl = photo.url.replace("/square.", "/medium.");
                                            imageUrls.add(mediumUrl);
                                        }
                                    }
                                }
                            }

                            // Actualizar UI en hilo principal
                            runOnUiThread(() -> {
                                if (imageUrls.isEmpty()) {
                                    tvSinFotos.setVisibility(View.VISIBLE);
                                } else {
                                    // Notificar al adaptador que hay nuevas fotos
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            runOnUiThread(() -> tvSinFotos.setVisibility(View.VISIBLE));
                        }
                    }

                    @Override
                    public void onFailure(Call<INaturalistObservationResponse> call, Throwable t) {
                        t.printStackTrace();
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            tvSinFotos.setVisibility(View.VISIBLE);
                        });
                    }
                });
    }

    /*
     * Callback del adaptador cuando el usuario pulsa una foto.
     * Abre la Activity de visor a pantalla completa con la URL de la imagen.
     */
    @Override
    public void onImageClick(String imageUrl) {
        Intent intent = new Intent(this, FullscreenImageActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        startActivity(intent);
    }
}
