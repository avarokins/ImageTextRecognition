package com.example.textrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                        takePicture();
                    }

                } else if (which == 1) {
                    // Gallery option
                }
           }
       });
       dialog.create().show();
    }
}