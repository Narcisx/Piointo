package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import androidx.core.content.ContextCompat;
import com.google.android.material.navigation.NavigationView;

//Hereda de BaseActivity para aplicar el idioma correcto
public class ChatIA extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    Toolbar toolbar;
    DrawerLayout drawerlayout;
    NavigationView navigationView;

    ImageButton button;

    LinearLayout contenedorMensajes;
    android.widget.ScrollView scrollChat;
    EditText inputPregunta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_ia);

        toolbar = findViewById(R.id.toolbar);
        drawerlayout = findViewById(R.id.main_chat);
        navigationView = findViewById(R.id.nav_view);

        // Enlazamos ids con los elementos del xml
        button = findViewById(R.id.boton_curiosidad);
        inputPregunta = findViewById(R.id.input_pregunta);
        contenedorMensajes = findViewById(R.id.contenedor_mensajes);
        scrollChat = findViewById(R.id.scroll_chat);

        // Mensaje de bienvenida para que el chat no esté en blanco al entrar
        agregarBurbujaChat(getString(R.string.chat_ia_bienvenida), false);
        // Cuando clickamos manda la pregunta del usuario al método que responde el
        // prompt
        button.setOnClickListener(view -> {
            String pregunta = inputPregunta.getText().toString();
            if (pregunta.trim().isEmpty())
                return; // Si está vacío, no hace nada

            // agrega la pregunta del usuario al chat
            agregarBurbujaChat(pregunta, true);

            // Limpia la caja y esconde el teclado para que no moleste
            inputPregunta.setText("");
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                    android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            // Envia la pregunta a Gemini
            preguntarAGemini(pregunta);
        });

        // Configuración de la hamburguesa igual que en el resto de codigos
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

    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_pajaros) {
            Intent intent = new Intent(ChatIA.this, ListaPajaros.class);
            startActivity(intent);
        } else if (id == R.id.nav_inicio) {
            Intent intent = new Intent(ChatIA.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ajustes) {
            Intent intent = new Intent(ChatIA.this, AjustesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_agra) {
            Intent intent = new Intent(ChatIA.this, AgradecimientosActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ayuda) {
            Intent intent = new Intent(ChatIA.this, ayuda.class);
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(ChatIA.this, InformacionActivity.class);
            startActivity(intent);
        }
        drawerlayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Con la pregunta del usuario, la mandamos a responder a gemini con el modelo
    // gemini-2.5-flash, ya que es más rapido
    private void preguntarAGemini(String preguntaDelUsuario) {

        agregarBurbujaChat("...", false);

        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", "AIzaSyANg_tNiopmi8zYzN_JmV6r3J8bCrpiF-o"); // API
                                                                                                                 // gemini
        GenerativeModelFutures model = GenerativeModelFutures.from(gm); // consulta por internet al modelo

        // Primero ponemos el prompt para que asuma el rol de experto en ornitología y
        // le sumamos la pregunta del usuario
        String promptCompleto = "Eres un experto en ornitología. Responde de forma breve, curiosa y amigable a esto: "
                + preguntaDelUsuario;
        // Como Android fuerza a crear content, a los Strings de java tenemos que convertirlo en el
        // formato de google (Content)
        // Usamos un contructor (Builder) para pasar el prompt que esta en String a
        // Content
        Content content = new Content.Builder().addText(promptCompleto).build();
        // mandamos el Content a la IA, Con el listenableFuture, sabemos cuando nos
        // devuelve el mensaje
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        // Aqui cuando nos avisa, reacciona y depende si ha salido bien o mal hace una
        // accion u otra
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            // Si todo sale bien
            public void onSuccess(GenerateContentResponse result) {
                // Dibuja el mensaje de GEMINI (A la izquierda)
                agregarBurbujaChat(result.getText(), false);
            }

            @Override
            // Si sale mal
            public void onFailure(Throwable t) {
                String errorMsg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
                if (errorMsg.contains("quota exceeded") || errorMsg.contains("429")) {
                    agregarBurbujaChat(getString(R.string.chat_ia_error_cuota), false);
                } else {
                    agregarBurbujaChat(getString(R.string.chat_ia_error_conexion, t.getMessage()), false);
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Este metodo nos sirve para poner en el chat los mensajes, dependiendo si es
    // el usuario o la ia.
    private void agregarBurbujaChat(String texto, boolean esUsuario) {
        // Creamos el texto de la burbuja
        TextView burbuja = new TextView(this);
        burbuja.setText(texto);
        burbuja.setTextSize(16f);
        burbuja.setPadding(40, 30, 40, 30);

        // Configuramos su tamaño y márgenes
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 20); // Espacio entre mensajes

        // Si es del usuario
        if (esUsuario) {
            params.gravity = android.view.Gravity.END; // A la derecha
            burbuja.setBackgroundResource(R.drawable.bg_mensaje_usuario);
            burbuja.setTextColor(android.graphics.Color.WHITE);
            // SI es de la IA
        } else {
            params.gravity = android.view.Gravity.START; // A la izquierda
            burbuja.setBackgroundResource(R.drawable.bg_mensaje_bot);
            burbuja.setTextColor(android.graphics.Color.BLACK);
        }
        // Seteamos el layout del mensaje
        burbuja.setLayoutParams(params);

        // Lo pegamos en la pantalla
        contenedorMensajes.addView(burbuja);

        // Con este codigo hace un scroll hacia abajo automaticamente cuando manda mensajes
        scrollChat.post(() -> scrollChat.fullScroll(android.view.View.FOCUS_DOWN));
    }
}
