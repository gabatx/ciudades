package com.example.ciudades.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ciudades.R
import com.example.ciudades.databinding.FragmentListaCiudadesBinding
import com.example.ciudades.ui.adapter.AdapterListaCiudades
import com.example.ciudades.ui.adapter.IVisitadoListener
import com.example.ciudades.viewmodel.ViewModelCiudad
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ListaCiudadesFragment : Fragment() {

    // Creamos variable LifecycleOwner para pasarsela al adapter
    private val lifecycleOwner by lazy{
        binding.root.context as? LifecycleOwner
    }

    // Una vez que se crea por primera vez, ya vale hasta que muera la actividad.
    private val viewModelCiudades by activityViewModels<ViewModelCiudad> {
        ViewModelCiudad.MyViewModelFactory(requireActivity().application)
    }


    private lateinit var binding: FragmentListaCiudadesBinding
    private lateinit var adapter: AdapterListaCiudades


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentListaCiudadesBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModelCiudades.toast.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        // Esto para decirle a la actividad que el menu lo va a poner el fragment
        setHasOptionsMenu(true)

        // Guarda el nombre del fragment actual en el ViewModel
        viewModelCiudades.origen.value = "ListaCiudadFragment"

        // Inicializamos el adapter
        configuracionReyclerView(viewModelCiudades, lifecycleOwner)

        // Carga las ciudades en el RecyclerView ordenadas por nombre
        viewModelCiudades.listaCiudades.observe(viewLifecycleOwner) { ciudad ->
            adapter.actualizaresultado(ciudad.sortedBy { it.distancia })
        }
    }

    private fun configuracionReyclerView(viewModelCiudades: ViewModelCiudad, lifecycleOwner: LifecycleOwner?) {

        val layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = AdapterListaCiudades(
            viewModelCiudades,
            object : IVisitadoListener {

                override fun listenerBotonVisitado(color: Int, boton: FloatingActionButton) {
                    cambiaFondoIcono(color, boton)
                }

                override fun asignaColorBotonVisitado(visitado: Boolean, boton: FloatingActionButton) {
                    if (visitado) {
                        cambiaFondoIcono(R.color.viajado_activado, boton)
                    } else {
                        cambiaFondoIcono(R.color.viajado_desactivado, boton)
                    }
                }
            },
            lifecycleOwner!!
        )
        binding.recyclerViewCiudades.layoutManager = layoutManager
        binding.recyclerViewCiudades.adapter = adapter
    }


    // funcion para inflar el toolbar
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_lista_ciudades, menu)

        // Filtra por visitados al pulsar el switch
        val menuItem = menu.findItem(R.id.switchActionBarVisitados)
        val miSwitch = menuItem.actionView as SwitchCompat

        miSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                viewModelCiudades.listaCiudades.observe(viewLifecycleOwner) { ciudad ->
                    adapter.actualizaresultado(ciudad.filter { it.Visitada }.sortedBy { it.distancia })
                }
            } else {
                viewModelCiudades.listaCiudades.observe(viewLifecycleOwner) { ciudad ->
                    adapter.actualizaresultado(ciudad.sortedBy { it.distancia })
                }
            }
        }
    }

    // La función sirve de evento al pulsar un item del toolbar superior
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.botonBorrarTodosContactos) {
            // Preguntar borrar todos los contactos
            alertDialogPreguntarBorrar()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun alertDialogPreguntarBorrar(): Boolean {
        // Crear un alertDialog
        val builder = AlertDialog.Builder(requireContext(), R.style.temaAlertDialog)
        builder.setTitle("Eliminar todas las ciudades")
        builder.setMessage("¿Está seguro de que desea eliminar todas las ciudades?")
        builder.setPositiveButton("Sí") { _, _ ->

            viewModelCiudades.listaCiudades.value?.let {
                if (it.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay ciudades para borrar", Toast.LENGTH_SHORT).show()

                } else {
                    // Eliminar todos las ciudades
                    viewModelCiudades.borrarTodasLasCiudades()
                    Toast.makeText(requireContext(), "Se han eliminado todas las ciudades", Toast.LENGTH_SHORT).show()
                    viewModelCiudades.listaCiudades = viewModelCiudades.repositorio.todasLasCiudades
                }
            }
        }
        builder.setNegativeButton("No") { _, _ ->
            Toast.makeText(requireContext(), "No se eliminarán las ciudades", Toast.LENGTH_SHORT).show()
        }
        builder.show()
        return true
    }

    // CAMBIA EL ESTADO DEL BOTÓN DE VOTO
    private fun cambiaFondoIcono(color: Int, boton: FloatingActionButton) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boton.backgroundTintList = ColorStateList.valueOf(
                resources.getColorStateList(
                    color,
                    null
                ).defaultColor
            )
        }
    }
}
