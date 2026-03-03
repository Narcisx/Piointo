package com.example.proyectofinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.bumptech.glide.Glide;

/*
 * Adaptador para el GridView de la lista de pájaros.
 * Extiende ArrayAdapter para mostrar cada pájaro como una tarjeta
 * con imagen y nombre en el GridView de ListaPajaros.
 */
public class PajaroAdapter extends ArrayAdapter<Pajaro> {
    private Context context;

    //Constructor: recibe el contexto y la lista de pájaros
    public PajaroAdapter(Context context, List<Pajaro> pajaros) {
        super(context, 0, pajaros);
        this.context = context;
    }

    /*
     * Genera la vista de cada elemento del GridView.
     * Reutiliza vistas existentes (convertView) para mejor rendimiento.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        //Reutilizar la vista si ya existe, si no, inflar el layout item_pajaro
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pajaro, parent, false);
        }

        //Obtener el pájaro en esta posición
        Pajaro pajaro = getItem(position);

        //Vincular vistas del item
        ImageView imgPajaro = convertView.findViewById(R.id.imgPajaro);
        TextView tvNombre = convertView.findViewById(R.id.nombrePajaro);

        if (pajaro != null) {
            tvNombre.setText(pajaro.getNombre());

            //Cargar imagen con Glide si hay URL disponible
            if (pajaro.getImagen_url() != null && !pajaro.getImagen_url().trim().isEmpty()) {
                String imgUrl = pajaro.getImagen_url().trim();
                android.util.Log.d("DEBUG_IMAGE", "URL parseada: [" + imgUrl + "]");

                Glide.with(context)
                        .load(imgUrl)
                        .placeholder(android.R.drawable.ic_menu_report_image) //Imagen mientras carga
                        .error(android.R.drawable.ic_dialog_alert) //Imagen si hay error
                        .centerCrop()
                        .into(imgPajaro);
            } else {
                //Si no hay URL, mostrar icono por defecto
                android.util.Log.d("DEBUG_IMAGE", "Imagen nula o vacia para: " + pajaro.getNombre());
                imgPajaro.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        return convertView;
    }

    /*
     * Reemplaza toda la lista de pájaros y refresca la vista.
     * Se usa cuando llegan los datos de Supabase.
     */
    public void setLista(List<Pajaro> nuevaLista) {
        clear();
        addAll(nuevaLista);
        notifyDataSetChanged();
    }
}
