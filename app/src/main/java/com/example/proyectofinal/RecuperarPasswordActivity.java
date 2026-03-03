package com.example.proyectofinal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;

public class RecuperarPasswordActivity extends BaseActivity {

    private SupabaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_password);

        authManager = new SupabaseAuthManager();

        EditText etRecEmail = findViewById(R.id.etRecEmail);
        Button btnRecuperar = findViewById(R.id.btnRecuperar);

        btnRecuperar.setOnClickListener(v -> {
            String email = etRecEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce tu correo", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.recuperarPassword(email, new SupabaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        Toast.makeText(RecuperarPasswordActivity.this, "Correo enviado si la cuenta existe.",
                                Toast.LENGTH_LONG).show();
                        finish(); // Vuelve al Login
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast
                            .makeText(RecuperarPasswordActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
                }
            });
        });
    }
}
