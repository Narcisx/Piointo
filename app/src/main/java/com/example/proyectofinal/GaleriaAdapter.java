package com.example.proyectofinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/*
 * Adaptador de RecyclerView para la galería de fotos.
 * Carga las imágenes en tamaño "medium" usando Glide y gestiona los clicks.
 */
public class GaleriaAdapter extends RecyclerView.Adapter<GaleriaAdapter.ViewHolder> {

    private final Context context;
    private final List<String> imageUrls;      // Lista de URLs de imágenes
    private final OnImageClickListener listener; // Listener para clicks en imágenes

    /*
     * Interfaz que debe implementar la Activity para recibir clicks en las fotos.
     */
    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    /*
     * Constructor del adaptador.
     */
    public GaleriaAdapter(Context context, List<String> imageUrls, OnImageClickListener listener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout de cada item del grid (CardView con ImageView)
        View view = LayoutInflater.from(context).inflate(R.layout.item_galeria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = imageUrls.get(position);

        // Cargar imagen con Glide (maneja caché, descarga asíncrona, y reciclaje)
        Glide.with(context)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)  // Icono mientras carga
                .error(android.R.drawable.ic_dialog_alert)        // Icono si hay error
                .centerCrop()                                      // Recortar para llenar el espacio
                .into(holder.imgGaleria);

        // Al hacer click en la imagen, convertir URL a tamaño "original" y notificar
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Cambiar "/square." o "/medium." por "/original." para obtener máxima resolución
                String largeUrl = url.replace("/square.", "/original.").replace("/medium.", "/original.");
                listener.onImageClick(largeUrl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    /*
     * ViewHolder que mantiene la referencia al ImageView de cada item.
     * Evita llamar a findViewById() cada vez que se recicla un item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgGaleria;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGaleria = itemView.findViewById(R.id.imgGaleria);
        }
    }
}
