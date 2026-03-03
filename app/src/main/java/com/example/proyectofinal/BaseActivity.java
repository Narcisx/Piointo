package com.example.proyectofinal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

//Clase BaseActivity creada para centralizar el cambio de idioma en toda la app
public class BaseActivity extends AppCompatActivity {

    private String currentLanguage;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Lee el idioma configurado antes de que la actividad se adhiera a la pantalla
        SharedPreferences prefs = newBase.getSharedPreferences("MisAjustes", Context.MODE_PRIVATE);
        currentLanguage = prefs.getString("idioma", "es");
        Context context = updateBaseContextLocale(newBase, currentLanguage);
        super.attachBaseContext(context);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Si el idioma cambió mientras esta pantalla estaba en segundo
        // plano (ej. volviendo de Ajustes), la recargamos
        SharedPreferences prefs = getSharedPreferences("MisAjustes", Context.MODE_PRIVATE);
        String savedLanguage = prefs.getString("idioma", "es");
        if (currentLanguage != null && !currentLanguage.equals(savedLanguage)) {
            recreate();
        }
    }

    private Context updateBaseContextLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }
}
