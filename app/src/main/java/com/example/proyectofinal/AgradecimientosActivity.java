package com.example.proyectofinal;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

//Hereda de BaseActivity para aplicar el idioma correcto
public class AgradecimientosActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agradecimientos);

        Button btnVolver = findViewById(R.id.btnVolverAgradecimientos);

        btnVolver.setOnClickListener(v -> {
            finish(); // Cierra esta pantalla y vuelve a la anterior
        });
    }
}
