package com.example.proyectofinal; //Se cambió el paquete para integrarlo en el proyecto principal

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.bumptech.glide.Glide;

// Hereda de BaseActivity para aplicar el idioma correcto
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Creamos las variables de cada uno de los elementos que tengamos que utilizar
    Toolbar toolbar;
    DrawerLayout drawerlayout;
    NavigationView navigationView;
    CardView cardView1, cardView2, cardView3, cardView4;
    ImageView imgCarousel1, imgCarousel2, imgCarousel3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Enlazamos las ids con los elementos de la app
        toolbar = findViewById(R.id.toolbar);
        drawerlayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);
        cardView1 = findViewById(R.id.cardview1);
        cardView2 = findViewById((R.id.cardview2));
        cardView3 = findViewById(R.id.cardview3);
        cardView4 = findViewById(R.id.cardview4);
        imgCarousel1 = findViewById(R.id.carrusel_img1);
        imgCarousel2 = findViewById(R.id.carrusel_img2);
        imgCarousel3 = findViewById(R.id.carrusel_img3);

        // Hago que mi diseño del toolbar sea el que se utilice
        setSupportActionBar(toolbar);
        // Hacemos que la barra de navegación siempre este por encima de los demás
        // elementos
        navigationView.bringToFront();

        // Con esto conectamos el drawerlayout y la barra superior con la pantalla. Crea la hamburguesa
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerlayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        // Con esto sabemos cuando se abre y cierra la barra de navegación, crea la
        // animacion de la hamburguesa
        drawerlayout.addDrawerListener(toggle);
        // Sincroniza el icono de la hamburguesa para cambiarlo si esta cerrada o
        // abierta
        toggle.syncState();
        // Hacemos que los clicks que se hacen en la barra de navegación se escuchen por
        // la app y podamos hacerla funcionar
        // Gracias a eso podemos hacer un onNavigationItemSelected
        navigationView.setNavigationItemSelectedListener(this);

        // Falta la pantalla lista
        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirigir a la Lista de Pájaros fusionada
                Intent intent = new Intent(MainActivity.this, ListaPajaros.class);
                startActivity(intent);
            }
        });
        // CardView2 todavia falta la pantalla de añadir
        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirige a la nueva pantalla Añadir Pájaro
                Intent intent = new Intent(MainActivity.this, AnadirPajaroActivity.class);
                startActivity(intent);
            }
        });

        // Navegar a Favoritos
        cardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ListaFavoritos.class);
                startActivity(intent);
            }
        });

        // Navegar al Quiz
        cardView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contenido_principal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cargar fotos reales de los pajaros desde la API
        loadCarouselFromApi();
    }

    private void loadCarouselFromApi() {
        SupabaseService service = SupabaseClient.getClient().create(SupabaseService.class);
        service.getPajaros().enqueue(new Callback<List<Pajaro>>() {
            @Override
            public void onResponse(Call<List<Pajaro>> call, Response<List<Pajaro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pajaro> lista = response.body();
                    if (!lista.isEmpty()) {
                        // Barajamos la lista de objetos Pajaro obtenida de la base de datos
                        Collections.shuffle(lista);

                        // Limitamos a los primeros 3 (o menos si hay pocos pajaros)
                        int size = Math.min(3, lista.size());
                        ImageView[] carousels = { imgCarousel1, imgCarousel2, imgCarousel3 };

                        INaturalistService iNatService = INaturalistClient.getClient().create(INaturalistService.class);

                        for (int i = 0; i < size; i++) {
                            Pajaro p = lista.get(i);
                            ImageView targetView = carousels[i];
                            String sciName = p.getNombre_cientifico();

                            if (sciName != null && !sciName.trim().isEmpty()) {
                                iNatService.searchTaxa(sciName.trim(), "species", 5)
                                        .enqueue(new Callback<INaturalistResponse>() {
                                            @Override
                                            public void onResponse(Call<INaturalistResponse> call,
                                                    Response<INaturalistResponse> responseInat) {
                                                if (responseInat.isSuccessful() && responseInat.body() != null
                                                        && responseInat.body().results != null
                                                        && !responseInat.body().results.isEmpty()) {

                                                    for (INaturalistResponse.Result result : responseInat
                                                            .body().results) {
                                                        if (result.name != null
                                                                && result.name.equalsIgnoreCase(sciName.trim())
                                                                && result.defaultPhoto != null
                                                                && result.defaultPhoto.mediumUrl != null) {

                                                            String imageUrl = result.defaultPhoto.mediumUrl;
                                                            runOnUiThread(() -> Glide.with(MainActivity.this)
                                                                    .load(imageUrl)
                                                                    .placeholder(R.drawable.icono)
                                                                    .into(targetView));
                                                            break;
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<INaturalistResponse> call, Throwable t) {
                                                // Si falla, se queda el drawable local
                                            }
                                        });
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Pajaro>> call, Throwable t) {
                // Si falla, se quedan los drawables locales
                t.printStackTrace();
            }
        });
    }

    // codigo para cerrar el menu sin que se salga de la aplicación
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Consigo la ID del item del menu clickado
        int id = menuItem.getItemId();

        // Depende de que id clickemos, hacemos un intent.
        if (id == R.id.nav_pajaros) {
            // Redirigir a la Lista de Pájaros fusionada
            Intent intent = new Intent(MainActivity.this, ListaPajaros.class);
            startActivity(intent);
        } else if (id == R.id.nav_ia) {
            Intent intent = new Intent(MainActivity.this, ChatIA.class);
            startActivity(intent);
        } else if (id == R.id.nav_ajustes) {
            Intent intent = new Intent(MainActivity.this, AjustesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_agra) {
            Intent intent = new Intent(MainActivity.this, AgradecimientosActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(MainActivity.this, InformacionActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ayuda) {
            Intent intent = new Intent(MainActivity.this, ayuda.class);
            startActivity(intent);
        }
        // Cierro la barra de navegación
        drawerlayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
