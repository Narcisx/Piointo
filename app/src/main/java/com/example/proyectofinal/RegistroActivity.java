package com.example.proyectofinal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.R;

public class RegistroActivity extends BaseActivity {

    private SupabaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        authManager = new SupabaseAuthManager();

        EditText etRegEmail = findViewById(R.id.etRegEmail);
        EditText etRegPassword = findViewById(R.id.etRegPassword);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(v -> {
            String email = etRegEmail.getText().toString().trim();
            String password = etRegPassword.getText().toString().trim();

            if (email.isEmpty() || password.length() < 6) {
                Toast.makeText(this, "Email inválido o contraseña muy corta", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.registrarUsuario(email, password, new SupabaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegistroActivity.this, "Registro completado. Ya puedes iniciar sesión.",
                                Toast.LENGTH_LONG).show();
                        finish(); // Cierra esta pantalla y vuelve al Login
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(RegistroActivity.this, "Error al registrar", Toast.LENGTH_LONG)
                            .show());
                }
            });
        });
    }
}
