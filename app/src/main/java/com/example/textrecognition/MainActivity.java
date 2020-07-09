package com.example.textrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText mResultET;
    ImageView mPreviewIV;

    private static final int CAMERA_REQUEST = 200;
    private static final int STORAGE_REQUEST = 400;
    private static final int IMAGE_PICK_GALLERY = 1000;
    private static final int IMAGE_PICK_CAMERA = 1001;

    String cameraPermissions[];
    String storagePermissions[];

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultET = findViewById(R.id.resultET);
        mPreviewIV = findViewById(R.id.previewIV);

        cameraPermissions = new String[]{Manifest.permission.CAMERA,
          Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }

    // Actionbar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Show menu
        getMenuInflater().inflate(R.menu.manu_main, menu);
        return true;
    }

    // Item selection in menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if ( id == R.id.addImage) {
            showImageImportDialog();
        } else if ( id == R.id.settings) {
            Toast.makeText(this,"Settings",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog() {
        String [] items = {" Camera", " Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
       dialog.setItems(items, new DialogInterface.OnClickListener()  {
           @Override
           public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Camera option
                    if ( !checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickCamera();
                    }
                } else if (which == 1) {
                    // Gallery option
                    if ( !checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickGallery();
                    }
                }
           }
       });
       dialog.create().show();
    }


    private boolean checkCameraPermission() {
        boolean camera = ContextCompat.checkSelfPermission(this,
          Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        boolean save = ContextCompat.checkSelfPermission(this,
          Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return camera && save;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions,CAMERA_REQUEST);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
          Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST);
    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPicture");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA);
    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY);

    }

    // Handle permission result
    // Check grant request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if ( grantResults.length > 0){
                    boolean cameraAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if ( cameraAccept && writeStorageAccept ) {
                        pickCamera();
                    } else {
                        Toast.makeText(this,  "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST:
                boolean writeStorageAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if ( writeStorageAccept ) {
                    pickGallery();
                } else {
                    Toast.makeText(this,  "permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }
}