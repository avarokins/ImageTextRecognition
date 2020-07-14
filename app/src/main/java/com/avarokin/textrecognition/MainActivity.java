package com.avarokin.textrecognition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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
        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle("Click the image icon");

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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

    // Handle image result. Crop it
    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON)
                  .start(this);
            }
            if (requestCode == IMAGE_PICK_CAMERA) {
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON)
                  .start(this);
            }
        }

        // Once cropped
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resulUri = result.getUri();
                mPreviewIV.setImageURI(resulUri);

                // Drawable bitmap for text recognition
                BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreviewIV.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if (recognizer.isOperational()) {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();

                    // Get text from stream
                    for (int i = 0; i < items.size(); i++) {
                        TextBlock item = items.valueAt(i);
                        sb.append(item.getValue());
                        sb.append('\n');
                    }

                    // Set result
                    mResultET.setText(sb.toString());

                } else {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception e = result.getError();
                Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
            }
        }
    }


} // class MainActivity