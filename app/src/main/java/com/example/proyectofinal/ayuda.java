package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class ayuda extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    Toolbar toolbar;
    DrawerLayout drawerlayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ayuda);

        toolbar = findViewById(R.id.toolbar);
        drawerlayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerlayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerlayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contenido_principal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    @Override
    public void onBackPressed() {
        // Si la barra de navegación estaba abierta
        if (drawerlayout.isDrawerOpen(GravityCompat.START)) {
            // Cierra la barra de navegación
            drawerlayout.closeDrawer(GravityCompat.START);
        } else {
            // Si no estaba abierta se hace la accion por defecto de Android
            super.onBackPressed();
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Consigo la ID del item del menu clickado
        int id = menuItem.getItemId();

        // Depende de que id clickemos, hacemos un intent.
        if (id == R.id.nav_pajaros) {
            // Redirigir a la Lista de Pájaros fusionada
            Intent intent = new Intent(ayuda.this, ListaPajaros.class);
            startActivity(intent);
        } else if (id == R.id.nav_ia) {
            Intent intent = new Intent(ayuda.this, ChatIA.class);
            startActivity(intent);
        } else if (id == R.id.nav_ajustes) {
            Intent intent = new Intent(ayuda.this, AjustesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_agra) {
            Intent intent = new Intent(ayuda.this, AgradecimientosActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(ayuda.this, InformacionActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_inicio) {
            Intent intent = new Intent(ayuda.this, MainActivity.class);
            startActivity(intent);
        }
        // Cierro la barra de navegación
        drawerlayout.closeDrawer(GravityCompat.START);
        return true;
    }
}