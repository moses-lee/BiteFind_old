package com.wordpress.necessitateapps.bitefind;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;


class UrlBuilder {

    String getUrl(Context mContext, String nextPageToken) {

        //initialize sharedpreferences
        SharedPreferences sharedPref = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        Preferences defPref=new Preferences();
        String defJson = gson.toJson(defPref);
        String json = sharedPref.getString("prefClass", defJson);
        Preferences pref = gson.fromJson(json, Preferences.class);

        //get the values from SP

        int radius=pref.radius;
        int cost=pref.cost;
        boolean opennow=pref.opennow;
        String keyword=pref.keyword;

        //get latlng from pref
        //grabs string lat & lng because getDouble does not exist
        String lat=pref.lat;
        String lng=pref.lng;

        //Builds string
        StringBuilder googlePlaceUrl= new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");


        //converts string to lat&lng, which then converts it to LatLng value
        googlePlaceUrl.append("location="+lat+","+lng);

        //build type
        googlePlaceUrl.append("&type="+"restaurant");

        //build custom keyword
        if(!keyword.equals("Any")){
            googlePlaceUrl.append("&keyword="+keyword);
        }

        //build radius
        switch (radius){
            case 0:
                radius=6000;
                break;
            case 1:
                radius=9000;
                break;
            case 2:
                radius=17000;
                break;
            case 3:
                radius=25000;
                break;
        }
        googlePlaceUrl.append("&radius="+radius);



        //build cost
        if(cost!=0){
            switch (cost) {
                case 1:
                    googlePlaceUrl.append("&minprice=0");
                    googlePlaceUrl.append("&maxprice=1");
                    break;
                case 2:
                    googlePlaceUrl.append("&minprice=1");
                    googlePlaceUrl.append("&maxprice=2");
                    break;
                case 3:
                    googlePlaceUrl.append("&minprice=3");
                    googlePlaceUrl.append("&maxprice=4");
                    break;
            }
        }

        //build open now
        if(opennow){
            googlePlaceUrl.append("&opennow="+String.valueOf(opennow));
        }

        //build key+sensor
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+ mContext.getResources().getString(R.string.API_KEY));

        if(nextPageToken!=null&&!nextPageToken.isEmpty()){
            googlePlaceUrl.append("&pagetoken="+ nextPageToken);
        }

        Log.v("LOG::URL", googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    //algorithm for 2.0
    private void randAlgo(){

    }
}
