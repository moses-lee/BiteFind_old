package com.wordpress.necessitateapps.bitefind;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

public class WebActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        //back button action
        ImageView imageBack=findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String url=getIntent().getExtras().getString("url");
        WebView webView=findViewById(R.id.webView);


        if(url!=null){
            webView.loadUrl(url);
        }

    }

}
