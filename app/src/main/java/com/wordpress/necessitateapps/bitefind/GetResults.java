package com.wordpress.necessitateapps.bitefind;


import android.os.AsyncTask;
import android.util.Log;


import java.io.IOException;

import java.util.HashMap;
import java.util.List;

public class GetResults extends AsyncTask<Object, String, String> {



    @Override
    protected String doInBackground(Object... object) {
        //gets url
        String googlePlacesData=null;
        String url = (String)object[0];

        //receives url in object form from MainActivity
        //sends user to DownloadUrl to get data
        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googlePlacesData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    // Async Response,returns nearbyPlaceList : List<HashMap<String, String>>
    public interface AsyncResponse {
        void processFinish(List<HashMap<String, String>> nearbyPlaceList, String nextPageToken, String status);
    }
    private AsyncResponse delegate = null;
    GetResults(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute(String s){
        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);

        String status=nearbyPlaceList.get(0).get("status");

        //checks if there was an error& for next page token
        if(status==null||status.isEmpty()){
            HashMap<String, String> nextPageHash = nearbyPlaceList.get(0);
            String nextPageToken = nextPageHash.get("next_page_token");

            //remove all the tokens
            for(int i=0; i<nearbyPlaceList.size();i++) {
                nearbyPlaceList.get(i).remove("next_page_token");
            }

            if(nearbyPlaceList.isEmpty()){
                nearbyPlaceList=null;
            }
            //passes finished result
            delegate.processFinish(nearbyPlaceList, nextPageToken, null);

        }else{
            delegate.processFinish(null, null, status);
        }

    }






}
