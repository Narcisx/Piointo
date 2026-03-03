package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

// Hereda de BaseActivity para aplicar el idioma correcto
public class LoginActivity extends BaseActivity {

    private SupabaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bloque de autologin: Si ya hay credenciales, saltamos
        // MainActivity
        android.content.SharedPreferences prefs = getSharedPreferences("MisAjustes",
                android.content.Context.MODE_PRIVATE);
        String savedToken = prefs.getString("USER_TOKEN", null);
        String savedEmail = prefs.getString("USER_EMAIL", null);

        if (savedToken != null && savedEmail != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USER_EMAIL", savedEmail);
            intent.putExtra("USER_TOKEN", savedToken);
            startActivity(intent);
            finish();
            return; // Detenemos la creación de la UI de login
        }

        setContentView(R.layout.activity_login);

        authManager = new SupabaseAuthManager();
        // Busco los campos en el XML
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegistrarse = findViewById(R.id.tvRegistrarse);
        TextView tvOlvidastePassword = findViewById(R.id.tvOlvidastePassword);

        tvRegistrarse.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
        tvOlvidastePassword.setOnClickListener(v -> startActivity(new Intent(this, RecuperarPasswordActivity.class)));

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            // Intenta iniciar sesiony, si sale bien, "desmonto" el JSON para sacar el token
            authManager.iniciarSesion(email, password, new SupabaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        try {
                            // Extraemos los datos necesarios para que Ajustes funcione
                            JSONObject jsonResponse = new JSONObject(result);
                            String token = jsonResponse.getString("access_token");
                            String userEmail = jsonResponse.getJSONObject("user").getString("email");

                            //  Guardar las credenciales en SharedPreferences para la próxima
                            // vez
                            android.content.SharedPreferences prefs = getSharedPreferences("MisAjustes",
                                    android.content.Context.MODE_PRIVATE);
                            prefs.edit()
                                    .putString("USER_TOKEN", token)
                                    .putString("USER_EMAIL", userEmail)
                                    .apply();

                            // guarda esos datos en el "mochila" (intent) y te lleva a Ajustes
                            // (borrado) Intent intent = new Intent(LoginActivity.this,
                            // AgradecimientosActivity.class);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            // Se cambia AgradecimientosActivity por MainActivity para conectar los proyectos
                            intent.putExtra("USER_EMAIL", userEmail);
                            intent.putExtra("USER_TOKEN", token);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Log.e("ERROR_JSON", e.getMessage());
                            Toast.makeText(LoginActivity.this, "Error al procesar el login", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(
                            () -> Toast
                                    .makeText(LoginActivity.this, "Error: Credenciales incorrectas", Toast.LENGTH_LONG)
                                    .show());
                }
            });
        });
    }
}
