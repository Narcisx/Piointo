package com.example.proyectofinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

//Hereda de BaseActivity e implementa OnNavigationItemSelectedListener
public class AjustesActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SupabaseAuthManager authManager;
    private String userEmail, userToken;
    private SharedPreferences prefs;

    Toolbar toolbar;
    DrawerLayout drawerlayout;
    NavigationView navigationView;

    Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // El idioma se carga automáticamente en BaseActivity
        prefs = getSharedPreferences("MisAjustes", Context.MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        authManager = new SupabaseAuthManager();
        // Extraemos la sesión directamente de SharedPreferences para que funcione desde
        // cualquier pantalla
        userEmail = prefs.getString("USER_EMAIL", null);
        userToken = prefs.getString("USER_TOKEN", null);

        Button btnCambiarPass = findViewById(R.id.btnCambiarPassAjustes);
        Button btnBorrarCuenta = findViewById(R.id.btnBorrarCuenta);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        Switch swIdioma = findViewById(R.id.swIdioma);

        swIdioma.setChecked(idioma.equals("en"));

        Button btnVolver = findViewById(R.id.btnVolverAjustes);

        btnVolver.setOnClickListener(v -> {
            finish(); // Cierra esta pantalla y vuelve a la anterior
        });

        // pide el correo de recuperación si quieres cambiar la pass
        btnCambiarPass.setOnClickListener(v -> {
            if (userEmail == null) {
                Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
                return;
            }
            authManager.recuperarPassword(userEmail, new SupabaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> Toast
                            .makeText(AjustesActivity.this, "Correo enviado a " + userEmail, Toast.LENGTH_LONG).show());
                }

                @Override
                public void onError(String error) {
                }
            });
        });

        btnBorrarCuenta.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("¿Borrar cuenta?")
                    .setMessage("Esta acción no se puede deshacer. Tu cuenta será eliminada para siempre.")
                    .setPositiveButton("Borrar", (dialog, which) -> {

                        if (userToken == null) {
                            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        authManager.borrarCuenta(userToken, new SupabaseAuthManager.AuthCallback() {
                            @Override
                            public void onSuccess(String result) {
                                runOnUiThread(() -> {
                                    // También vaciamos la mochila local al borrar la cuenta
                                    prefs.edit().remove("USER_TOKEN").remove("USER_EMAIL").apply();

                                    Toast.makeText(AjustesActivity.this, "Cuenta eliminada correctamente",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AjustesActivity.this, LoginActivity.class));
                                    finish();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    // ver el error exacto en pantalla
                                    Log.e("BORRAR_ERROR", error);
                                    Toast.makeText(AjustesActivity.this, "Error al borrar: " + error, Toast.LENGTH_LONG)
                                            .show();
                                });
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Guarda el nuevo idioma y reinicia para que cambie de golpe
        swIdioma.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String nuevoIdioma = isChecked ? "en" : "es";
            if (!nuevoIdioma.equals(prefs.getString("idioma", "es"))) {
                prefs.edit().putString("idioma", nuevoIdioma).apply();

                Intent intent = new Intent(this, AjustesActivity.class);
                // Ya no hace falta pasar los extras por intent porque los lee de las prefs
                finish();
                startActivity(intent);
            }
        });

        // Te devuelve al inicio si decides salir
        btnCerrarSesion.setOnClickListener(v -> {
            prefs.edit().remove("USER_TOKEN").remove("USER_EMAIL").apply(); // Borramos sesión guardada
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Configuración de la hamburguesa
        toolbar = findViewById(R.id.toolbar);
        drawerlayout = findViewById(R.id.main_ajustes);
        navigationView = findViewById(R.id.nav_view_ajustes);

        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerlayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerlayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Ajuste de márgenes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contenido_principal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Vuelve a la pantalla anterior
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_pajaros) {
            Intent intent = new Intent(AjustesActivity.this, ListaPajaros.class);
            startActivity(intent);
        } else if (id == R.id.nav_inicio) {
            Intent intent = new Intent(AjustesActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_agra) {
            Intent intent = new Intent(AjustesActivity.this, AgradecimientosActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ia) {
            Intent intent = new Intent(AjustesActivity.this, ChatIA.class);
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(AjustesActivity.this, InformacionActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ayuda) {
            Intent intent = new Intent(AjustesActivity.this, ayuda.class);
            startActivity(intent);
        }
        drawerlayout.closeDrawer(GravityCompat.START);
        return true;
    }

}
