package com.example.ciudades.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ciudades.R
import com.example.ciudades.databinding.HolderCiudadBinding
import com.example.ciudades.domain.models.Ciudad
import com.example.ciudades.viewmodel.ViewModelCiudad
import com.google.android.material.floatingactionbutton.FloatingActionButton

interface IVisitadoListener {
    fun listenerBotonVisitado(color: Int, boton: FloatingActionButton)
    fun asignaColorBotonVisitado(visitado: Boolean, boton: FloatingActionButton)
}

class AdapterListaCiudades(
        val viewModelCiudad: ViewModelCiudad,
        private val listenerCiudad: IVisitadoListener,
        val lifecycleOwner: androidx.lifecycle.LifecycleOwner
) : RecyclerView.Adapter<AdapterListaCiudades.holderCiudad>() {

    var listaCiudades = ArrayList<Ciudad>()

    inner class holderCiudad(val binding: HolderCiudadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): holderCiudad {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = HolderCiudadBinding.inflate(layoutInflater, parent, false)
        return holderCiudad(binding)
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBindViewHolder(holder: holderCiudad, position: Int) {

        val ciudad = listaCiudades[position]

        holder.binding.nombreCiudadHolder.text = ciudad.nombre
        holder.binding.paisCiudadHolder.text = ciudad.pais
        if (ciudad.Visitada) holder.binding.visitadoHolder.text = "Visitado"
        else holder.binding.visitadoHolder.text = "No visitado"
       holder.binding.distanciaHolderCiudad.text = viewModelCiudad
           .calculaDistanciaUbicacionActualDestino(ciudad).toString() + " km"


        viewModelCiudad.ubicacionActual.observe(lifecycleOwner) {
            holder.binding.distanciaHolderCiudad.text = viewModelCiudad
                .calculaDistanciaUbicacionActualDestino(ciudad).toString() + " km"
        }


        Glide.with(holder.binding.root.context)
            .load(ciudad.Imagen)
            .into(holder.binding.imagenCiudadHolder)

        listenerCiudad.asignaColorBotonVisitado(ciudad.Visitada, holder.binding.botonVisitadoHolder)

        holder.binding.botonVisitadoHolder.setOnClickListener {

            ciudad.Visitada = !ciudad.Visitada
            ciudad.let { it1 -> viewModelCiudad.actualizarCiudad(it1) }
            viewModelCiudad.ciudadSeleccionada.value = ciudad

            holder.binding.visitadoHolder.text = "No Visitado"

            listenerCiudad.listenerBotonVisitado(R.color.viajado_desactivado, holder.binding.botonVisitadoHolder)
            notifyDataSetChanged()
        }

        holder.binding.botonEditarHolder.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_app_bar_Listado_Ciudades_to_app_bar_Config_Ciudad)
            viewModelCiudad.origen.value = "Editar"
            viewModelCiudad.ciudadSeleccionada.value = ciudad
        }

        holder.binding.cardViewCiudad.setOnClickListener {

            viewModelCiudad.ciudadSeleccionada.value = ciudad

            when (viewModelCiudad.origen.value) {
                "ListaCiudadFragment"     -> {
                    Navigation.findNavController(it)
                        .navigate(R.id.action_app_bar_Listado_Ciudades_to_ciudadDetalleFragment)
                }
                "BuscarResultadoFragment" -> {
                    Navigation.findNavController(it)
                        .navigate(R.id.action_buscarResultadoFragment_to_ciudadDetalleFragment)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listaCiudades.size
    }

    fun actualizaresultado(listaCiudadesActualizada: List<Ciudad>) {
        listaCiudades.clear()
        listaCiudades.addAll(listaCiudadesActualizada)
        notifyDataSetChanged()
    }

}