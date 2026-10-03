package com.example.camara2;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageView imgFoto;
    private EditText etTitulo;
    private EditText etDescripcion;
    private TextView tvFecha;
    private Bitmap fotografiaBitmap;

    private final ActivityResultLauncher<String> launcherPermisoCamara =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    concedido -> {
                        if (concedido) abrirCamara();
                        else Toast.makeText(this, "Se necesita permiso de camara", Toast.LENGTH_SHORT).show();
                    });

    private final ActivityResultLauncher<Intent> launcherCamara =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    resultado -> {
                        if (resultado.getResultCode() == RESULT_OK) {
                            Intent datos = resultado.getData();
                            if (datos != null && datos.getExtras() != null) {
                                fotografiaBitmap = (Bitmap) datos.getExtras().get("data");
                                imgFoto.setImageBitmap(fotografiaBitmap);
                                Toast.makeText(this, "Foto tomada", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgFoto = findViewById(R.id.imgFoto);
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        tvFecha = findViewById(R.id.tvFecha);
        Button btnTomarFoto = findViewById(R.id.btnTomarFoto);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnVerLista = findViewById(R.id.btnVerLista);

        mostrarFecha();

        btnTomarFoto.setOnClickListener(v -> verificarPermisoCamara());
        btnGuardar.setOnClickListener(v -> guardarEvidencia());
        btnVerLista.setOnClickListener(v -> verLista());
    }

    private void mostrarFecha() {
        String fecha = new SimpleDateFormat("dd/MM/yyyy  -  HH:mm", Locale.getDefault()).format(new Date());
        tvFecha.setText("Registrado: " + fecha);
    }

    private void verificarPermisoCamara() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                launcherPermisoCamara.launch(Manifest.permission.CAMERA);
            }
        } else {
            abrirCamara();
        }
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            launcherCamara.launch(intent);
        } else {
            Toast.makeText(this, "No se pudo abrir la camara", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarEvidencia() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (titulo.isEmpty()) {
            Toast.makeText(this, "Escribe un titulo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Escribe una descripcion", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fotografiaBitmap == null) {
            Toast.makeText(this, "Toma una foto primero", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File carpeta = new File(getExternalFilesDir(null), "EvidenciasCESBA");
            if (!carpeta.exists()) carpeta.mkdirs();

            String nombre = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
            File archivo = new File(carpeta, nombre);

            FileOutputStream fos = new FileOutputStream(archivo);
            fotografiaBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            Toast.makeText(this, "Guardado: " + titulo, Toast.LENGTH_LONG).show();

            etTitulo.setText("");
            etDescripcion.setText("");
            imgFoto.setImageBitmap(null);
            fotografiaBitmap = null;

        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    private void verLista() {
        File carpeta = new File(getExternalFilesDir(null), "EvidenciasCESBA");
        StringBuilder lista = new StringBuilder();

        if (!carpeta.exists()) {
            lista.append("No hay evidencias aun.\nToma una foto y guardala.");
            imgFoto.setImageBitmap(null);
        } else {
            File[] archivos = carpeta.listFiles();
            if (archivos == null || archivos.length == 0) {
                lista.append("No hay evidencias aun.\nToma una foto y guardala.");
                imgFoto.setImageBitmap(null);
            } else {
                lista.append("Total: ").append(archivos.length).append(" evidencias\n\n");
                File ultimaFoto = archivos[archivos.length - 1];
                lista.append("Mostrando:\n").append(ultimaFoto.getName());
                Bitmap fotoGuardada = BitmapFactory.decodeFile(ultimaFoto.getAbsolutePath());
                if (fotoGuardada != null) {
                    imgFoto.setImageBitmap(fotoGuardada);
                }
            }
        }
        Toast.makeText(this, lista.toString(), Toast.LENGTH_LONG).show();
    }
}