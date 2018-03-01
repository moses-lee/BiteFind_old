package com.wordpress.necessitateapps.bitefind;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;

import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.skyfishjy.library.RippleBackground;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements GetResults.AsyncResponse {

    private final static int PERMISSION_ACCESS_COARSE_LOCATION = 10;
    private final static int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private Location location;
    private FusedLocationProviderClient mFusedLocationClient;
    private TextView textCost, textRadius, textLocation, textWarning;
    private SeekBar barCost, barRadius;
    private Switch switchOpen;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private int valRadius, valCost;
    private ProgressBar progressBar;
    private Spinner spinnerKeyword;
    private String mLocation=null;
    private Vibrator vibrate;
    private RippleBackground rippleBackground;
    private SlidingUpPanelLayout slidingLayout;
    private Preferences pref;
    private Gson gson;

    //TODO: REMOVE/ADD TOKEN CODE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //sets the toolbar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //gets rid of toolbar title
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);



        //initializes views
        progressBar=findViewById(R.id.progressBar);
        textRadius=findViewById(R.id.text_radius);
        textCost=findViewById(R.id.text_cost);
        barRadius=findViewById(R.id.bar_radius);
        barCost=findViewById(R.id.bar_cost);
        switchOpen=findViewById(R.id.switch_open);
        spinnerKeyword=findViewById(R.id.spinner_keyword);
        textLocation=findViewById(R.id.text_location);
        textWarning=findViewById(R.id.text_warning);
        textLocation.setPaintFlags(textLocation.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        vibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        slidingLayout=findViewById(R.id.sliding_layout);
        rippleBackground = findViewById(R.id.ripple);

        //initialize sharedpref
        //BOY WHAT EVEN IS THIS CODE
        sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        gson = new Gson();
        Preferences defPref=new Preferences();
        String defJson = gson.toJson(defPref);
        String json = sharedPref.getString("prefClass", defJson);
        pref = gson.fromJson(json, Preferences.class);


        //empty=first time
        //location_auto==auto
        //manual_null=manual without location
        //"location=manual with location
        mLocation=sharedPref.getString("location", "");

        //sets pref controls from sharedpreferences
        setPref();

        textLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLocation.isEmpty()){
                    //asks for location preferences if first time pressing
                    Snackbar.make(findViewById(R.id.layout),"You Can Always Change Location Preferences by Long Clicking on Bottom Text", 4000).show();
                    askLocationPref(false);
                }else
                    if(mLocation.equals("location_auto")){
                        Snackbar.make(findViewById(R.id.layout),"Long Click to Change Location Preferences", Snackbar.LENGTH_SHORT).show();
                    }else{
                        getManualLocation();
                }

            }
        });

        textLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //vibrate
                if (vibrate != null) {
                    vibrate.vibrate(20);
                }
                //change location pref
                askLocationPref(false);
                return true;
            }
        });

        //image click action
        ImageView imageRand =findViewById(R.id.image_rand);
        imageRand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //vibrate
                if (vibrate != null) {
                    vibrate.vibrate(30);
                }

                //makes sure button isn't clicked multiple times
                if(progressBar.getVisibility()==View.GONE){
                    buttonClick();
                }

            }
        });


        //initialize location service
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //prevents settings being closed with a click
        LinearLayout settingsLayout=findViewById(R.id.settings_layout);
        settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        //gets ads
        if(!BuildConfig.DEBUG)
            getAds();

        askRating();
        easterEgg();

    }

    //LIFECYCLES
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onStart() {
        super.onStart();
        rippleBackground.startRippleAnimation();

        easterEgg();
    }


    @Override
    protected void onStop() {
        super.onStop();
        rippleBackground.stopRippleAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rippleBackground.stopRippleAnimation();
    }

    private void askRating(){
        //asks to rate every 10 times the app is open
        final int ratingCounter=sharedPref.getInt("rating_count", 0);

        //checks if user have asked to stop seeing this
        if(ratingCounter!=-1){
            editor.putInt("rating_count", ratingCounter+1);
            editor.apply();

            if(ratingCounter%8==0&&ratingCounter!=0){
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
                mBuilder.setMessage("Enjoying BiteFind?\nWe Would Appreciate Your Rating!");
                mBuilder.setCancelable(true);

                mBuilder.setPositiveButton(
                        "Rate",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.wordpress.necessitateapps.bitefind"));
                                startActivity(i);
                                dialog.dismiss();
                            }
                        });
                mBuilder.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                mBuilder.setNeutralButton(
                        "Never",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                editor.putInt("rating_count", -1);
                                editor.apply();
                                dialog.dismiss();
                            }
                        });


                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        }

    }

    private void easterEgg(){
        ConstraintLayout constraintLayout=findViewById(R.id.layout);
        LinearLayout bottomLayout=findViewById(R.id.bottomLayout);
        LottieAnimationView imageEgg=findViewById(R.id.animation_view);

        boolean easterEgg=sharedPref.getBoolean("easteregg", false);
        if(!easterEgg){
            constraintLayout.setBackground(ContextCompat.getDrawable(this,R.drawable.gradient));
            bottomLayout.setBackground(ContextCompat.getDrawable(this,R.drawable.gradient_settings));

            imageEgg.setVisibility(View.GONE);
        }else{
            constraintLayout.setBackground(ContextCompat.getDrawable(this,R.drawable.gradient_egg));
            bottomLayout.setBackgroundColor(Color.parseColor("#633596"));
            imageEgg.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //gets the menu from menu folder
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        //menu button functions
        //a=inflate about fragment
        //s=inflate saved fragment
        switch (item.getItemId()) {
            case R.id.menu_about:
                Intent about_intent=new Intent(this, SecondActivity.class);
                about_intent.putExtra("type", 'a');
                startActivity(about_intent);
                break;
            case R.id.menu_saved:
                Intent saved_intent=new Intent(this, SecondActivity.class);
                saved_intent.putExtra("type", 's');
                startActivity(saved_intent);
                break;

        }

        return true;
    }


    private void buttonClick(){

        if(mLocation.isEmpty()){
            Snackbar.make(findViewById(R.id.layout),"You Can Always Change Location Preferences by Long Clicking on Bottom Text", Snackbar.LENGTH_LONG).show();
            askLocationPref(true);
        }else {

            if (mLocation.equals("location_auto")) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_ACCESS_COARSE_LOCATION);
                } else {
                    getAutoLocation();
                }
            } else
                if (mLocation.equals("manual_null")) {
                getManualLocation();
                } else {
                    startAsync(null);
            }
        }
    }


    private void askLocationPref(final boolean buttonClick){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setMessage("Location Preferences:");
        mBuilder.setCancelable(true);

        mBuilder.setPositiveButton(
                "Manual",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(mLocation.equals("manual_null")||mLocation.equals("location_auto")||mLocation.isEmpty()){
                            mLocation="manual_null";
                            editor.putString("location", mLocation);
                            editor.apply();
                            getManualLocation();
                            if(buttonClick)
                                buttonClick();
                            setLocationText();
                        }

                        dialog.dismiss();
                    }
                });

        mBuilder.setNegativeButton(
                "Auto",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mLocation="location_auto";
                        editor.putString("location", mLocation);
                        editor.apply();
                        if(buttonClick)
                            buttonClick();
                        setLocationText();
                        dialog.dismiss();
                    }
                });


        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }



    //open autocomplete activity
    private void getManualLocation(){
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setCountry("US")
                    .build();
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(typeFilter)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Snackbar.make(findViewById(R.id.layout),"Error: "+e, Snackbar.LENGTH_SHORT).show();
        }
    }

    //getting place back from auto complete
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                //gets name of location
                mLocation=place.getName().toString();
                //saves to shared preferences
                editor.putString("location", mLocation);
                pref.lat=String.valueOf(place.getLatLng().latitude);
                pref.lng=String.valueOf(place.getLatLng().longitude);
                saveClass();
                setLocationText();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Snackbar.make(findViewById(R.id.layout),"Error: "+status, Snackbar.LENGTH_SHORT).show();
            } else
                if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getAutoLocation();
                } else {
                    //no to permission
                    Snackbar.make(findViewById(R.id.layout),"Permission Denied", Snackbar.LENGTH_SHORT).show();
                }

                break;
        }
    }



    //gets last known location and saves lat&lng value to sp
    private void getAutoLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        location = task.getResult();
                        pref.lat=String.valueOf(location.getLatitude());
                        pref.lng=String.valueOf(location.getLongitude());
                        saveClass();
                        startAsync(null);

                    } else {
                        Snackbar.make(findViewById(R.id.layout),"Location Must be Turned on for Auto Location", Snackbar.LENGTH_LONG).show();
                    }
                    }
                });
        }

    }


    private void startAsync(String nextPageToken){
        //gets pref from SharedPreferences
        //sends Context
        //sends completed url to ResultHolder class

        //show progress
        progressBar.setVisibility(View.VISIBLE);

        UrlBuilder urlBuilder=new UrlBuilder();
        final GetResults getResults=new GetResults(this);
        Context mContext=MainActivity.this;

        //getUrl returns URL string
        String url= urlBuilder.getUrl(mContext, nextPageToken);

        final Object objectUrl[]=new Object[1];
        objectUrl[0] = url;

        //have to delay if page token exists
        //TODO:FIX
       /** if(nextPageToken!=null){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getResults.execute(objectUrl);
                }
            }, 2000);
        }else{
            getResults.execute(objectUrl);
        }*/
        getResults.execute(objectUrl);

    }



    private void setPref(){

        //sets radius
        valRadius = pref.radius;
        //sets bar to radius value
        barRadius.setProgress(valRadius);

        //sets txt below to the radius value
        switch (valRadius) {
            case 0:
                textRadius.setText(getResources().getString(R.string.miles,3));
                break;
            case 1:
                textRadius.setText(getResources().getString(R.string.miles,5));
                break;
            case 2:
                textRadius.setText(getResources().getString(R.string.miles,10));
                break;
            case 3:
                textRadius.setText(getResources().getString(R.string.miles,15));
                break;
            default:
                Toast.makeText(this, "Error: SP:R", Toast.LENGTH_LONG).show();
        }



        //sets cost
        valCost = pref.cost;
        //sets bar to cost value
        barCost.setProgress(valCost);
        //sets txt below to the radius value
        switch (valCost) {
            case 0:
                textCost.setText(getResources().getString(R.string.any));
                break;
            case 1:
                textCost.setText("$");
                break;
            case 2:
                textCost.setText("$$");
                break;
            case 3:
                textCost.setText("$$$");
                break;
            default:
                Toast.makeText(this, "Error: SP:C", Toast.LENGTH_LONG).show();
        }

        //sets open button
        if(pref.opennow){
            switchOpen.setChecked(true);
        }else{
            switchOpen.setChecked(false);
        }

        //sets custom choice
        String[] arrayKeyword=getResources().getStringArray(R.array.rest_array);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.rest_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        // Apply the adapter to the spinner
        spinnerKeyword.setAdapter(adapter);
        //spinner selection in the beginning
        if(pref.keyword.equals("Any")){
            spinnerKeyword.setSelection(0);
        }else {
            textWarning.setVisibility(View.VISIBLE);
            String mGenre = pref.keyword;
            int index = 0;
            for (int i = 0; i < arrayKeyword.length; i++) {
                if (arrayKeyword[i].equals(mGenre)) {
                    index = i;
                    break;
                }

            }
            spinnerKeyword.setSelection(index);
        }

        setLocationText();
        //gets pref changes made by the user
        getPref();

    }

    private void setLocationText(){
        //set location text
        switch (mLocation){
            case "":
                textLocation.setText(getResources().getText(R.string.set));
                break;
            case"location_auto":
                textLocation.setText(getResources().getText(R.string.auto));
                break;
            case "manual_null":
                textLocation.setText(getResources().getText(R.string.manual));
                break;
            default:
                textLocation.setText(mLocation);
        }
    }

    private void saveClass(){
        String json = gson.toJson(pref);
        editor.putString("prefClass", json);
        editor.commit();
    }

    private void getPref(){
        //listens to radius changes
        barRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                valRadius=progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String msg = null;

                switch(valRadius){
                    case 0:
                        msg=getResources().getString(R.string.miles,3);
                        break;
                    case 1:
                        msg=getResources().getString(R.string.miles,5);
                        break;
                    case 2:
                        msg=getResources().getString(R.string.miles,10);
                        break;
                    case 3:
                        msg=getResources().getString(R.string.miles,15);
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Error: SP:R2", Toast.LENGTH_LONG).show();
                }

                pref.radius=valRadius;
                saveClass();
                textRadius.setText(msg);
            }
        });

        //listens to cost changes
        barCost.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                valCost=progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String msg = null;

                switch(valCost){
                    case 0:
                        msg=getResources().getString(R.string.any);
                        break;
                    case 1:
                        msg="$";
                        break;
                    case 2:
                        msg="$$";
                        break;
                    case 3:
                        msg="$$$";
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Error: SP:R2", Toast.LENGTH_LONG).show();
                }

                pref.cost=valCost;
                saveClass();
                textCost.setText(msg);
            }
        });

        //listens to open now button changes
        switchOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pref.opennow=true;
                } else {
                    pref.opennow=false;
                }
                saveClass();
            }
        });


        spinnerKeyword.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //gets selected string
                String genre=parent.getItemAtPosition(position).toString();

                pref.keyword=genre;
                saveClass();

                if(genre.equals("Any")){
                    textWarning.setVisibility(View.INVISIBLE);
                }else{
                    textWarning.setVisibility(View.VISIBLE);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private String tempJson="";
    private boolean isPrefSame(){
        boolean isSame;
        String json = gson.toJson(pref);
        if(tempJson.equals(json))
            isSame=true;
        else
            isSame= false;
        tempJson=json;

         return isSame;
    }

    //this override the implemented method from AsyncResponse
    List<HashMap<String, String>> tempList=null;
    @Override
    public void processFinish(List<HashMap<String, String>> nearbyPlaceList, final String nextPageToken, String status){
        //Receives  the result fired from async class
        //of onPostExecute(result) method.
        if(status==null){
            //TODO FIX THIS S
            //Returned list is not empty
//            if (nextPageToken != null && !nextPageToken.isEmpty()) {
//                //first token
//                if (tempList == null || tempList.isEmpty()) {
//                    tempList = nearbyPlaceList;
//                    startAsync(nextPageToken);
//                } else {
//                    //second and third token
//                    tempList.addAll(nearbyPlaceList);
//                    startAsync(nextPageToken);
//                }
//            } else
//                //had tokens, but no more
//                if (tempList != null && !tempList.isEmpty()) {
//                    tempList.addAll(nearbyPlaceList);
//                    filterNull(tempList);
//                    tempList = null;
//                } else {
//                    //had no tokens in the first place
//                    filterNull(nearbyPlaceList);
//                }
//            if (nextPageToken != null && isPrefSame()) {
//                //first token
//
//            }
//
            Log.v("LOG::", String.valueOf(isPrefSame()));
            filterNull(nearbyPlaceList);
        }else{
            //1st call and list returns empty
            if(tempList==null||nearbyPlaceList==null){
                errorHandler(3, Thread.currentThread().getStackTrace()[2].getLineNumber(), status);

            }else{
                //not the first call, but the returned is empty
                filterNull(tempList);
                tempList=null;
            }
        }
    }



    private void filterNull(List<HashMap<String, String>> nearbyPlaceList){
        int length=nearbyPlaceList.size();

        //weeds out any null or empty entries
        for(int i=0;i<length;i++){
            if(nearbyPlaceList.get(i).get("place_name")==null||nearbyPlaceList.get(i).get("place_name").isEmpty()
                    ||nearbyPlaceList.get(i).get("place_image")==null||nearbyPlaceList.get(i).get("place_image").isEmpty()){
                nearbyPlaceList.remove(i);
                i--;
                length--;
            }

        }

        filterKey(nearbyPlaceList);
    }

    String[] blackList={"hotel","inn","marriott", "hilton", "residence", "golf", "shell", "resort","motel","mobil"};
    private void filterKey(List<HashMap<String, String>> nearbyPlaceList){
        int length=nearbyPlaceList.size();
        String resName, tempName;

        //removes results with any of the key words from key Array
        for(int i=0;i<length;i++){
            resName=nearbyPlaceList.get(i).get("place_name");
            tempName=resName.toLowerCase();

            for (String keyString : blackList) {

                if (tempName.contains(keyString)) {
                    nearbyPlaceList.remove(i);
                    i--;
                    length--;
                }

            }
        }

        if (nearbyPlaceList.isEmpty()) {
            errorHandler(1, Thread.currentThread().getStackTrace()[2].getLineNumber(),null);
        }else{
            randomize(nearbyPlaceList);
        }

    }


    int tempNumber=-1;
    private void randomize(List<HashMap<String, String>> nearbyPlaceList){
        int length=nearbyPlaceList.size();
        Random rand = new Random();
        int n = rand.nextInt(length);

        String resName=nearbyPlaceList.get(n).get("place_name");
        String resImage=nearbyPlaceList.get(n).get("place_image");

        //so random number don't repeat
        if(length>1){
            while(tempNumber==n){
                n = rand.nextInt(length);
            }
            resName=nearbyPlaceList.get(n).get("place_name");
            resImage=nearbyPlaceList.get(n).get("place_image");
        }
        tempNumber=n;

        resImage = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + resImage + "&key=" + getResources().getString(R.string.API_KEY);
        //sends title and image to popup method
        //Log.v("LOG:PICKED-TOTAL:", String.valueOf(n)+"-"+String.valueOf(nearbyPlaceList.size()));
        popUp(resName, resImage);
    }


    private void errorHandler(int errorCode, int source, String status) {
        progressBar.setVisibility(View.GONE);
        //error codes:
        //0=connection error
        //1=non found
        Log.v("LOG:ERROR", String.valueOf(source));
        if(status!=null) {
            if (status.equals("ERROR"))
                errorCode = 0;
            if (status.equals("ZERO_RESULTS"))
                errorCode = 1;
        }

        switch (errorCode){
            case 0:
                Snackbar.make(findViewById(R.id.layout),"Connection Error:"+source, Snackbar.LENGTH_LONG).show();
                break;
            case 1:
                Snackbar.make(findViewById(R.id.layout),"No Results Found :(\nPlease Change the Settings for More Results", Snackbar.LENGTH_LONG).show();
                break;
            default:
                Snackbar.make(findViewById(R.id.layout),status, Snackbar.LENGTH_LONG).show();
        }
    }


    private void popUp(final String resName, final String resImage) {


        //initialize dialogue
        //opens popup
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.popup, null);

        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();


        TextView textName = view.findViewById(R.id.text_name);
        final TextView textError = view.findViewById(R.id.text_error);
        ImageView imageResult = view.findViewById(R.id.image_result);
        ImageView imageRedo = view.findViewById(R.id.image_redo);
        ImageView imageSave = view.findViewById(R.id.image_save);
        ImageView imageMap = view.findViewById(R.id.image_map);
        final ProgressBar progressPop = view.findViewById(R.id.progress_pop);

        dialog.show();


        //sets Name
        textName.setText(resName);
        progressPop.setVisibility(View.VISIBLE);

        //sets image using picasso
        //makes progress bar visible while image loads
        Picasso.with(this).load(resImage).fit().centerCrop().into(imageResult, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                progressPop.setVisibility(View.GONE);

            }

            @Override
            public void onError() {
                progressPop.setVisibility(View.GONE);
                textError.setVisibility(View.VISIBLE);
            }
        });

        //dismisses dialogue
        imageRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        //sends user to google maps
        //searches non-specific location
        imageMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String redirect = "https://www.google.com/maps/search/" + resName;
                Intent map_intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(redirect));
                startActivity(map_intent);
            }
        });

        //saves restaurant in SavedClass array
        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                save(resName, resImage, dialog);

            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }


    //WORKS
    private void save(String resName, String resImage, AlertDialog dialog){
        String filename = "saved";

        LinkedHashMap hashSaved= null;

        //read from internal file
        try {
            //get hashmap as object
            FileInputStream inputStream = openFileInput(filename);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            hashSaved = (LinkedHashMap) objectInputStream.readObject();
            objectInputStream.close();
        }catch (ClassNotFoundException | IOException e){
            e.printStackTrace();
        }

        //if hashmap does not exist(user's first save), it creates a new
        if(hashSaved==null){
            hashSaved=new LinkedHashMap();
            hashSaved.put(resName, resImage);
        }else{

            hashSaved.put(resName, resImage);
        }

        //write to internal file, save hashmap as object
        try {
            FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(hashSaved);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.dismiss();
        Snackbar.make(findViewById(R.id.layout),"Restaurant Saved!", Snackbar.LENGTH_SHORT).show();
    }

    private void getAds(){
        AdView adView=findViewById(R.id.adView);
        AdRequest adRequest;

        MobileAds.initialize(this, getResources().getString(R.string.AD_ID));
        adRequest=new AdRequest.Builder().build();

        adView.loadAd(adRequest);
    }


    @Override
    public void onBackPressed() {
        if(slidingLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }else{
            super.onBackPressed();
        }
    }
}



