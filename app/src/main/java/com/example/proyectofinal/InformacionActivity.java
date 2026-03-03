package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

// Hereda de BaseActivity para aplicar el idioma correctamente
public class InformacionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion);

        // Recuperamos datos por si la navegación continúa
        String email = getIntent().getStringExtra("USER_EMAIL");
        String token = getIntent().getStringExtra("USER_TOKEN");

        Button btnVolver = findViewById(R.id.btnVolverInfo);

        btnVolver.setOnClickListener(v -> {
            // Aquí puedes decidir si volver atrás o ir a Ajustes
            finish();
        });
    }
}