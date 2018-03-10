package com.wordpress.necessitateapps.bitefind;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class DataParser {

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson, String nextPageToken){
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String placeName = "--NA--";

        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }

            JSONObject jImage = googlePlaceJson.getJSONArray("photos").getJSONObject(0);
            String image=jImage.getString("photo_reference");

            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("place_image", image);
            googlePlaceMap.put("next_page_token", nextPageToken);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlaceMap;
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray, String nextPageToken, String status){
        int count = jsonArray.length();
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap = new HashMap<>();

        if(status.equals("OK")){
            for(int i = 0; i<count;i++)
            {
                try {
                    placeMap = getPlace((JSONObject) jsonArray.get(i), nextPageToken);
                    placelist.add(placeMap);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }else{
            placeMap.put("status",status);
            placelist.add(placeMap);
        }


        return placelist;
    }


    List<HashMap<String, String>> parse(String jsonData){
        JSONArray jsonArray = null;
        String nextPageToken=null;
        String status=null;
        JSONObject jsonObject;


        try {
            jsonObject = new JSONObject(jsonData);
            status=jsonObject.getString("status");
            jsonArray = jsonObject.getJSONArray("results");
            nextPageToken =jsonObject.getString("next_page_token");

        } catch (JSONException|NullPointerException e) {
            e.printStackTrace();
        }

        return getPlaces(jsonArray,nextPageToken, status);
    }
}