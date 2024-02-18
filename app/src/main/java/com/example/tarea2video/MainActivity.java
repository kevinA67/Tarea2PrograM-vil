package com.example.tarea2video;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Uri uriImgVid;
    ImageView imgView;
    VideoView videoView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICK = 2;
    static final int REQUEST_VIDEO_CAPTURE = 3;
    static final int REQUEST_VIDEO_PICK = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1000);
        }


        imgView = (ImageView) findViewById(R.id.imageView);
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVisibility(View.INVISIBLE);

    }


    public void grabarVideo(View view) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }


    public void capturarFoto(View view) {
        Intent intentFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intentFoto.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intentFoto, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void galeria(View view) {
        Intent intentGaleria = new Intent(Intent.ACTION_PICK);
        intentGaleria.setType("image/* video/*");
        try {
            startActivityForResult(Intent.createChooser(intentGaleria, "Seleccione imagen o video"), REQUEST_IMAGE_PICK);
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir la galería.", Toast.LENGTH_LONG).show();
        }
    }

    public void compartir(View view) {
        Intent intentCompartir = new Intent(Intent.ACTION_SEND);
        intentCompartir.setType("image/* video/*");
        //intentCompartir.setPackage("*");

        if (uriImgVid != null) {
            intentCompartir.putExtra(Intent.EXTRA_STREAM, uriImgVid);
            try {
                startActivity(intentCompartir);

            } catch (Exception e) {
                Toast.makeText(this, "Error al compartir\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No se seleccionó una imagen.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Obtén el Bitmap de la foto capturada
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            imgView.setImageBitmap(imageBitmap);
            videoView.setVisibility(View.INVISIBLE);
            imgView.setVisibility(View.VISIBLE);

            guardarImagen(imageBitmap);

        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            uriImgVid = data.getData();
            videoView.setVideoURI(uriImgVid);
            imgView.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);
            videoView.start();

        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                uriImgVid = data.getData();
                String mimeType = getContentResolver().getType(uriImgVid);
                if (mimeType != null && mimeType.startsWith("image")) {
                    imgView.setImageURI(uriImgVid);
                    videoView.setVisibility(View.INVISIBLE);
                    imgView.setVisibility(View.VISIBLE);
                } else {
                    videoView.setVideoURI(uriImgVid);
                    imgView.setVisibility(View.INVISIBLE);
                    videoView.setVisibility(View.VISIBLE);
                    videoView.start();
                }
            }
        }
    }


    private void guardarImagen(Bitmap imageBitmap) {
        // Asegúrate de que el almacenamiento externo esté disponible para escritura
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Crea un directorio para guardar las imágenes si no existe
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyApp");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            // Crea un nombre de archivo único para la imagen
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "IMG_" + timeStamp + ".jpg";
            // Guarda la imagen en un archivo en el directorio creado
            File imageFile = new File(directory, imageFileName);
            try {
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                // Notifica al sistema que se ha agregado una nueva imagen
                MediaScannerConnection.scanFile(this,
                        new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                uriImgVid = uri; // Establecer la URI de la imagen guardada
                                // Muestra un mensaje de éxito
                                // Toast.makeText(MainActivity.this, "Imagen guardada en " + path, Toast.LENGTH_LONG).show();

                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
                // Muestra un mensaje de error si ocurre un problema al guardar la imagen
                Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_LONG).show();
            }
        } else {
            // Muestra un mensaje si el almacenamiento externo no está disponible
            Toast.makeText(this, "El almacenamiento externo no está disponible", Toast.LENGTH_LONG).show();
        }
    }
}