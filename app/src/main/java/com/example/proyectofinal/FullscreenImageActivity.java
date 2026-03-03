package com.example.proyectofinal;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/*
 * Activity que muestra una foto a pantalla completa.
 * Se abre al pulsar una foto en la galería.
 */
//Hereda de BaseActivity para aplicar el idioma
public class FullscreenImageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        // Vincular vistas
        ImageView imgFullscreen = findViewById(R.id.imgFullscreen);
        ImageButton btnCerrar = findViewById(R.id.btnCerrar);

        // Obtener la URL de la imagen enviada desde la galería
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Cargar imagen en resolución original con Glide
        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery) // Icono mientras carga
                    .error(android.R.drawable.ic_dialog_alert) // Icono si hay error
                    .into(imgFullscreen);
        }

        // Cerrar al pulsar el botón ✕
        btnCerrar.setOnClickListener(v -> finish());

        // También cerrar al pulsar la imagen
        imgFullscreen.setOnClickListener(v -> finish());
    }
}
