package com.example.proyectofinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaFavoritos extends BaseActivity {
    GridView gridFavoritos;
    PajaroAdapter adapter;
    List<Pajaro> todosLosPajaros = new ArrayList<>();
    
    // Variables para búsqueda y filtros
    List<Pajaro> listaFavoritosActual = new ArrayList<>();
    SearchView searchView;
    ImageButton btnFiltros;
    String textoBuscado = "";
    String filtroFamilia = "Todas";
    String filtroOrden = "Todos";
    float maximaLongitud = -1;
    float maximaEnvergadura = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_favoritos);

        gridFavoritos = findViewById(R.id.gridFavoritos);
        Toolbar toolbar = findViewById(R.id.toolbarFavoritos);
        searchView = findViewById(R.id.svBuscarFavoritos);
        btnFiltros = findViewById(R.id.btnFiltrosFavoritos);
        
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mis Favoritos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        gridFavoritos.setOnItemClickListener((adapterView, view, position, id) -> {
            Pajaro pajaroSeleccionado = (Pajaro) adapter.getItem(position);
            Intent intent = new Intent(ListaFavoritos.this, FichaPajaro.class);
            intent.putExtra("pajaro", pajaroSeleccionado);
            startActivity(intent);
        });

        // Funcionalidad del Buscador
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                textoBuscado = newText.toLowerCase();
                aplicarFiltros();
                return true;
            }
        });

        // Funcionalidad del Menú de Filtros
        btnFiltros.setOnClickListener(v -> mostrarDialogoFiltros());

        // Solo cargamos los datos de la red una vez al crear la actividad
        cargarDatosDesdeSupabase();
    }

    private void cargarDatosDesdeSupabase() {
        SupabaseService service = SupabaseClient.getClient().create(SupabaseService.class);
        service.getPajaros().enqueue(new Callback<List<Pajaro>>() {
            @Override
            public void onResponse(Call<List<Pajaro>> call, Response<List<Pajaro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todosLosPajaros = response.body();
                    // Una vez tenemos los datos, filtramos y buscamos imágenes
                    cargarFavoritos();
                }
            }
            @Override
            public void onFailure(Call<List<Pajaro>> call, Throwable t) { }
        });
    }

    public void cargarFavoritos() {
        SharedPreferences prefs = getSharedPreferences("Favoritos", MODE_PRIVATE);
        listaFavoritosActual.clear();

        for (Pajaro p : todosLosPajaros) {
            // Verificamos el estado actual en SharedPreferences
            if (prefs.getBoolean(p.getNombre(), false)) {
                listaFavoritosActual.add(p);
                // Solo buscamos la imagen si no la tiene ya
                if (p.getImagen_url() == null) {
                    obtenerImagenYActualizar(p);
                }
            }
        }

        // Aplicamos los filtros actuales sobre la lista de favoritos
        aplicarFiltros();
    }

    /*
     * Implementación central para intersecar todos los filtros en memoria
     */
    private void aplicarFiltros() {
        if (listaFavoritosActual == null || listaFavoritosActual.isEmpty()) {
            if (adapter != null) {
                adapter.setLista(new ArrayList<>());
            } else {
                adapter = new PajaroAdapter(this, new ArrayList<>());
                gridFavoritos.setAdapter(adapter);
            }
            return;
        }

        List<Pajaro> pajarosFiltrados = new ArrayList<>();

        for (Pajaro p : listaFavoritosActual) {
            // Filtrar por texto (nombre común o científico)
            boolean coincideNombre = p.getNombre() != null && p.getNombre().toLowerCase().contains(textoBuscado);
            boolean coincideCientifico = p.getNombre_cientifico() != null && p.getNombre_cientifico().toLowerCase().contains(textoBuscado);

            if (!textoBuscado.isEmpty() && !coincideNombre && !coincideCientifico) {
                continue;
            }

            // Filtrar por familia
            if (!filtroFamilia.equals("Todas") && (p.getFamilia() == null || !p.getFamilia().equals(filtroFamilia))) {
                continue;
            }

            // Filtrar por orden
            if (!filtroOrden.equals("Todos") && (p.getOrden() == null || !p.getOrden().equals(filtroOrden))) {
                continue;
            }

            // Filtrar por longitud y envergadura usando expresiones regulares
            if (maximaLongitud > 0 && !esMedidaMenor(p.getLongitud(), maximaLongitud)) {
                continue;
            }

            if (maximaEnvergadura > 0 && !esMedidaMenor(p.getEnvergadura(), maximaEnvergadura)) {
                continue;
            }

            pajarosFiltrados.add(p);
        }

        if (adapter == null) {
            adapter = new PajaroAdapter(this, pajarosFiltrados);
            gridFavoritos.setAdapter(adapter);
        } else {
            adapter.setLista(pajarosFiltrados);
        }
    }

    /*
     * Extrae el mayor número de una cadena como "22-25 cm" o "30.5" y
     * comprueba si es <= a la restricción especificada. Retorna true si cumple.
     */
    private boolean esMedidaMenor(String medidaTexto, float limiteMaximo) {
        if (medidaTexto == null || medidaTexto.trim().isEmpty() || medidaTexto.equals("—")) {
            return false;
        }

        float maxEncontrado = -1;
        Matcher matcher = Pattern.compile("(\\d+([.,]\\d+)?)").matcher(medidaTexto);

        while (matcher.find()) {
            try {
                String numeroStr = matcher.group(1).replace(",", ".");
                float valor = Float.parseFloat(numeroStr);
                if (valor > maxEncontrado) {
                    maxEncontrado = valor;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (maxEncontrado == -1) return false;
        return maxEncontrado <= limiteMaximo;
    }

    /*
     * Muestra el diálogo desplegable con los filtros avanzados
     */
    private void mostrarDialogoFiltros() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filtros, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Spinner spinFamilia = dialogView.findViewById(R.id.spinFamilia);
        Spinner spinOrden = dialogView.findViewById(R.id.spinOrden);
        EditText etLongitud = dialogView.findViewById(R.id.etFiltroLongitud);
        EditText etEnvergadura = dialogView.findViewById(R.id.etFiltroEnvergadura);
        Button btnLimpiar = dialogView.findViewById(R.id.btnFiltroLimpiar);
        Button btnAplicar = dialogView.findViewById(R.id.btnFiltroAplicar);

        // Extraer familias únicas de mis FAVORITOS
        Set<String> familiasList = new HashSet<>();
        familiasList.add("Todas");
        for (Pajaro p : listaFavoritosActual) {
            if (p.getFamilia() != null && !p.getFamilia().isEmpty()) {
                familiasList.add(p.getFamilia());
            }
        }
        ArrayAdapter<String> famAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(familiasList));
        spinFamilia.setAdapter(famAdapter);
        spinFamilia.setSelection(((ArrayAdapter) spinFamilia.getAdapter()).getPosition(filtroFamilia));

        // Extraer ordenes únicos de mis FAVORITOS
        Set<String> ordenesList = new HashSet<>();
        ordenesList.add("Todos");
        for (Pajaro p : listaFavoritosActual) {
            if (p.getOrden() != null && !p.getOrden().isEmpty()) {
                ordenesList.add(p.getOrden());
            }
        }
        ArrayAdapter<String> ordAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(ordenesList));
        spinOrden.setAdapter(ordAdapter);
        spinOrden.setSelection(((ArrayAdapter) spinOrden.getAdapter()).getPosition(filtroOrden));

        // Cargar medidas guardadas
        if (maximaLongitud > 0) etLongitud.setText(String.valueOf((int) maximaLongitud));
        if (maximaEnvergadura > 0) etEnvergadura.setText(String.valueOf((int) maximaEnvergadura));

        btnLimpiar.setOnClickListener(v -> {
            filtroFamilia = "Todas";
            filtroOrden = "Todos";
            maximaLongitud = -1;
            maximaEnvergadura = -1;
            searchView.setQuery("", false);
            dialog.dismiss();
            aplicarFiltros();
        });

        btnAplicar.setOnClickListener(v -> {
            filtroFamilia = spinFamilia.getSelectedItem().toString();
            filtroOrden = spinOrden.getSelectedItem().toString();

            String tLongitud = etLongitud.getText().toString();
            String tEnvergadura = etEnvergadura.getText().toString();

            maximaLongitud = tLongitud.isEmpty() ? -1 : Float.parseFloat(tLongitud);
            maximaEnvergadura = tEnvergadura.isEmpty() ? -1 : Float.parseFloat(tEnvergadura);

            dialog.dismiss();
            aplicarFiltros();
        });

        dialog.show();
    }

    // Copiamos la lógica de búsqueda de imágenes de ListaPajaros.java
    private void obtenerImagenYActualizar(Pajaro pajaro) {
        String nombreCientifico = pajaro.getNombre_cientifico();
        if (nombreCientifico == null || nombreCientifico.trim().isEmpty()) {
            System.out.println("Nombre científico vacío para: " + pajaro.getNombre());
            return;
        }

        System.out.println("iNaturalist: Buscando: " + nombreCientifico);

        // Crear servicio de iNaturalist
        INaturalistService iNatService = INaturalistClient.getClient().create(INaturalistService.class);

        // Buscar la especie por nombre científico (máximo 5 resultados)
        iNatService.searchTaxa(nombreCientifico.trim(), "species", 5)
                .enqueue(new Callback<INaturalistResponse>() {

                    @Override
                    public void onResponse(Call<INaturalistResponse> call, Response<INaturalistResponse> response) {

                        if (response.isSuccessful() && response.body() != null
                                && response.body().results != null && !response.body().results.isEmpty()) {

                            // Buscar un resultado cuyo nombre científico coincida exactamente
                            for (INaturalistResponse.Result result : response.body().results) {
                                if (result.name != null
                                        && result.name.equalsIgnoreCase(nombreCientifico.trim())
                                        && result.defaultPhoto != null
                                        && result.defaultPhoto.mediumUrl != null) {

                                    String imageUrl = result.defaultPhoto.mediumUrl;
                                    System.out.println("iNaturalist URL encontrada: " + imageUrl);

                                    // Guardar URL en el objeto local (en memoria)
                                    pajaro.setImagen_url(imageUrl);
                                    notificarCambioUI();
                                    return; // Coincidencia encontrada, salir
                                }
                            }

                            System.out.println("iNaturalist: No hay coincidencia exacta para " + nombreCientifico);
                        } else {
                            System.out.println("iNaturalist: Sin resultados para " + nombreCientifico);
                        }
                    }

                    @Override
                    public void onFailure(Call<INaturalistResponse> call, Throwable t) {
                        System.out.println("iNaturalist: Error de red para " + nombreCientifico);
                        t.printStackTrace();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cada vez que volvemos a la pantalla, refrescamos los favoritos localmente
        if (todosLosPajaros != null && !todosLosPajaros.isEmpty()) {
            cargarFavoritos();
        }
    }

    private void notificarCambioUI() {
        runOnUiThread(() -> {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }
}