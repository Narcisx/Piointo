package com.example.proyectofinal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnadirPajaroActivity extends BaseActivity {

    private EditText etNombre, etCientifico, etDesc, etOrden, etFamilia, etLongitud, etEnvergadura, etIdentificacion,
            etHabitat, etAlimentacion;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_pajaro);

        etNombre = findViewById(R.id.etNuevoNombre);
        etCientifico = findViewById(R.id.etNuevoCientifico);
        etDesc = findViewById(R.id.etNuevaDesc);
        etOrden = findViewById(R.id.etNuevoOrden);
        etFamilia = findViewById(R.id.etNuevaFamilia);
        etLongitud = findViewById(R.id.etNuevaLongitud);
        etEnvergadura = findViewById(R.id.etNuevaEnvergadura);
        etIdentificacion = findViewById(R.id.etNuevaIdentificacion);
        etHabitat = findViewById(R.id.etNuevoHabitat);
        etAlimentacion = findViewById(R.id.etNuevaAlimentacion);

        btnGuardar = findViewById(R.id.btnGuardarAve);

        btnGuardar.setOnClickListener(v -> guardarAve());
    } 

    // Metodo para guardar un ave
    private void guardarAve() {
        String nombre = etNombre.getText().toString().trim();
        String cientifico = etCientifico.getText().toString().trim();
        String descripcion = etDesc.getText().toString().trim();
        String orden = etOrden.getText().toString().trim();
        String familia = etFamilia.getText().toString().trim();
        String longitud = etLongitud.getText().toString().trim();
        String envergadura = etEnvergadura.getText().toString().trim();
        String identificacion = etIdentificacion.getText().toString().trim();
        String habitat = etHabitat.getText().toString().trim();
        String alimentacion = etAlimentacion.getText().toString().trim();

        if (nombre.isEmpty() || cientifico.isEmpty()) {
            Toast.makeText(this, "Nombre y científico son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Pajaro nuevo = new Pajaro();
        // No inicializamos el id para que sea null (autogenerado)
        nuevo.setNombre(nombre);
        nuevo.setNombre_cientifico(cientifico);
        nuevo.setDesc(descripcion);

        // El error 23502 indica que 'orden' (y probablemente otros) son NOT NULL.
        // Los inicializamos con el texto introducido por el usuario.
        // NO inicializamos 'imagen_url' porque esa columna NO existe (error PGRST204).
        nuevo.setOrden(orden);
        nuevo.setFamilia(familia);
        nuevo.setLongitud(longitud);
        nuevo.setEnvergadura(envergadura);
        nuevo.setIdentificacion(identificacion);
        nuevo.setHabitat(habitat);
        nuevo.setAlimentacion(alimentacion);
        nuevo.setCanto(""); // Se rellena automáticamente por API después

        SupabaseService service = SupabaseClient.getClient().create(SupabaseService.class);
        service.insertarPajaro(nuevo).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AnadirPajaroActivity.this, "¡Ave añadida con éxito!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // Logueamos el error detallado para saber qué columna o restricción está
                    // fallando
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string()
                                : "Sin detalles";
                        android.util.Log.e("SUPABASE_ERROR", "Error " + response.code() + ": " + errorBody);
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(AnadirPajaroActivity.this, "Ya existe este ave.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                android.util.Log.e("SUPABASE_ERROR", "Error de red: " + t.getMessage());
                Toast.makeText(AnadirPajaroActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}