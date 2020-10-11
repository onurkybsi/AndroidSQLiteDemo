package com.onurkayabasi.androidsqlitedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.onurkayabasi.Entity.Person;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Person> people;
    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        people = new ArrayList<>();

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, people);
        listView.setAdapter(arrayAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, PersonDetail.class);
                intent.putExtra("id", people.get(i).Id);
                intent.putExtra("saveType", "update");
                startActivity(intent);
            }
        });

        setPeopleSource();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Bir xml'i aktivite içinde gosterebilmek icin: Inflater
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_person, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.addPerson) {
            Intent toPersonDetail = new Intent(MainActivity.this, PersonDetail.class);
            startActivity(toPersonDetail);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setPeopleSource() {
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("People",MODE_PRIVATE,null);

            Cursor cursor = database.rawQuery("SELECT * FROM People", null);
            int idIx = cursor.getColumnIndex("Id");
            int firstNameIx = cursor.getColumnIndex("FirstName");

            while (cursor.moveToNext()) {
                Person person = new Person();

                person.Id = cursor.getInt(idIx);
                person.firstName = cursor.getString(firstNameIx);

                people.add(person);
            }

            arrayAdapter.notifyDataSetChanged();

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}