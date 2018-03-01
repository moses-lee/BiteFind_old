package com.wordpress.necessitateapps.bitefind;




import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class DownloadUrl {

    //gets data from Url, IOException needed for finally
    String readUrl(String myUrl) throws IOException,NullPointerException {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(myUrl)
                .build();

        Response response = client.newCall(request).execute();

        return response.body().string();
    }

}