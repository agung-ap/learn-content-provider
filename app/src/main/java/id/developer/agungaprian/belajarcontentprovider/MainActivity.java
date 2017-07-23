package id.developer.agungaprian.belajarcontentprovider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button addName, retriveStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addName = (Button) findViewById(R.id.add_name);
        retriveStudent = (Button) findViewById(R.id.retrieve_student);

        addName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add a new student record
                ContentValues values = new ContentValues();
                values.put(StudentProvider.NAME,
                        ((EditText)findViewById(R.id.name_et)).getText().toString());

                values.put(StudentProvider.GRADE,
                        ((EditText)findViewById(R.id.grade_et)).getText().toString());

                Uri uri = getContentResolver().insert(
                        StudentProvider.CONTENT_URI, values);

                Toast.makeText(getBaseContext(),
                        uri.toString(), Toast.LENGTH_LONG).show();
            }
        });

        retriveStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //retrieve student record
                String URL = "content://com.example.MyApplication.StudentProvider";

                Uri students = Uri.parse(URL);
                Cursor cursor = managedQuery(students, null, null, null, "name");

                if (cursor.moveToFirst()){
                    do {
                        Toast.makeText(MainActivity.this, cursor.getString(cursor.getColumnIndex((StudentProvider._ID))) +
                                ", " + cursor.getString(cursor.getColumnIndex(StudentProvider.NAME)) +
                                ", " + cursor.getString(cursor.getColumnIndex(StudentProvider.GRADE)),
                                Toast.LENGTH_SHORT).show();
                    }while (cursor.moveToNext());
                }
            }
        });
    }
}
