package com.example.proyectofinal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CambiarPasswordActivity extends BaseActivity {

    private String accessToken;
    private SupabaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_password);

        authManager = new SupabaseAuthManager();
        EditText etNuevaPass = findViewById(R.id.etNuevaPass);
        Button btnConfirmar = findViewById(R.id.btnConfirmarCambio);

        // Capturar token del fragmento (#access_token=...)
        Uri data = getIntent().getData();
        if (data != null && data.getFragment() != null) {
            String fragment = data.getFragment();
            if (fragment.contains("access_token=")) {
                accessToken = fragment.split("access_token=")[1].split("&")[0];
                Log.d("DEBUG", "Token recibido");
            }
        }

        btnConfirmar.setOnClickListener(v -> {
            String pass = etNuevaPass.getText().toString().trim();
            if (pass.length() < 6 || accessToken == null) {
                Toast.makeText(this, "Error o contraseña corta", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.actualizarPassword(pass, accessToken, new SupabaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        Toast.makeText(CambiarPasswordActivity.this, "¡Éxito!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CambiarPasswordActivity.this, LoginActivity.class));
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    /* Toast error */ }
            });
        });
    }
}
