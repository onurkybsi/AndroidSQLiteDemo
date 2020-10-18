package com.onurkayabasi.androidsqlitedemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.onurkayabasi.Entity.Person;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PersonDetail extends AppCompatActivity {

    public Bitmap selectedImg;
    private ImageView image;
    private EditText firstName, lastName, phoneNumber;
    private Button saveButton;
    private SQLiteDatabase db;
    private String saveType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_detail);

        Intent intent = getIntent();
        saveType = intent.getStringExtra("saveType");

        image = findViewById(R.id.image);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        phoneNumber = findViewById(R.id.phoneNumber);
        saveButton = findViewById(R.id.saveButton);
        selectedImg = BitmapFactory.decodeResource(getResources(), R.drawable.default_person);

        db = this.openOrCreateDatabase("People", MODE_PRIVATE, null);

        if (saveType.equals("update")) {
            int editedId = intent.getIntExtra("id", 0);

            fillTScreenValuesWithEditedPerson(editedId);
        }
    }

    public void selectImage(View view) {
        // izin alınmamissa izin iste:
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            goToGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goToGallery();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri imgData = data.getData();

            try {
                if (Build.VERSION.SDK_INT < 28) {
                    selectedImg = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgData);
                } else {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imgData);
                    selectedImg = ImageDecoder.decodeBitmap(source);
                }

                image.setImageBitmap(selectedImg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void savePerson(View view) {
        Person savedPerson = getPersonValuesFromScreen();

        if (savedPerson == null) {
            Toast.makeText(getApplicationContext(), "Please enter values!", Toast.LENGTH_LONG).show();

            return;
        }

        boolean savePersonToDbResult = savePersonToDb(savedPerson);

        if (savePersonToDbResult) {
            Intent intent = new Intent(PersonDetail.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "An error occured please try later.", Toast.LENGTH_LONG).show();
        }
    }

    private void goToGallery() {
        Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intentToGallery, 2);
    }

    private Bitmap fixBitmapSize(Bitmap img, int maxSize) {
        int width = img.getWidth();
        int height = img.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (width * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(img, width, height, true);
    }

    private Person getPersonValuesFromScreen() {
        String firstNameText = firstName.getText().toString();
        String lastNameText = lastName.getText().toString();
        String phoneNumberText = phoneNumber.getText().toString();

        boolean validationResult = validateScreenValues(firstNameText, lastNameText, phoneNumberText);

        if (!validationResult)
            return null;

        Person person = new Person();

        person.firstName = firstNameText;
        person.lastName = lastNameText;
        person.phoneNumber = phoneNumberText;

        Bitmap fixedImg = fixBitmapSize(selectedImg, 250);

        ByteArrayOutputStream selectedImgOutputStream = new ByteArrayOutputStream();
        fixedImg.compress(Bitmap.CompressFormat.PNG, 50, selectedImgOutputStream);
        byte[] selectedImgByteArray = selectedImgOutputStream.toByteArray();

        person.imgByteArray = selectedImgByteArray;

        return person;
    }

    private boolean savePersonToDb(Person person) {
        boolean saveProcessResult = true;

        try {

            db = this.openOrCreateDatabase("People", MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS People (Id INTEGER PRIMARY KEY,FirstName VARCHAR, LastName VARCHAR, PhoneNumber VARCHAR, Image BLOB)");


            String sqlString = "INSERT INTO People (FirstName, LastName, PhoneNumber, Image) VALUES (?, ?, ?, ?)";

            SQLiteStatement sqLiteStatement = db.compileStatement(sqlString);
            sqLiteStatement.bindString(1, person.firstName);
            sqLiteStatement.bindString(2, person.firstName);
            sqLiteStatement.bindString(3, person.phoneNumber);
            sqLiteStatement.bindBlob(4, person.imgByteArray);

            sqLiteStatement.execute();
        } catch (Exception e) {
            saveProcessResult = false;
        }

        return saveProcessResult;
    }

    private boolean validateScreenValues(String... params) {
        for (String param : params) {
            if (param == null || param.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private void fillTScreenValuesWithEditedPerson(int editedId) {
        Log.i("Edited id:", Integer.toString(editedId));

        if (editedId != 0) {
            Person editedPerson = getPerson(editedId);

            firstName.setText(editedPerson.firstName);
            lastName.setText(editedPerson.lastName);
            phoneNumber.setText(editedPerson.phoneNumber);

            Bitmap imgBitmap = BitmapFactory.decodeByteArray(editedPerson.imgByteArray, 0, editedPerson.imgByteArray.length);
            image.setImageBitmap(imgBitmap);
        }
    }

    private Person getPerson(int id) {
        Person person = new Person();

        try {

            Cursor cursor = db.rawQuery("SELECT * FROM people WHERE Id = ?", new String[]{String.valueOf(id)});

            int firstNameIx = cursor.getColumnIndex("FirstName");
            int lastNameIx = cursor.getColumnIndex("LastName");
            int phoneNumberIx = cursor.getColumnIndex("PhoneNumber");
            int imgByteArrayIx = cursor.getColumnIndex("Image");

            while (cursor.moveToNext()) {
                person.firstName = cursor.getString(firstNameIx);
                person.lastName = cursor.getString(lastNameIx);
                person.phoneNumber = cursor.getString(phoneNumberIx);
                person.imgByteArray = cursor.getBlob(imgByteArrayIx);
            }

            cursor.close();
        } catch (Exception e) {
            Log.e("getPerson", e.getMessage());
        }

        return person;
    }
}