package com.example.finalapp

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.runtime.remember

import android.Manifest

import androidx.compose.foundation.clickable

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

import androidx.compose.material.*
import androidx.compose.material.icons.Icons

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField


import androidx.compose.ui.text.TextStyle

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.viewinterop.AndroidView

import androidx.lifecycle.lifecycleScope
import org.osmdroid.views.overlay.Marker
import androidx.compose.material3.Text as Text1



enum class Pantalla {
    FORM,
    INGRESO,
    EDITAR,
    VISUALIZAR

}


class CameraAppViewModel : ViewModel() {
    val pantalla = mutableStateOf(Pantalla.FORM)


    var onPermisoUbicacionOk: () -> Unit = {}


    var lanzadorPermisos: ActivityResultLauncher<Array<String>>? = null



    fun cambiarPantallaEditar(registro: Registro) {
        pantalla.value = Pantalla.EDITAR
    }



    fun cambiarPantallaIngreso() {
        pantalla.value = Pantalla.INGRESO
    }

    fun cambiarPantallaForm() {
        pantalla.value = Pantalla.FORM
    }
    fun mostrarImagenCompleta(){
        pantalla.value = Pantalla.VISUALIZAR
    }

}


//camara
class MainActivity : ComponentActivity() {

    val cameraAppVm: CameraAppViewModel by viewModels()
    val lanzadorPermisos =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                (it[Manifest.permission.ACCESS_FINE_LOCATION] ?: false)
                        or (it[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false) -> {
                    Log.v("callback RequestMultiplePermissions", "permiso ubicacion granted")
                    cameraAppVm.onPermisoUbicacionOk()
                }

                else -> {
                }
            }
        }


    private lateinit var registroDao: RegistroDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            registroDao = AppDatabase.getInstance(this@MainActivity).registroDao()

            val registrosFromDb = registroDao.getAllRegistros()

            withContext(Dispatchers.Main) {
                // EJECUCION EN HILO PRINCIPAL
                cameraAppVm.cambiarPantallaForm()
                cameraAppVm.lanzadorPermisos = lanzadorPermisos
                setContent {
                    MyApp(
                        cameraAppViewModel = cameraAppVm,
                        registroDao = registroDao,


                        )
                }
            }
        }
    }
}

@Composable
fun MyApp(
    onSave: () -> Unit = {},
    cameraAppViewModel: CameraAppViewModel,
    registroDao: RegistroDao,


    ) {
    val (registros) = remember { mutableStateOf(emptyList<Registro>()) }
    val alcanceCorrutina = rememberCoroutineScope()




    when (cameraAppViewModel.pantalla.value) {
        Pantalla.FORM -> {
            ListaRegistros(
                appViewModel = cameraAppViewModel,
                registros = registros,
                onEliminarClick = { registro ->
                    alcanceCorrutina.launch {
                        registroDao.eliminarRegistro(registro)
                        cameraAppViewModel.cambiarPantallaForm()


                    }
                },
                onEditarClick = { registro ->
                    cameraAppViewModel.cambiarPantallaEditar(registro)

                }
            )
        }


        Pantalla.INGRESO -> {
            PantallaIngresoDatos(
                appViewModel = cameraAppViewModel,
                registroDao = registroDao,
                onGuardarClick = { nuevoRegistro ->
                    alcanceCorrutina.launch {
                        registroDao.insertarRegistro(nuevoRegistro)
                        cameraAppViewModel.cambiarPantallaForm()
                    }

                }
            )
        }

        Pantalla.EDITAR -> {

            val registroSeleccionado = Registro(
                lugar = "Nombre del lugar",
                imagenReferencia = "https://www.spanaturamallinco.com/?lightbox=dataItem-lbi7kww4",
                latitud = 123.456, // LATITUD
                longitud = 789.012, // LONGITUD
                orden = 1, // NUMERO DE LA ORDEN
                costoAlojamiento = 100.0, // PRECIO DEL ALOJAMIENTO
                costoTraslado = 50.0, // PRECIO DEL TRASLADO
                comentario = "Comentario"

            )

            PantallaEditar(
                appViewModel = cameraAppViewModel,
                registro = registroSeleccionado,
                onActualizarClick = { editedRegistro ->
                    alcanceCorrutina.launch {
                        registroDao.actualizarRegistro(editedRegistro)
                        cameraAppViewModel.cambiarPantallaForm()
                    }
                }
            )
        }

        // registroSeleccionado SEA NULO
        // MENSAJE DE ERROR o realizar alguna otra acción.



        Pantalla.VISUALIZAR -> {
            val registroSeleccionado = Registro(
                lugar = "Buin Zoo  ",
                imagenReferencia = "https://chileestuyo.cl/wp-content/uploads/2021/02/buinzoo.jpg",
                latitud = -33.71557,
                longitud = -70.72680,
                orden = 1, // NUM DE ORDEN
                costoAlojamiento = 10000.0, // PRECIO ALOJAMIENTO
                costoTraslado =4000.0, // PRECIO TRASLADO
                comentario = "El Buin Zoo es otro de los imperdibles paseos familiares de los santiaguinos"
            )

            PantallaDetalleRegistro(
                appViewModel = cameraAppViewModel,
                registro = registroSeleccionado!!,
                onEliminarClick = { registro ->
                    alcanceCorrutina.launch {
                        cameraAppViewModel.cambiarPantallaForm()
                        onSave()
                    }
                },
                onEditarClick = { registro ->
                    cameraAppViewModel.cambiarPantallaEditar(registro)
                }
            )
        }


        else -> {}
    }
}






@Composable
fun ListaRegistros(
    registros: List<Registro>,
    onEliminarClick: (Registro) -> Unit,
    onEditarClick: (Registro) -> Unit,
    appViewModel: CameraAppViewModel
) {

    val contexto = LocalContext.current
    val (registros, setRegistros) = remember { mutableStateOf(emptyList<Registro>()) }

    LaunchedEffect(registros) {
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(contexto).registroDao()
            setRegistros(dao.getAllRegistros())
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart) // LISTA EN LA ZONA SUPERIOR
        ) {
            items(registros) { registro ->
                RegistroItem(
                    registro = registro,
                    onEliminarClick = onEliminarClick,
                    cameraAppViewModel = appViewModel,
                ) {
                    setRegistros(emptyList<Registro>())
                }
            }
        }

        Button(
            onClick = {
                appViewModel.cambiarPantallaIngreso()
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(BottomCenter) // BOTON ZONA INFERIOR
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Text1("Agregar Lugar")
            }
        }
    }
}




@Composable
fun RegistroItem(
    registro: Registro,
    onSave: () -> Unit = {},
    onEliminarClick: (Registro) -> Unit,
    cameraAppViewModel: CameraAppViewModel,
    function: () -> Unit
) {
    val contexto = LocalContext.current
    val alcanceCorrutina = rememberCoroutineScope()
    val (registros, setRegistros) = remember { mutableStateOf(emptyList<Registro>()) }

    Card(
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth()
            .padding(0.dp)
            .clickable {
                cameraAppViewModel.mostrarImagenCompleta()
            }
            .background(Color.White)
            .padding(10.dp)

    ) {
        // VER LOS DATOS

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val painter = rememberImagePainter(
                data = registro.imagenReferencia,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.placeholder)
                    error(R.drawable.error)
                }
            )

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp) // DAR TAMAñO
                    .offset(y = -5.dp) // POSICION VERTICAL
                    .fillMaxSize() // IMAGEN DENTRO DEL TAMAñO
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp)
            ) {

                Spacer(modifier = Modifier.height(15.dp))

                Text1(
                    text = "${registro.lugar}",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 0.dp).offset(y = -5.dp)
                )
                Spacer(modifier = Modifier.height(0.dp))

                Text1(
                    text = "Costo por Alojamiento: ${registro.costoAlojamiento}",
                    style = TextStyle(fontSize = 10.sp),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text1(
                    text = "Costo por Traslado: ${registro.costoTraslado}",
                    style = TextStyle(fontSize = 10.sp),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.End
                ) {

                    // ESTE ES EL ICONO PARA EDITAR
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier

                            .clickable {
                            cameraAppViewModel.cambiarPantallaEditar(registro)
                        }
                    )

                    // ESTE ES EL BOTON PARA ELIMINAR
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Eliminar Producto",
                        modifier = Modifier


                            .clickable {
                            alcanceCorrutina.launch(Dispatchers.IO) {
                                val dao = AppDatabase.getInstance(contexto).registroDao()
                                dao.eliminarRegistro(registro)
                                onSave()
                                val nuevosRegistros = dao.getAllRegistros()
                                setRegistros(nuevosRegistros)
                                onSave()
                            }
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditar(
    onSave: () -> Unit = {},
    appViewModel: CameraAppViewModel,
    registro: Registro,
    onActualizarClick: (Registro) -> Unit
) {

    var editedLugar by remember { mutableStateOf(registro.lugar) }
    var editedImagenReferencia by remember { mutableStateOf(registro.imagenReferencia) }
    var editedLatitud by remember { mutableStateOf(registro.latitud.toString()) }
    var editedLongitud by remember { mutableStateOf(registro.longitud.toString()) }
    var editedOrden by remember { mutableStateOf(registro.orden.toString()) }
    var editedCostoAlojamiento by remember { mutableStateOf(registro.costoAlojamiento.toString()) }
    var editedCostoTraslado by remember { mutableStateOf(registro.costoTraslado.toString()) }
    var editedComentario by remember { mutableStateOf(registro.comentario) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Campos de edición
        Spacer(modifier = Modifier.width(20.dp))

        TextField(
            value = editedLugar,
            onValueChange = { editedLugar = it },
            label = { Text1("Lugar") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = editedImagenReferencia,
            onValueChange = { editedImagenReferencia = it },
            label = { Text1("Imagen de Referencia") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = editedLatitud,
            onValueChange = { editedLatitud = it },
            label = { Text1("Latitud") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = editedLongitud,
            onValueChange = { editedLongitud = it },
            label = { Text1("Longitud") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = editedOrden,
            onValueChange = { editedOrden = it },
            label = { Text1("Orden") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = editedCostoAlojamiento,
            onValueChange = { editedCostoAlojamiento = it },
            label = { Text1("Costo Alojamiento") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = editedCostoTraslado,
            onValueChange = { editedCostoTraslado = it },
            label = { Text1("Costo Traslado") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = editedComentario,
            onValueChange = { editedComentario = it },
            label = { Text1("Comentario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(60.dp))
        // ESTE BOTON ES PARA EDITAR EL REGISTRO
        Button(
            onClick = {
                // GUARDAR CAMBIOS
                val editedRegistro = Registro(
                    lugar = editedLugar,
                    imagenReferencia = editedImagenReferencia,
                    latitud = editedLatitud.toDoubleOrNull() ?: 0.0,
                    longitud = editedLongitud.toDoubleOrNull() ?: 0.0,
                    orden = editedOrden.toIntOrNull() ?: 0,
                    costoAlojamiento = editedCostoAlojamiento.toDoubleOrNull() ?: 0.0,
                    costoTraslado = editedCostoTraslado.toDoubleOrNull() ?: 0.0,
                    comentario = editedComentario
                )
                onActualizarClick(editedRegistro)
                onSave()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)

        ) {
            Text1("Actualizar Registro")

        }
        Spacer(modifier = Modifier.height(5.dp))

        Button(
            onClick = {
                // CAMBIAR AL FORMULARIO PRINCIPAL
                appViewModel.cambiarPantallaForm()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
        ) {
            Text1("Volver")
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaIngresoDatos(
    onSave: () -> Unit = {},
    appViewModel: CameraAppViewModel,
    registroDao: RegistroDao,
    onGuardarClick: (Registro) -> Unit


) {

    var lugar by remember { mutableStateOf("") }
    var imagenReferencia by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var orden by remember { mutableStateOf(0) }
    var costoAlojamiento by remember { mutableStateOf(0.0) }
    var costoTraslado by remember { mutableStateOf(0.0) }
    var comentario by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = lugar,
            onValueChange = { lugar = it },
            label = { Text1("Lugar") },
            modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho disponible
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = imagenReferencia,
            onValueChange = { imagenReferencia = it },
            label = { Text1("Imagen de Referencia (URL)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = latitud.toString(),
            onValueChange = { latitud = it.toDoubleOrNull() ?: 0.0 },
            label = { Text1("Latitud") },
            modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho disponible
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = longitud.toString(),
            onValueChange = { longitud = it.toDoubleOrNull() ?: 0.0 },
            label = { Text1("Longitud") },
            modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho disponible
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = orden.toString(),
            onValueChange = { orden = it.toIntOrNull() ?: 0 },
            label = { Text1("Orden") },
            modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho disponible
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = costoAlojamiento.toString(),
            onValueChange = { costoAlojamiento = it.toDoubleOrNull() ?: 0.0 },
            label = { Text1("Costo Alojamiento") },
            modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho disponible
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = costoTraslado.toString(),
            onValueChange = { costoTraslado = it.toDoubleOrNull() ?: 0.0 },
            label = { Text1("Costo Traslado") },
            modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho disponible
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = comentario,
            onValueChange = { comentario = it },
            label = { Text1("Comentario") },
            modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho disponible
        )

        Spacer(modifier = Modifier.height(70.dp))

        Button(
            onClick = {
                val nuevoRegistro = Registro(
                    lugar = lugar,
                    imagenReferencia = imagenReferencia,
                    latitud = latitud,
                    longitud = longitud,
                    orden = orden,
                    costoAlojamiento = costoAlojamiento,
                    costoTraslado = costoTraslado,
                    comentario = comentario
                )
                onGuardarClick(nuevoRegistro)
                onSave()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
        ) {

            Text1("Guardar")
        }




        }
    }
@Composable
fun PantallaDetalleRegistro(
    appViewModel: CameraAppViewModel,
    registro: Registro,
    onEditarClick: (Registro) -> Unit,
    onEliminarClick: (Registro) -> Unit
) {
    val contexto = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)

    ) {
        Text(
            text = registro.lugar,
            style = TextStyle(fontSize = 15.sp),
            modifier = Modifier
                )

        Spacer(modifier = Modifier.height(15.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val painter = rememberImagePainter(
                data = registro.imagenReferencia,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.placeholder)
                    error(R.drawable.error)
                }
            )
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp,200.dp)

            )
        }


        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Costo Alojamiento: ${registro.costoAlojamiento}",
            style = TextStyle(
                fontSize = 15.sp,

            ), // TAMAñO FUENTE

            modifier = Modifier.padding(bottom = 8.dp) // Espacio en la parte inferior
        )
        Text(
            text = "Costo Traslado: ${registro.costoTraslado}",
            style = TextStyle(
                fontSize = 15.sp,

            ), // AJUSTAR EL TAMANO
            modifier = Modifier.padding(bottom = 8.dp) // Espacio en la parte inferior
        )
        Text(
            text = "${registro.comentario}",
            style = TextStyle(
                fontSize = 15.sp

            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // LOS ICONOS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.width(20.dp)) // ESPACIO ENTRE ICONOS

            Icon(
                Icons.Default.Edit,
                contentDescription = "Editar",
                modifier = Modifier
                    .clickable {
                        onEditarClick(registro)
                    }
                    .size(30.dp) // TAMAñO
            )
            Spacer(modifier = Modifier.width(32.dp))
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Eliminar Producto",
                modifier = Modifier
                    .clickable {
                        onEliminarClick(registro)
                    }
                    .size(30.dp)
            )
        }

        // Agrega espacio entre los iconos y el mapa
        Spacer(modifier = Modifier.height(80.dp))


        // MAPA
        AndroidView(
            factory = {
                MapView(it).also { mapView ->
                    mapView.setTileSource(TileSourceFactory.MAPNIK)
                    Configuration.getInstance().userAgentValue =
                        contexto.packageName

                    mapView.controller.setZoom(18.0)
                    val geoPoint = GeoPoint(registro.latitud, registro.longitud)
                    mapView.controller.animateTo(geoPoint)

                    val marcador = Marker(mapView)
                    marcador.position = geoPoint
                    marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    mapView.overlays.add(marcador)

                    mapView.setClickable(false)
                }
            },
            modifier = Modifier
                .fillMaxWidth()



        )
        Button(
            onClick = {
                appViewModel.cambiarPantallaForm()
            },
            modifier = Modifier
                .fillMaxWidth()

                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text1("Volver")
            }
        }
    }
}











