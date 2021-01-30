package com.example.mapgoogleplaces;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddLocations extends AppCompatActivity {

    private static final String TAG = "AddLocation";


    EditText place_name,place_address,place_latlng;
    Button addlocation;
    ListView mplacelist;

    DatabaseReference databasePlaces;
    List<PlaceInfo> placesList;
    String id,name,address,latlng,iname;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addlocations);

        Intent intent = getIntent();
        databasePlaces = FirebaseDatabase.getInstance().getReference("Marked Places");

        place_name = findViewById(R.id.editText_name);
        place_address = findViewById(R.id.editText_Address);
        place_latlng = findViewById(R.id.editText_latlng);
        addlocation = findViewById(R.id.btn_addlocation);
        mplacelist = findViewById(R.id.mplacelist);

        placesList = new ArrayList<>();

        Log.e(TAG, "name is set: " +intent.getStringExtra("name"));
        place_name.setText(intent.getStringExtra("name"));
        place_address.setText(intent.getStringExtra("address"));
        Log.e(TAG, "name is set: " +intent.getStringExtra("address"));
        place_latlng.setText(intent.getStringExtra("latlng"));
        Log.e(TAG, "name is set: " +intent.getStringExtra("latlng"));

        addlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        databasePlaces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                placesList.clear();

                for(DataSnapshot eventSnapshot : dataSnapshot.getChildren()){

                    PlaceInfo place = eventSnapshot.getValue(PlaceInfo.class);
                    placesList.add(place);
//                   System.out.println(eventsList);
                }

                PlacesList adapter = new PlacesList(AddLocations.this,placesList);
                mplacelist.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addPlace(){

        name = place_name.getText().toString();
        address = place_address.getText().toString();
        latlng = place_latlng.getText().toString();


        // Toast.makeText(getActivity(),name,Toast.LENGTH_SHORT).show();
        //eshow.setText(s);

        if(!TextUtils.isEmpty(latlng)){


            id = databasePlaces.push().getKey();
            PlaceInfo place = new PlaceInfo(id,name,address,latlng);
            databasePlaces.child(id).setValue(place);
            place_name.setText("");
            place_address.setText("");
            place_latlng.setText("");

            Toast.makeText(this,"Place Marked",Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(this,"enter all fields",Toast.LENGTH_SHORT).show();
        }



    }
}
