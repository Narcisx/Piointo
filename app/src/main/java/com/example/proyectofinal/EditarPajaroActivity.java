package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPajaroActivity extends BaseActivity {
    private EditText etNombre, etCientifico, etOrden, etFamilia, etLongitud, etEnvergadura, etIdentificacion, etHabitat, etAlimentacion, etDesc;
    private Button btnGuardar;
    private Pajaro pajaroOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_pajaro);

        // Usamos las IDs de activity_anadir_pajaro que se copiaron a activity_editar_pajaro
        etNombre = findViewById(R.id.etNuevoNombre);
        etCientifico = findViewById(R.id.etNuevoCientifico);
        etOrden = findViewById(R.id.etNuevoOrden);
        etFamilia = findViewById(R.id.etNuevaFamilia);
        etLongitud = findViewById(R.id.etNuevaLongitud);
        etEnvergadura = findViewById(R.id.etNuevaEnvergadura);
        etIdentificacion = findViewById(R.id.etNuevaIdentificacion);
        etHabitat = findViewById(R.id.etNuevoHabitat);
        etAlimentacion = findViewById(R.id.etNuevaAlimentacion);
        etDesc = findViewById(R.id.etNuevaDesc);
        btnGuardar = findViewById(R.id.btnGuardarEdicion);

        pajaroOriginal = (Pajaro) getIntent().getSerializableExtra("pajaro");
        if (pajaroOriginal != null) {
            etNombre.setText(pajaroOriginal.getNombre() != null ? pajaroOriginal.getNombre() : "");
            etCientifico.setText(pajaroOriginal.getNombre_cientifico() != null ? pajaroOriginal.getNombre_cientifico() : "");
            etOrden.setText(pajaroOriginal.getOrden() != null ? pajaroOriginal.getOrden() : "");
            etFamilia.setText(pajaroOriginal.getFamilia() != null ? pajaroOriginal.getFamilia() : "");
            etLongitud.setText(pajaroOriginal.getLongitud() != null ? pajaroOriginal.getLongitud() : "");
            etEnvergadura.setText(pajaroOriginal.getEnvergadura() != null ? pajaroOriginal.getEnvergadura() : "");
            etIdentificacion.setText(pajaroOriginal.getIdentificacion() != null ? pajaroOriginal.getIdentificacion() : "");
            etHabitat.setText(pajaroOriginal.getHabitat() != null ? pajaroOriginal.getHabitat() : "");
            etAlimentacion.setText(pajaroOriginal.getAlimentacion() != null ? pajaroOriginal.getAlimentacion() : "");
            etDesc.setText(pajaroOriginal.getDesc() != null ? pajaroOriginal.getDesc() : "");
        } else {
            Toast.makeText(this, "Error al cargar datos del pájaro", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es requerido");
            return;
        }

        Pajaro pajaroActualizado = new Pajaro();
        
        // (Solución Error 400) Supabase rechaza el PATCH si enviamos { id: null } a una Primary Key.
        // Debemos inyectar el ID real del pájaro original.
        if (pajaroOriginal != null && pajaroOriginal.getId() != null) {
            pajaroActualizado.setId(pajaroOriginal.getId());
        }
        
        pajaroActualizado.setNombre(nombre);
        pajaroActualizado.setNombre_cientifico(etCientifico.getText().toString().trim());
        pajaroActualizado.setOrden(etOrden.getText().toString().trim());
        pajaroActualizado.setFamilia(etFamilia.getText().toString().trim());
        pajaroActualizado.setLongitud(etLongitud.getText().toString().trim());
        pajaroActualizado.setEnvergadura(etEnvergadura.getText().toString().trim());
        pajaroActualizado.setIdentificacion(etIdentificacion.getText().toString().trim());
        pajaroActualizado.setHabitat(etHabitat.getText().toString().trim());
        pajaroActualizado.setAlimentacion(etAlimentacion.getText().toString().trim());
        pajaroActualizado.setDesc(etDesc.getText().toString().trim());

        // (Solución Error PGRST204) "imagen_url" y "canto" NO existen en la base de datos de Supabase, 
        // son calculados/descargados al vuelo por la API. Si intentamos enviar esos campos 
        // (aunque sea con el valor original), Supabase rechaza toda la petición porque esas 
        // columnas no existen. Por tanto, los dejamos a null (Gson los ignorará) y sólo enviamos 
        // los datos reales para no romper el esquema.

        SupabaseService service = SupabaseClient.getClient().create(SupabaseService.class);
        service.updatePajaro("eq." + pajaroOriginal.getNombre(), pajaroActualizado).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditarPajaroActivity.this, "Pájaro guardado", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditarPajaroActivity.this, ListaPajaros.class);
                    // Recargar la lista
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Cuerpo de error nulo";
                        System.out.println("ERROR 400 SUPABASE: " + errorBody);
                        Toast.makeText(EditarPajaroActivity.this, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditarPajaroActivity.this, "Fallo de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
