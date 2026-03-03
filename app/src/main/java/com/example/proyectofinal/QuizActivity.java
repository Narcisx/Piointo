package com.example.proyectofinal;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Activity del Quiz de Aves de Pinto.
 * Muestra 10 preguntas aleatorias de un banco de ~25 preguntas sobre las aves
 * que se pueden encontrar en la localidad de Pinto, Madrid.
 * Al finalizar muestra la puntuación obtenida.
 */
public class QuizActivity extends BaseActivity {

    // --- Vistas del layout ---
    private Toolbar toolbar;
    private TextView tvProgreso;
    private ProgressBar progressBar;
    private TextView tvPregunta;
    private Button btnOpcion1, btnOpcion2, btnOpcion3, btnOpcion4;
    private CardView cardPregunta, cardResultado;
    private View layoutOpciones;
    private TextView tvResultadoTitulo, tvPuntuacion, tvMensajeResultado;
    private Button btnVolverInicio;

    // --- Estado del quiz ---
    private List<PreguntaQuiz> preguntasSeleccionadas;
    private int preguntaActual = 0;
    private int aciertos = 0;
    private boolean respondida = false; // Evita doble pulsación

    // Total de preguntas por intento
    private static final int TOTAL_PREGUNTAS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // --- Enlazar vistas con el layout XML ---
        toolbar = findViewById(R.id.toolbarQuiz);
        tvProgreso = findViewById(R.id.tvProgreso);
        progressBar = findViewById(R.id.progressBarQuiz);
        tvPregunta = findViewById(R.id.tvPregunta);
        btnOpcion1 = findViewById(R.id.btnOpcion1);
        btnOpcion2 = findViewById(R.id.btnOpcion2);
        btnOpcion3 = findViewById(R.id.btnOpcion3);
        btnOpcion4 = findViewById(R.id.btnOpcion4);
        cardPregunta = findViewById(R.id.cardPregunta);
        cardResultado = findViewById(R.id.cardResultado);
        layoutOpciones = findViewById(R.id.layoutOpciones);
        tvResultadoTitulo = findViewById(R.id.tvResultadoTitulo);
        tvPuntuacion = findViewById(R.id.tvPuntuacion);
        tvMensajeResultado = findViewById(R.id.tvMensajeResultado);
        btnVolverInicio = findViewById(R.id.btnVolverInicio);

        // --- Configurar Toolbar con botón de retroceso ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.quiz_titulo));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // --- Preparar preguntas ---
        List<PreguntaQuiz> banco = crearBancoPreguntas();
        Collections.shuffle(banco);
        // Seleccionar las primeras 10 (o menos si el banco fuera más pequeño)
        int cantidad = Math.min(TOTAL_PREGUNTAS, banco.size());
        preguntasSeleccionadas = new ArrayList<>(banco.subList(0, cantidad));

        // Configurar barra de progreso
        progressBar.setMax(cantidad);
        progressBar.setProgress(0);

        // --- Asignar listeners a los botones ---
        View.OnClickListener listenerOpcion = v -> {
            if (respondida) return; // Ignorar si ya respondió
            respondida = true;

            Button botonPulsado = (Button) v;
            int indiceSeleccionado = -1;
            if (v.getId() == R.id.btnOpcion1) indiceSeleccionado = 0;
            else if (v.getId() == R.id.btnOpcion2) indiceSeleccionado = 1;
            else if (v.getId() == R.id.btnOpcion3) indiceSeleccionado = 2;
            else if (v.getId() == R.id.btnOpcion4) indiceSeleccionado = 3;

            PreguntaQuiz pregunta = preguntasSeleccionadas.get(preguntaActual);
            int correcta = pregunta.respuestaCorrecta;

            // Feedback visual: verde para la correcta, rojo si se falló
            Button[] botones = {btnOpcion1, btnOpcion2, btnOpcion3, btnOpcion4};

            if (indiceSeleccionado == correcta) {
                aciertos++;
                botonPulsado.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, android.R.color.holo_green_dark)));
            } else {
                // Marcar la pulsada en rojo
                botonPulsado.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, android.R.color.holo_red_dark)));
                // Marcar la correcta en verde
                botones[correcta].setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, android.R.color.holo_green_dark)));
            }

            // Esperar 1 segundo y pasar a la siguiente pregunta
            new Handler().postDelayed(() -> {
                preguntaActual++;
                if (preguntaActual < preguntasSeleccionadas.size()) {
                    mostrarPregunta();
                } else {
                    mostrarResultado();
                }
            }, 1000);
        };

        btnOpcion1.setOnClickListener(listenerOpcion);
        btnOpcion2.setOnClickListener(listenerOpcion);
        btnOpcion3.setOnClickListener(listenerOpcion);
        btnOpcion4.setOnClickListener(listenerOpcion);

        // Botón de volver al inicio tras ver el resultado
        btnVolverInicio.setOnClickListener(v -> {
            Intent intent = new Intent(QuizActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Mostrar la primera pregunta
        mostrarPregunta();
    }

    /*
     * Muestra la pregunta actual en pantalla y resetea los colores de los botones.
     */
    private void mostrarPregunta() {
        respondida = false;
        PreguntaQuiz pregunta = preguntasSeleccionadas.get(preguntaActual);

        // Actualizar progreso
        tvProgreso.setText(getString(R.string.quiz_pregunta_progreso, (preguntaActual + 1), preguntasSeleccionadas.size()));
        progressBar.setProgress(preguntaActual);

        // Mostrar texto de la pregunta
        tvPregunta.setText(pregunta.pregunta);

        // Mostrar las 4 opciones
        Button[] botones = {btnOpcion1, btnOpcion2, btnOpcion3, btnOpcion4};
        for (int i = 0; i < 4; i++) {
            botones[i].setText(pregunta.opciones[i]);
            // Resetear color al color primario de la app
            botones[i].setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.colorPrimary)));
        }
    }

    /*
     * Oculta la zona de preguntas y muestra la card de resultado con la puntuación.
     */
    private void mostrarResultado() {
        // Ocultar preguntas y opciones
        cardPregunta.setVisibility(View.GONE);
        layoutOpciones.setVisibility(View.GONE);

        // Completar barra de progreso
        progressBar.setProgress(preguntasSeleccionadas.size());
        tvProgreso.setText(getString(R.string.quiz_completado));

        // Mostrar card de resultado
        cardResultado.setVisibility(View.VISIBLE);
        tvPuntuacion.setText(aciertos + " / " + preguntasSeleccionadas.size());

        // Mensaje según la puntuación
        String mensaje;
        if (aciertos == preguntasSeleccionadas.size()) {
            mensaje = getString(R.string.quiz_msg_perfecto);
        } else if (aciertos >= 8) {
            mensaje = getString(R.string.quiz_msg_excelente);
        } else if (aciertos >= 6) {
            mensaje = getString(R.string.quiz_msg_bueno);
        } else if (aciertos >= 4) {
            mensaje = getString(R.string.quiz_msg_regular);
        } else {
            mensaje = getString(R.string.quiz_msg_mal);
        }
        tvMensajeResultado.setText(mensaje);
    }

    // ============================================================================================
    // Clase interna que representa una pregunta del quiz
    // ============================================================================================
    private static class PreguntaQuiz {
        String pregunta;
        String[] opciones; // Siempre 4 opciones
        int respuestaCorrecta; // Índice (0-3) de la opción correcta

        PreguntaQuiz(String pregunta, String[] opciones, int respuestaCorrecta) {
            this.pregunta = pregunta;
            this.opciones = opciones;
            this.respuestaCorrecta = respuestaCorrecta;
        }
    }

    // ============================================================================================
    // Banco de preguntas sobre aves de Pinto (Madrid)
    // Basado en la fauna oficial del Ayuntamiento y especies de la zona esteparia
    // ============================================================================================
    private List<PreguntaQuiz> crearBancoPreguntas() {
        List<PreguntaQuiz> banco = new ArrayList<>();

        // --- Avutarda (Otis tarda) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q1_q),
                getResources().getStringArray(R.array.quiz_q1_opts),
                0));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q2_q),
                getResources().getStringArray(R.array.quiz_q2_opts),
                1));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q3_q),
                getResources().getStringArray(R.array.quiz_q3_opts),
                1));

        // --- Perdiz Roja (Alectoris rufa) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q4_q),
                getResources().getStringArray(R.array.quiz_q4_opts),
                1));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q5_q),
                getResources().getStringArray(R.array.quiz_q5_opts),
                2));

        // --- Cigüeña Blanca (Ciconia ciconia) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q6_q),
                getResources().getStringArray(R.array.quiz_q6_opts),
                1));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q7_q),
                getResources().getStringArray(R.array.quiz_q7_opts),
                1));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q8_q),
                getResources().getStringArray(R.array.quiz_q8_opts),
                2));

        // --- Sisón Común (Tetrax tetrax) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q9_q),
                getResources().getStringArray(R.array.quiz_q9_opts),
                1));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q10_q),
                getResources().getStringArray(R.array.quiz_q10_opts),
                1));

        // --- Críalo (Clamator glandarius) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q11_q),
                getResources().getStringArray(R.array.quiz_q11_opts),
                1));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q12_q),
                getResources().getStringArray(R.array.quiz_q12_opts),
                1));

        // --- Jilguero (Carduelis carduelis) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q13_q),
                getResources().getStringArray(R.array.quiz_q13_opts),
                1));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q14_q),
                getResources().getStringArray(R.array.quiz_q14_opts),
                1));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q15_q),
                getResources().getStringArray(R.array.quiz_q15_opts),
                1));

        // --- Milano Real (Milvus milvus) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q16_q),
                getResources().getStringArray(R.array.quiz_q16_opts),
                1));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q17_q),
                getResources().getStringArray(R.array.quiz_q17_opts),
                2));

        // --- Aguilucho Pálido (Circus cyaneus) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q18_q),
                getResources().getStringArray(R.array.quiz_q18_opts),
                2));

        // --- Avefría Europea (Vanellus vanellus) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q19_q),
                getResources().getStringArray(R.array.quiz_q19_opts),
                1));

        // --- Busardo Ratonero (Buteo buteo) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q20_q),
                getResources().getStringArray(R.array.quiz_q20_opts),
                0));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q21_q),
                getResources().getStringArray(R.array.quiz_q21_opts),
                2));

        // --- Cernícalo Vulgar (Falco tinnunculus) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q22_q),
                getResources().getStringArray(R.array.quiz_q22_opts),
                1));

        // --- Mochuelo Europeo (Athene noctua) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q23_q),
                getResources().getStringArray(R.array.quiz_q23_opts),
                2));

        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q24_q),
                getResources().getStringArray(R.array.quiz_q24_opts),
                1));

        // --- Cogujada Común (Galerida cristata) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q25_q),
                getResources().getStringArray(R.array.quiz_q25_opts),
                1));

        // --- Gorrión Común (Passer domesticus) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q26_q),
                getResources().getStringArray(R.array.quiz_q26_opts),
                1));

        // --- Paloma Torcaz (Columba palumbus) ---
        banco.add(new PreguntaQuiz(
                getString(R.string.quiz_q27_q),
                getResources().getStringArray(R.array.quiz_q27_opts),
                1));

        return banco;
    }
}
