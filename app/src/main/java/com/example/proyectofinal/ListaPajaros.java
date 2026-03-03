package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

import androidx.core.content.ContextCompat;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Locale;

/*
 * Activity que muestra la lista de pájaros en un GridView.
 * Carga los datos desde Supabase y obtiene las imágenes de iNaturalist.
 */
public class ListaPajaros extends BaseActivity {

    GridView listaPajaros;
    Toolbar toolbar;
    SearchView searchView;
    ImageButton btnFiltros;
    
    // Variables para búsqueda y filtros
    List<Pajaro> listaCompleta = new ArrayList<>();
    PajaroAdapter adapter;
    
    // Estados actuales de los filtros
    String textoBuscado = "";
    String filtroFamilia = "Todas";
    String filtroOrden = "Todos";
    float maximaLongitud = -1;
    float maximaEnvergadura = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_pajaros);

        listaPajaros = findViewById(R.id.listaPajaros);
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.svBuscarPajaros);
        btnFiltros = findViewById(R.id.btnFiltrosPajaros);

        // Configurar toolbar con botón de retroceso
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.lista_pajaros_titulo);
        }
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(ListaPajaros.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Crear lista vacía y adaptador (se rellenarán cuando lleguen los datos)
        adapter = new PajaroAdapter(this, listaCompleta);
        listaPajaros.setAdapter(adapter);
        
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

        // Cargar pájaros desde Supabase
        SupabaseService service = SupabaseClient.getClient().create(SupabaseService.class);

        service.getPajaros().enqueue(new Callback<List<Pajaro>>() {
            @Override
            public void onResponse(Call<List<Pajaro>> call, Response<List<Pajaro>> response) {

                System.out.println("Código HTTP: " + response.code());

                if (response.body() != null) {
                    System.out.println("Tamaño lista: " + response.body().size());
                } else {
                    System.out.println("Body es NULL");
                }

                if (response.isSuccessful() && response.body() != null) {

                    listaCompleta = response.body();

                    // Comprobar si debemos traducir los nombres (Inglés)
                    String idiomaActual = Locale.getDefault().getLanguage();
                    if (idiomaActual.equals(new Locale("en").getLanguage())) {
                        traducirNombresListaPajaros(listaCompleta, adapter);
                    } else {
                        // Español: Actualizar el adaptador con la lista recibida tal cual
                        adapter.setLista(new ArrayList<>(listaCompleta));
                    }

                    // Para cada pájaro, buscar su imagen en iNaturalist
                    for (Pajaro p : listaCompleta) {
                        obtenerImagenYActualizar(p);
                    }
                } else {
                    System.out.println("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Pajaro>> call, Throwable t) {
                t.printStackTrace();
            }
        });

        // Al pulsar un pájaro en la lista, abrir su ficha detallada
        listaPajaros.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pajaro pajaro = (Pajaro) parent.getItemAtPosition(position);
                Intent intent = new Intent(ListaPajaros.this, FichaPajaro.class);
                // Pasar el objeto Pajaro completo a la siguiente pantalla
                intent.putExtra("pajaro", pajaro);
                startActivity(intent);
            }
        });

        // Ajustar padding para las barras del sistema (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }// onCreate

    /*
     * Envía todos los nombres a Gemini para traducirlos de golpe.
     * Al terminar, actualiza la lista local y notifica al Adapter.
     */
    private void traducirNombresListaPajaros(List<Pajaro> lista, PajaroAdapter adapter) {
        if (lista.isEmpty()) {
            adapter.setLista(new ArrayList<>(lista));
            return;
        }

        // 1. Recolectar nombres y preservar IDs para reasignar después
        StringBuilder nombresS = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            nombresS.append(lista.get(i).getNombre());
            nombresS.append(lista.get(i).getNombre_cientifico()); // Metemos también los nombres científicos
            if (i < lista.size() - 1) {
                nombresS.append(" ||| ");
            }
        }

        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", "AIzaSyANg_tNiopmi8zYzN_JmV6r3J8bCrpiF-o");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        String promptCompleto = "Translate the following Spanish bird names into English. Retain their scientific names exactly as they are. Only return the translated texts separated by '|||' strictly in the exact same order and amount as provided. Here are the texts: "
                + nombresS.toString();

        Content content = new Content.Builder().addText(promptCompleto).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (result.getText() != null) {
                    String[] partes = result.getText().split("\\|\\|\\|");
                    // Gemini debe devolver exactamente el mismo número de nombres
                    if (partes.length >= lista.size()) {
                        for (int i = 0; i < lista.size(); i++) {
                            // Cambiamos el nombre en el objeto en memoria
                            lista.get(i).setNombre(partes[i].trim());
                        }
                    } else {
                        System.out.println("Error de formato de Gemini: Faltan elementos de la lista");
                    }
                }
                adapter.setLista(new ArrayList<>(lista)); // Aplicamos lista traducida (o si falló la misma)
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("Error al traducir nombres de lista con Gemini: " + t.getMessage());
                adapter.setLista(new ArrayList<>(lista)); // Fallback: Español original
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /*
     * Implementación central para intersecar todos los filtros en memoria
     */
    private void aplicarFiltros() {
        if (listaCompleta == null || listaCompleta.isEmpty()) return;

        List<Pajaro> pajarosFiltrados = new ArrayList<>();

        for (Pajaro p : listaCompleta) {
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

        adapter.setLista(pajarosFiltrados);
    }

    /*
     * Extrae el mayor número de una cadena como "22-25 cm" o "30.5" y
     * comprueba si es <= a la restricción especificada. Retorna true si cumple.
     */
    private boolean esMedidaMenor(String medidaTexto, float limiteMaximo) {
        if (medidaTexto == null || medidaTexto.trim().isEmpty() || medidaTexto.equals("—")) {
            return false; // Si no hay datos, no cumple el filtro restrictivo
        }

        float maxEncontrado = -1;
        // Regex asume que los decimales pueden usar coma o punto
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

        // Extraer familias únicas
        Set<String> familiasList = new HashSet<>();
        familiasList.add("Todas");
        for (Pajaro p : listaCompleta) {
            if (p.getFamilia() != null && !p.getFamilia().isEmpty()) {
                familiasList.add(p.getFamilia());
            }
        }
        ArrayAdapter<String> famAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(familiasList));
        spinFamilia.setAdapter(famAdapter);
        spinFamilia.setSelection(((ArrayAdapter) spinFamilia.getAdapter()).getPosition(filtroFamilia));

        // Extraer ordenes únicos
        Set<String> ordenesList = new HashSet<>();
        ordenesList.add("Todos");
        for (Pajaro p : listaCompleta) {
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

    /*
     * Busca la imagen de un pájaro en iNaturalist usando su nombre científico.
     * Si la encuentra, actualiza la URL en el objeto local (en memoria).
     */
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

    /*
     * Fuerza la actualización visual de la lista en el hilo principal (UI thread).
     */
    private void notificarCambioUI() {
        runOnUiThread(() -> {
            if (listaPajaros.getAdapter() instanceof PajaroAdapter) {
                ((PajaroAdapter) listaPajaros.getAdapter()).notifyDataSetChanged();
            }
        });
    }

}// class
