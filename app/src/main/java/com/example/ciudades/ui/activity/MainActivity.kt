package com.example.ciudades.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ciudades.R
import com.example.ciudades.databinding.ActivityMainBinding
import com.example.ciudades.viewmodel.ViewModelCiudad
import com.google.android.gms.location.LocationServices
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val viewModelCiudad: ViewModelCiudad by viewModels {
        ViewModelCiudad.MyViewModelFactory(this.application)
    }
    private lateinit var uri: Uri
    private lateinit var photoFile: File
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Al abrir la actividad, pedimos servicios de localización al usuario para que nos deje acceder a su ubicación
        activaUbicacionActual()

        // Establecemos un Toolbar para que actúe como barra de ActionBar para esta actividad.
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerViewListaCiudades) as NavHostFragment
        val navController = navHostFragment.navController

        // Inicializamos el navController y el appBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.app_bar_Listado_Ciudades,
                R.id.app_bar_Config_Ciudad,
                R.id.app_bar_Mapa_Ciudades,
                R.id.app_bar_Buscar_Ciudad,
            )
        )
        // Sincronizamos el menu, la navegación y el toolbar
        setupActionBarWithNavController(navController, appBarConfiguration)
        // Configurar la sincronización con el BottomNavigationView con toro esto
        binding.menuInferior.setupWithNavController(navController)

        // Listener al pulsar botón menu
        binding.menuInferior.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.app_bar_Listado_Ciudades -> {

                    navController.navigate(R.id.app_bar_Listado_Ciudades)
                }
                R.id.app_bar_Config_Ciudad    -> {
                    viewModelCiudad.origen.value = "nuevaCiudad"
                    navController.navigate(R.id.app_bar_Config_Ciudad)
                }
                R.id.app_bar_Mapa_Ciudades    -> {
                    navController.navigate(R.id.app_bar_Mapa_Ciudades)
                }
                // Al pulsar sobre el botón de buscar ciudad se muestra el fragment de buscar ciudad no el de resultado.
                R.id.app_bar_Buscar_Ciudad    -> {
                    navController.navigate(R.id.app_bar_Buscar_Ciudad)
                }
            }
            true
        }
    }

    // Se llama a este método cada vez que el usuario elige navegar hacia arriba dentro de la jerarquía de actividad de su aplicación desde la barra de acción.
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragmentContainerViewListaCiudades)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // -------- CAMARA ------------

    fun tomarImagenCamara() {
        photoFile = crearArchivoParaFoto()

        uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        resultCameraLauncher.launch(uri)
    }

    private val resultCameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            viewModelCiudad.imageUri.value = uri
        } else {
            photoFile.let {
                if (photoFile.exists()) {
                    val result = photoFile.delete()
                    val msg = if (result) "OK" else "ERROR"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun crearArchivoParaFoto(): File {
        //nombre de archivo con fecha y hora actual
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // ruta a la carpeta privada de la App
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // Crea un objeto File con el nombre de archivo, la extensión y la carpeta donde se almacena el archivo
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefijo */
            ".jpg", /* sufijo */
            storageDir /* directorio */
        )
    }

    fun tomarImagenGaleria() = resultadoImagenGaleria.launch(arrayOf("image/*"))

    private val resultadoImagenGaleria = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        // Comprobamos que la URI no sea nula
        uri?.let {
            //            binding.imageView.setImageURI(uri)
            val file = crearArchivoParaFoto()
            copyImageToPrivateFolder(uri, file)
            viewModelCiudad.imageUri.value = file.toUri()
        }
    }


    //dataString es la ruta de la imagen seleccionada de la galería
    //fileDestino es un file temporal que hemos creado en la carpeta privada
    //esta función convierte la imagen de la galería en un bitmap, y llama a la función saveImage
    //pasándole el bitmap y el file de destino
    private fun copyImageToPrivateFolder(dataString: Uri?, fileDestino: File) {
        if (dataString != null) {
            try {
                val inStream: InputStream? = contentResolver.openInputStream(dataString)
                val selected_img = BitmapFactory.decodeStream(inStream)
                saveImage(selected_img, fileDestino)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this, "Ha ocurrido un error!", Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //esta función copia el bitmap recibido al file destino
    @Throws(IOException::class)
    private fun saveImage(bitmap: Bitmap, fileDestino: File) {
        val saved: Boolean
        val fos: OutputStream
        fos = FileOutputStream(fileDestino)
        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        if (saved) Toast.makeText(this, "Todo fue bien", Toast.LENGTH_SHORT).show()
    }


    //------------------   PERMISOS  ------------------------------------------------------------

    // -------- UBICACIÓN ------------

    val ubicacionPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it.containsValue(false)) {
            Toast.makeText(
                this,
                "No has aceptado los permisos",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            activaUbicacionActual()
        }

        it.forEach { mapa ->
            when (mapa.key) {
                Manifest.permission.ACCESS_FINE_LOCATION   -> {
                    if (mapa.value) {
                        Log.d("Permisos", "Permiso ACCESS_FINE_LOCATION concedido")
                    }
                }
                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                    if (mapa.value) {
                        Log.d("Permisos", "Permiso ACCESS_COARSE_LOCATION concedido")
                    }
                }
            }
        }
    }

    private fun activaUbicacionActual() {
        if (
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            // Listener para registrar la ubicación actual
            LocationServices.getFusedLocationProviderClient(this)
                .lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Obtener la latitud y longitud que servirá para mostrarla en el mapa al pulsar
                        viewModelCiudad.latitudVariable.value = location.latitude
                        viewModelCiudad.longitudVariable.value = location.longitude
                        // Inicializamos o actualizamos la ubicación actual
                        viewModelCiudad.ubicacionActual.value = location
                    } else {
                        Log.d("Ubicacion", "No se ha podido obtener la ubicación")
                    }
                }
        } else {
            ubicacionPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )
        }
    }

}