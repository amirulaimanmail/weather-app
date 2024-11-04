package com.example.weatherapp.booktutorial;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.MyDatabaseHelper;
import com.example.weatherapp.R;

public class AddBookActivity extends AppCompatActivity {

    EditText title_input, author_input, pages_input;
    Button add_book_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        title_input = findViewById(R.id.title_input);
        author_input = findViewById(R.id.author_input);
        pages_input = findViewById(R.id.pages_input);

        add_book_button = findViewById(R.id.submit_button);
        add_book_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDatabaseHelper myDB = new MyDatabaseHelper(AddBookActivity.this);
                myDB.addBook(title_input.getText().toString().trim(),
                        author_input.getText().toString().trim(),
                        Integer.parseInt(pages_input.getText().toString().trim()));
                        finish();
            }
        });


    }
}