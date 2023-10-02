package com.example.myapplicationbeso;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DetailActivity extends AppCompatActivity {

    TextView Age, detailTitle, Phone, ID,Email,Date,Location;
    ImageView detailImage;
    FloatingActionButton deleteButton, editButton;
    String key = "";
    String imageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Age = findViewById(R.id.d_age);
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);
        Phone = findViewById(R.id.d_phone);
        ID = findViewById(R.id.d_id);
        Email = findViewById(R.id.d_email);
        Date = findViewById(R.id.d_date);
        Location = findViewById(R.id.d_loc);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            detailTitle.setText("Name: "+bundle.getString("Title"));
            Age.setText("Age: "+bundle.getString("Age"));
            Phone.setText("Phone: "+bundle.getString("Phone"));
            ID.setText("ID: "+bundle.getString("ID"));
            Email.setText("Email: "+bundle.getString("Email"));
            Date.setText("Date: "+bundle.getString("Date"));
            Location.setText("Location: "+bundle.getString("Location"));
            key = bundle.getString("Key");
            imageUrl = bundle.getString("Image");
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);

        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Android Tutorials");
                FirebaseStorage storage = FirebaseStorage.getInstance();

                StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);
                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        reference.child(key).removeValue();
                        Toast.makeText(DetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), Home.class));
                        finish();
                    }
                });
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, UpdateActivity.class)
                        .putExtra("Title", detailTitle.getText().toString())
                        .putExtra("Age", Age.getText().toString())
                        .putExtra("Phone", Phone.getText().toString())
                        .putExtra("ID",ID.getText().toString())
                        .putExtra("Email",Email.getText().toString())
                        .putExtra("Date",Date.getText().toString())
                        .putExtra("Location",Location.getText().toString())
                        .putExtra("Image", imageUrl)
                        .putExtra("Key", key);
                startActivity(intent);
            }
        });
    }
}