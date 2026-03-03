package com.example.proyectofinal;

import okhttp3.*;
import java.io.IOException;

public class SupabaseAuthManager {

    private static final String SUPABASE_URL = "https://kgogfcfbkanmibmbaqas.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtnb2dmY2Zia2FubWlibWJhcWFzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE4Njc2OTQsImV4cCI6MjA4NzQ0MzY5NH0.vdmcpKW1BNCJ-h__4gYDPiV8DvgVA87k2-UrcyG3WI8";

    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public interface AuthCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    // 1. INICIAR SESIÓN
    public void iniciarSesion(String email, String password, AuthCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/token?grant_type=password";
        String jsonInput = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        ejecutarPeticion(url, jsonInput, "POST", null, callback);
    }

    // 2. REGISTRAR USUARIO
    public void registrarUsuario(String email, String password, AuthCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/signup";
        String jsonInput = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        ejecutarPeticion(url, jsonInput, "POST", null, callback);
    }

    // 3. RECUPERAR CONTRASEÑA
    public void recuperarPassword(String email, AuthCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/recover";
        String jsonInput = String.format("{\"email\": \"%s\"}", email);
        ejecutarPeticion(url, jsonInput, "POST", null, callback);
    }

    // 4. ACTUALIZAR CONTRASEÑA
    public void actualizarPassword(String nuevaPassword, String accessToken, AuthCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/user";
        String jsonInput = String.format("{\"password\": \"%s\"}", nuevaPassword);
        ejecutarPeticion(url, jsonInput, "PUT", accessToken, callback);
    }

    // --- 5. BORRAR CUENTA (CORREGIDO) ---
    public void borrarCuenta(String accessToken, AuthCallback callback) {
        // Ahora llamamos a la función RPC que creamos en SQL
        String url = SUPABASE_URL + "/rest/v1/rpc/eliminar_mi_usuario";

        // Las funciones RPC en Supabase siempre requieren un POST,
        // aunque el cuerpo esté vacío "{}"
        ejecutarPeticion(url, "{}", "POST", accessToken, callback);
    }

    private void ejecutarPeticion(String url, String jsonInput, String metodo, String token, AuthCallback callback) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json");

        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        RequestBody body = (jsonInput != null) ? RequestBody.create(jsonInput, JSON) : null;

        switch (metodo) {
            case "POST": builder.post(body); break;
            case "PUT": builder.put(body); break;
            case "DELETE": builder.delete(); break;
        }

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    callback.onSuccess(responseBody);
                } else {
                    // Mejorado para ver el código de error real
                    callback.onError("Error " + response.code() + ": " + responseBody);
                }
            }
        });
    }
}
