package com.example.mapgoogleplaces;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class PlacesList extends ArrayAdapter<PlaceInfo> {

    private Activity context;
    private List<PlaceInfo> placesList;

    public PlacesList(Activity context, List<PlaceInfo> placesList){
        super(context,R.layout.activity_placeslist,placesList);
        this.context = context;
        this.placesList = placesList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View listViewPlaces = inflater.inflate(R.layout.activity_placeslist,null,true);

        TextView textView_name = listViewPlaces.findViewById(R.id.textView_name);
        TextView textView_address = listViewPlaces.findViewById(R.id.textView_address);
        TextView textView_latlng = listViewPlaces.findViewById(R.id.textView_latlng);

        PlaceInfo place = placesList.get(position);

        textView_name.setText(place.getName());
        textView_address.setText(place.getAddress());
        textView_latlng.setText(place.getLatlng());

        return listViewPlaces;

    }
}
