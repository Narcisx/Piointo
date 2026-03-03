package com.example.proyectofinal;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import androidx.core.content.ContextCompat;
import java.util.Locale;

/*
 * Activity que muestra la ficha detallada de un pájaro.
 */
// Hereda de BaseActivity para aplicar el idioma
public class FichaPajaro extends BaseActivity {

    // --- Vistas del layout ---
    Toolbar toolbar;
    ImageView imagen;
    TextView nombre, nombreC, orden, familia, longitud, envergadura, desc, identificacion, canto, habitat, alimentacion;
    Button btnPlayCanto, btnEditar, btnEliminar; //Agregados botones Editar/Eliminar
    android.widget.ImageButton btnFavorito; //Agregado botón de Favoritos

    // Reproductor de audio
    private MediaPlayer mediaPlayer; // Reproductor de audio para el canto
    private boolean isPlaying = false; // Estado actual de reproducción
    private String audioUrl = null; // URL del audio encontrado en iNaturalist

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ficha_pajaro);

        // Vincular vistas del layout
        toolbar = findViewById(R.id.toolbar);
        nombre = findViewById(R.id.tvNombre);
        nombreC = findViewById(R.id.tvNombreC);
        desc = findViewById(R.id.tvDescripcion);
        imagen = findViewById(R.id.imgPajaro);
        orden = findViewById(R.id.tvOrden);
        familia = findViewById(R.id.tvFamilia);
        longitud = findViewById(R.id.tvLongitud);
        envergadura = findViewById(R.id.tvEnvergadura);
        identificacion = findViewById(R.id.tvIdentificacion);
        canto = findViewById(R.id.tvCanto);
        habitat = findViewById(R.id.tvHabitat);
        alimentacion = findViewById(R.id.tvAlimentacion);
        btnPlayCanto = findViewById(R.id.btnPlayCanto);
        btnFavorito = findViewById(R.id.btnFavoritoFicha);
        btnEditar = findViewById(R.id.btnEditarPajaro);
        btnEliminar = findViewById(R.id.btnEliminarPajaro);

        // Configurar toolbar con botón de retroceso
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Obtener el pájaro enviado desde ListaPajaros ---
        Pajaro pajaro = (Pajaro) getIntent().getSerializableExtra("pajaro");

        // Lógica para gestionar Pájaros Favoritos con SharedPreferences
        if (pajaro != null) {
            android.content.SharedPreferences prefs = getSharedPreferences("Favoritos", MODE_PRIVATE);
            boolean esFavorito = prefs.getBoolean(pajaro.getNombre(), false);
            actualizarIcono(esFavorito);

            btnFavorito.setOnClickListener(v -> {
                boolean nuevoEstado = !prefs.getBoolean(pajaro.getNombre(), false);
                prefs.edit().putBoolean(pajaro.getNombre(), nuevoEstado).apply();
                actualizarIcono(nuevoEstado);
            });
        }

        if (pajaro != null) {
            // Rellenar los campos de texto con los datos del pájaro
            nombre.setText(pajaro.getNombre());
            nombreC.setText(pajaro.getNombre_cientifico());
            desc.setText(pajaro.getDesc());

            // Datos taxonómicos (mostrar "—" si no hay dato)
            orden.setText(pajaro.getOrden() != null ? pajaro.getOrden() : "—");
            familia.setText(pajaro.getFamilia() != null ? pajaro.getFamilia() : "—");
            longitud.setText(pajaro.getLongitud() != null ? pajaro.getLongitud() : "—");
            envergadura.setText(pajaro.getEnvergadura() != null ? pajaro.getEnvergadura() : "—");

            identificacion.setText(pajaro.getIdentificacion());
            canto.setText(pajaro.getCanto());
            habitat.setText(pajaro.getHabitat());
            alimentacion.setText(pajaro.getAlimentacion());

            // Traducción dinámica con Gemini si el móvil está en inglés
            String idiomaActual = Locale.getDefault().getLanguage();
            if (idiomaActual.equals(new Locale("en").getLanguage())) {
                traducirDatosConGemini(pajaro);
            }

            // Cargar imagen principal con Glide ---
            if (pajaro.getImagen_url() != null && !pajaro.getImagen_url().trim().isEmpty()) {
                String imgUrl = pajaro.getImagen_url().trim();
                android.util.Log.d("DEBUG_IMAGE", "Cargando imagen Ficha: [" + imgUrl + "]");
                com.bumptech.glide.Glide.with(this)
                        .load(imgUrl)
                        .placeholder(android.R.drawable.ic_menu_report_image) // Imagen mientras carga
                        .error(android.R.drawable.ic_dialog_alert) // Imagen si hay error
                        .centerCrop()
                        .into(imagen);
            } else {
                android.util.Log.d("DEBUG_IMAGE", "La imagen FichaPajaro es nula o vacia");
                imagen.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Buscar canto en iNaturalist (si tiene nombre científico)
            if (pajaro.getNombre_cientifico() != null && !pajaro.getNombre_cientifico().trim().isEmpty()) {
                buscarCantoEnINaturalist(pajaro.getNombre_cientifico().trim());
            }

            // Botón de galería de fotos
            // Al pulsarlo, abre la Activity Galeria con las fotos del pájaro
            View btnGaleria = findViewById(R.id.btnGaleria);
            btnGaleria.setOnClickListener(v -> {
                Intent intent = new Intent(FichaPajaro.this, Galeria.class);
                intent.putExtra("nombre_cientifico", pajaro.getNombre_cientifico());
                intent.putExtra("nombre", pajaro.getNombre());
                startActivity(intent);
            });

            // Lógica para Eliminar Pájaro
            btnEliminar.setOnClickListener(v -> eliminarPajaro(pajaro.getNombre()));

            // Lógica para Editar Pájaro
            btnEditar.setOnClickListener(v -> {
                Intent intentEditar = new Intent(FichaPajaro.this, EditarPajaroActivity.class);
                intentEditar.putExtra("pajaro", pajaro);
                startActivity(intentEditar);
            });
        }

        // Botón de reproducción de canto
        // Alterna entre reproducir y detener el audio
        btnPlayCanto.setOnClickListener(v -> {
            if (isPlaying) {
                detenerAudio();
            } else {
                reproducirAudio();
            }
        });

        // Ajustar padding para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /*
     * Traduce los textos descriptivos usando Gemini
     */
    private void traducirDatosConGemini(Pajaro pajaro) {
        String textoCargando = "Translating with AI...";
        nombre.setText(textoCargando);
        nombreC.setText(textoCargando);
        orden.setText(textoCargando);
        familia.setText(textoCargando);
        desc.setText(textoCargando);
        identificacion.setText(textoCargando);
        canto.setText(textoCargando);
        habitat.setText(textoCargando);
        alimentacion.setText(textoCargando);

        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", "AIzaSyANg_tNiopmi8zYzN_JmV6r3J8bCrpiF-o");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Pedimos a Gemini que traduzca y separe todo con ||| para poder cortarlo luego
        // Añadimos también Nombre, Nombre Científico, Orden y Familia.
        String promptCompleto = "Translate the following 9 Spanish bird texts into English. Only return the translated texts separated by '|||' strictly in this order: Name ||| Scientific Name ||| Order ||| Family ||| Description ||| Identification ||| Song ||| Habitat ||| Diet. Here are the texts: "
                + pajaro.getNombre() + " ||| "
                + pajaro.getNombre_cientifico() + " ||| "
                + (pajaro.getOrden() != null ? pajaro.getOrden() : "—") + " ||| "
                + (pajaro.getFamilia() != null ? pajaro.getFamilia() : "—") + " ||| "
                + pajaro.getDesc() + " ||| "
                + pajaro.getIdentificacion() + " ||| "
                + pajaro.getCanto() + " ||| "
                + pajaro.getHabitat() + " ||| "
                + pajaro.getAlimentacion();

        Content content = new Content.Builder().addText(promptCompleto).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (result.getText() != null) {
                    String[] partes = result.getText().split("\\|\\|\\|");
                    if (partes.length >= 9) {
                        nombre.setText(partes[0].trim());
                        nombreC.setText(partes[1].trim());
                        orden.setText(partes[2].trim());
                        familia.setText(partes[3].trim());
                        desc.setText(partes[4].trim());
                        identificacion.setText(partes[5].trim());
                        canto.setText(partes[6].trim());
                        habitat.setText(partes[7].trim());
                        alimentacion.setText(partes[8].trim());
                    } else {
                        desc.setText("Translation format error from API");
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                nombre.setText(pajaro.getNombre());
                nombreC.setText(pajaro.getNombre_cientifico());
                orden.setText(pajaro.getOrden() != null ? pajaro.getOrden() : "—");
                familia.setText(pajaro.getFamilia() != null ? pajaro.getFamilia() : "—");
                desc.setText("Translation failed: " + t.getMessage());
                identificacion.setText(pajaro.getIdentificacion()); // Fallback a Español
                canto.setText(pajaro.getCanto());
                habitat.setText(pajaro.getHabitat());
                alimentacion.setText(pajaro.getAlimentacion());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /*
     * Busca grabaciones de audio del canto del pájaro en la API de iNaturalist.
     * Busca 10 observaciones verificadas (research grade) ordenadas por votos.
     * Prioriza archivos MP3 sobre WAV para mejor streaming.
     * Si encuentra audio, muestra el botón de reproducción.
     */
    private void buscarCantoEnINaturalist(String nombreCientifico) {
        System.out.println("Buscando canto para: " + nombreCientifico);

        INaturalistService service = INaturalistClient.getClient().create(INaturalistService.class);

        // Buscar 10 observaciones con sonido, calidad "research", ordenadas por votos
        service.searchObservationsWithSounds(nombreCientifico, true, 10, "research", "votes")
                .enqueue(new Callback<INaturalistObservationResponse>() {
                    @Override
                    public void onResponse(Call<INaturalistObservationResponse> call,
                            Response<INaturalistObservationResponse> response) {

                        if (response.isSuccessful() && response.body() != null
                                && response.body().results != null) {

                            String bestUrl = null;

                            // Recorrer todas las observaciones buscando el mejor audio
                            for (INaturalistObservationResponse.Observation obs : response.body().results) {
                                if (obs.sounds != null) {
                                    for (INaturalistObservationResponse.Sound sound : obs.sounds) {
                                        if (sound.fileUrl != null && !sound.fileUrl.isEmpty()) {
                                            // Preferir MP3 (mejor streaming que WAV)
                                            if (sound.fileContentType != null
                                                    && sound.fileContentType.contains("mp3")) {
                                                bestUrl = sound.fileUrl;
                                                break; // MP3 encontrado, usarlo
                                            }
                                            // Guardar primera URL válida como respaldo
                                            if (bestUrl == null) {
                                                bestUrl = sound.fileUrl;
                                            }
                                        }
                                    }
                                    // Si ya encontramos MP3, dejar de buscar
                                    if (bestUrl != null && bestUrl.contains("mp3")) {
                                        break;
                                    }
                                }
                            }

                            if (bestUrl != null) {
                                // Audio encontrado: guardar URL y mostrar botón
                                audioUrl = bestUrl;
                                System.out.println("Audio URL encontrada: " + audioUrl);
                                runOnUiThread(() -> btnPlayCanto.setVisibility(View.VISIBLE));
                            } else {
                                System.out.println("iNaturalist: No se encontró audio para " + nombreCientifico);
                            }
                        } else {
                            System.out.println("iNaturalist: Sin observaciones con audio para " + nombreCientifico);
                        }
                    }

                    @Override
                    public void onFailure(Call<INaturalistObservationResponse> call, Throwable t) {
                        System.out.println("Error buscando audio: " + t.getMessage());
                        t.printStackTrace();
                    }
                });
    }

    /*
     * Reproduce el audio del canto usando MediaPlayer en modo streaming.
     * Usa prepareAsync() para no bloquear el hilo principal mientras se descarga.
     * Muestra "Cargando..." mientras prepara el audio, luego "Detener" al
     * reproducir.
     */
    private void reproducirAudio() {
        if (audioUrl == null)
            return;

        try {
            // Liberar reproductor anterior si existe
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();

            // Configurar atributos de audio (tipo música, uso multimedia)
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build());

            // Establecer la URL del audio como fuente de datos
            mediaPlayer.setDataSource(audioUrl);

            // Mostrar estado de carga mientras se prepara
            btnPlayCanto.setText(R.string.btn_cargando);
            btnPlayCanto.setEnabled(false);

            // Callback cuando el audio está listo para reproducir
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                btnPlayCanto.setText(R.string.btn_detener);
                btnPlayCanto.setEnabled(true);
            });

            // Callback cuando termina la reproducción
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlayCanto.setText(R.string.btn_reproducir);
            });

            // Callback en caso de error durante la reproducción
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                System.out.println("Error MediaPlayer: " + what + " / " + extra);
                isPlaying = false;
                btnPlayCanto.setText(R.string.btn_reproducir);
                btnPlayCanto.setEnabled(true);
                Toast.makeText(FichaPajaro.this, R.string.error_reproducir_audio, Toast.LENGTH_SHORT).show();
                return true; // Error gestionado
            });

            // Preparar audio en segundo plano (no bloquea el hilo principal)
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_cargar_audio, Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Detiene la reproducción del audio y restaura el texto del botón.
     */
    private void detenerAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        isPlaying = false;
        btnPlayCanto.setText(R.string.btn_reproducir);
    }

    /*
     * Actualiza la estrella de Favoritos visualmente
     */
    private void actualizarIcono(boolean favorito) {
        if (btnFavorito == null) return;
        if (favorito) {
            btnFavorito.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            btnFavorito.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    /*
     * Método para eliminar un pájaro desde la ficha
     */
    private void eliminarPajaro(String nombre) {
        SupabaseService service = SupabaseClient.getClient().create(SupabaseService.class);
        service.deletePajaro("eq." + nombre).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FichaPajaro.this, "Pájaro eliminado correctamente", Toast.LENGTH_SHORT).show();
                    // Volver a ListaPajaros
                    Intent intent = new Intent(FichaPajaro.this, ListaPajaros.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(FichaPajaro.this, "Error al eliminar pájaro: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(FichaPajaro.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
     * Al destruir la Activity, liberar los recursos del MediaPlayer para evitar
     * fugas de memoria.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
