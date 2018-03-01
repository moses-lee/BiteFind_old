package com.wordpress.necessitateapps.bitefind.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.wordpress.necessitateapps.bitefind.R;
import com.wordpress.necessitateapps.bitefind.WebActivity;

public class AboutFragment extends Fragment {

    TextView textLibraries, textVersion, textPrivacy, textContact;
    ImageView imageLogo;
    Vibrator vibrate;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_about, container, false);

        vibrate = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        textLibraries=view.findViewById(R.id.text_libraries);
        textVersion=view.findViewById(R.id.text_version);
        textContact=view.findViewById(R.id.text_contact);
        textPrivacy=view.findViewById(R.id.text_privacy);
        imageLogo =view.findViewById(R.id.image_logo);

        imageLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://necessitateapps.wordpress.com/";
                webPage(url);
            }
        });

        textPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://necessitateapps.github.io/BiteFind/privacy_policy.html";
                webPage(url);
            }
        });

        textLibraries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://necessitateapps.github.io/BiteFind/libraries.html";
                webPage(url);
            }
        });

        textContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mailto = "mailto:necessitatedev@gmail.com" +
                        "?subject=" + Uri.encode("BiteFind Inquiry");

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(mailto));
                startActivity(emailIntent);
            }
        });

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            textVersion.setText(version);
            easterEgg(textVersion);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        return view;
    }

    private void webPage(String url){
        Intent i=new Intent(getActivity(), WebActivity.class);
        i.putExtra("url", url);
        startActivity(i);
    }

    private int counter=0;
    private void easterEgg(TextView textVersion){
        textVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter++;

                if(counter==20){
                    counter=0;

                    vibrate.vibrate(1000);
                    Snackbar.make(getActivity().findViewById(R.id.about_layout),"It's Necessary", Snackbar.LENGTH_SHORT).show();
                    SharedPreferences sharedPref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    boolean easteregg=sharedPref.getBoolean("easteregg", false);

                    if(easteregg){
                        editor.putBoolean("easteregg", false);
                        editor.apply();
                    }else{
                        editor.putBoolean("easteregg", true);
                        editor.apply();
                    }
                }
            }
        });

    }
}
