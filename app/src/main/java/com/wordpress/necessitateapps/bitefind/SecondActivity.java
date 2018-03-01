package com.wordpress.necessitateapps.bitefind;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.wordpress.necessitateapps.bitefind.Fragments.AboutFragment;
import com.wordpress.necessitateapps.bitefind.Fragments.SavedFragment;

public class SecondActivity extends AppCompatActivity {

    FragmentManager fragmentManager = getSupportFragmentManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        //back button action
        ImageView imageBack=findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView textTitle=findViewById(R.id.text_title);

        char type=getIntent().getExtras().getChar("type");

        if(type=='a'){
            textTitle.setText("About");
            fragmentManager.beginTransaction().add(R.id.frame, new AboutFragment()).commit();
        }
        if(type=='s'){
            textTitle.setText("Saved");
            fragmentManager.beginTransaction().add(R.id.frame, new SavedFragment()).commit();
        }

        if(!BuildConfig.DEBUG)
            getAds();
    }


    private void getAds(){
        AdView adView=findViewById(R.id.adView);
        AdRequest adRequest;

        MobileAds.initialize(this, getResources().getString(R.string.AD_ID));
        adRequest=new AdRequest.Builder().build();

        adView.loadAd(adRequest);
    }


}
