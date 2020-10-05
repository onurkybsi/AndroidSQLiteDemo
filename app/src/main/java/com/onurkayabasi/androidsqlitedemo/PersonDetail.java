package com.onurkayabasi.androidsqlitedemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PersonDetail extends AppCompatActivity {

    public Bitmap selectedImg;
    private ImageView image;
    private EditText firstName, lastName, phoneNumber;
    private Button saveButton;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_detail);

        image = findViewById(R.id.image);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        phoneNumber = findViewById(R.id.phoneNumber);
        saveButton = findViewById(R.id.saveButton);

        db = this.openOrCreateDatabase("People", MODE_PRIVATE, null);
    }

    public void selectImage(View view) {
        // izin alınmamissa izin iste:
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            goToGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                goToGallery();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri imgData = data.getData();

            try {
                if(Build.VERSION.SDK_INT < 28) {
                    selectedImg = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgData);
                } else {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imgData);
                    selectedImg = ImageDecoder.decodeBitmap(source);
                }

                image.setImageBitmap(selectedImg);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void savePerson(View view) {
        String firstNameText = firstName.getText().toString();
        String lastNameText = lastName.getText().toString();
        String phoneNumberText = phoneNumber.getText().toString();

        Bitmap fixedImg = fixBitmapSize(selectedImg, 250);

        ByteArrayOutputStream selectedImgOutputStream = new ByteArrayOutputStream();
        fixedImg.compress(Bitmap.CompressFormat.PNG, 50, selectedImgOutputStream);
        byte[] selectedImgByteArray = selectedImgOutputStream.toByteArray();

    }

    private void goToGallery() {
        Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intentToGallery, 2);
    }

    private Bitmap fixBitmapSize(Bitmap img, int maxSize){
        int width = img.getWidth();
        int height = img.getHeight();

        float bitmapRatio = (float)width / (float)height;

        if(bitmapRatio > 1) {
            width = maxSize;
            height = (int)(width/ bitmapRatio);
        } else {
            height = maxSize;
            width = (int)(width * bitmapRatio);
        }

        return  Bitmap.createScaledBitmap(img, width, height, true);
    }
}