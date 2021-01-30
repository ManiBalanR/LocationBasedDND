package com.example.mapgoogleplaces;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public class PlaceInfo {

    private String id;
    private String name;
    private String address;
    private String latlng;

//    private Uri websiteUri;
//    private float rating;
//    private String attributions;

    public PlaceInfo(String id, String name, String address, String latlng) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latlng = latlng;
    }

    public PlaceInfo() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }




}