package com.example.proyectofinal; //Se cambió el paquete para integrarlo en el proyecto principal

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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

//Hereda de BaseActivity para aplicar el idioma correcto
public class prueba2 extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    Button buton;
    Toolbar toolbar;
    DrawerLayout drawerlayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_prueba2);
        // este codigo es necesario para implementar el menu lateral
        toolbar = findViewById(R.id.toolbar);
        drawerlayout = findViewById(R.id.main_prueba);// aqui cambia la id
        navigationView = findViewById(R.id.nav_view);
        // para que funcione en todas las pantallas
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

    // Codigo necesario para
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Vuelve a la pantalla anterior
        return true;
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_ia) {
            Intent intent = new Intent(prueba2.this, ChatIA.class);
            startActivity(intent);
        } else if (id == R.id.nav_inicio) {
            Intent intent = new Intent(prueba2.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ajustes) {
            Intent intent = new Intent(prueba2.this, AjustesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_agra) {
            Intent intent = new Intent(prueba2.this, AgradecimientosActivity.class);
            startActivity(intent);
        }
        drawerlayout.closeDrawer(GravityCompat.START);
        return true;

    }
}
