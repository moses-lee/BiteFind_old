package com.wordpress.necessitateapps.bitefind.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.wordpress.necessitateapps.bitefind.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



public class SavedFragment extends Fragment {

    private List<SavedGetter> savedList = new ArrayList<>();
    SavedAdapter mAdapter=null;
    private TextView textEmpty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_saved, container, false);


        RecyclerView recyclerView =view.findViewById(R.id.recycler_view);
        textEmpty=view.findViewById(R.id.text_empty);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new SavedAdapter(savedList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
        loadSavedData();

        SharedPreferences sharedPref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if(sharedPref.getBoolean("firsttime", true)){
            warning(editor);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(getActivity());
        FlurryAgent.logEvent("SavedActivity");
    }


    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(getActivity());
    }

    //tells user saved list/notes are only saved on the app.
    private void warning(final SharedPreferences.Editor editor){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setMessage("Saved List And Notes Are Saved On Device. They Will Be Deleted If You Delete The App");
        mBuilder.setCancelable(false);

        mBuilder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editor.putBoolean("firsttime", false);
                        editor.apply();

                        dialog.dismiss();

                    }
                });

        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    private void loadSavedData() {

        String name;
        String image;

        //open internal file
        try {
            //get hashmap as object
            FileInputStream inputStream = getActivity().openFileInput("saved");
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            Map hashSaved = (LinkedHashMap) objectInputStream.readObject();
            objectInputStream.close();

            if(!hashSaved.isEmpty()){
                textEmpty.setVisibility(View.GONE);
            }

            for (Object typeKey: hashSaved.keySet()) {
                //key: rest name
                //value: rest image
                name = typeKey.toString();
                image = hashSaved.get(name).toString();

                //sends data to adapter
                SavedGetter savedGetter = new SavedGetter(name, image);
                savedList.add(savedGetter);
            }

            }catch (ClassNotFoundException | IOException e){
            e.printStackTrace();
        }

        mAdapter.notifyDataSetChanged();
    }



}
