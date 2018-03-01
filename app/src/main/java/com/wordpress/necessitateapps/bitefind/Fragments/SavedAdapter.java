package com.wordpress.necessitateapps.bitefind.Fragments;


import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wordpress.necessitateapps.bitefind.NotesActivity;
import com.wordpress.necessitateapps.bitefind.R;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SavedAdapter extends RecyclerView.Adapter<SavedAdapter.MyViewHolder> {

    private List<SavedGetter> savedList;


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textSaved;
        ImageView imageSaved,imageDelete,imageNotes;



        MyViewHolder(View view) {
            super(view);
            textSaved =view.findViewById(R.id.text_saved);
            imageSaved =view.findViewById(R.id.image_saved);
            imageNotes=view.findViewById(R.id.image_note);
            imageDelete =view.findViewById(R.id.image_delete);
        }
    }


    SavedAdapter(List<SavedGetter> savedList) {
        this.savedList = savedList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.saved_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final SavedGetter savedGetter = savedList.get(position);

        holder.textSaved.setText(savedGetter.getResName());
        final Context mContext=holder.imageSaved.getContext();
        Picasso.with(mContext).load(savedGetter.getResImage()).fit().centerCrop().into(holder.imageSaved);

        //testing
        holder.imageNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent notes=new Intent(mContext, NotesActivity.class);
                notes.putExtra("title",savedGetter.getResName());
                notes.putExtra("image",savedGetter.getResImage());
                mContext.startActivity(notes);
            }
        });


        //opens google maps
        holder.imageSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String redirect = "https://www.google.com/maps/search/" + savedGetter.getResName();
                Intent map_intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(redirect));
                mContext.startActivity(map_intent);
            }
        });

        //delete btn action
        holder.imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //opens snackbar for confirmation
                //no idea how the context works though
                Snackbar.make(((Activity) mContext).findViewById(R.id.saved_layout),"Delete Saved Restaurant?", Snackbar.LENGTH_LONG)
                        .setAction("Delete", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteAction(mContext, savedGetter.getResName(), holder);
                            }
                        }).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return savedList.size();
    }

    private void deleteAction(Context mContext, String name, MyViewHolder holder){
        String filename = "saved";
        String filename_notes= "saved_notes";


        LinkedHashMap hashSaved = null;
        HashMap hashNotes=null;
        //read from internal file
        try {
            //get hashmap as object
            FileInputStream inputStream = mContext.openFileInput(filename);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            hashSaved = (LinkedHashMap) objectInputStream.readObject();
            objectInputStream.close();

            //reads from notes file
            FileInputStream inputStreamNotes = mContext.openFileInput(filename_notes);
            ObjectInputStream objectInputStreamNotes = new ObjectInputStream(inputStreamNotes);
            hashNotes = (HashMap) objectInputStreamNotes.readObject();
            objectInputStreamNotes.close();

        }catch (ClassNotFoundException | IOException e){
            e.printStackTrace();
        }

        //removes value from the hashmap
        if (hashSaved != null) {
            hashSaved.remove(name);
        }
        if (hashNotes != null) {
            hashNotes.remove(name);
        }

        //write to internal file, saves hashmap as object
        try {
            FileOutputStream outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(hashSaved);
            outputStream.close();

            //writes to notes file
            FileOutputStream outputStreamNotes = mContext.openFileOutput(filename_notes, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStreamNotes= new ObjectOutputStream(outputStreamNotes);
            objectOutputStreamNotes.writeObject(hashNotes);
            objectOutputStreamNotes.close();

            //updates the recyclerview
            updateRecylerview(holder.getAdapterPosition());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateRecylerview(int position){
        //removes item from araylist
        savedList.remove(position);
        SavedAdapter mAdapter = new SavedAdapter(savedList);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, savedList.size());
        mAdapter.notifyDataSetChanged();
    }

}