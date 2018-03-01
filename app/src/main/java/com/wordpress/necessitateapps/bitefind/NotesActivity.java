package com.wordpress.necessitateapps.bitefind;

import android.content.Context;

import android.graphics.Typeface;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class NotesActivity extends AppCompatActivity {

    private ImageView imageNotes;
    private EditText editContent;
    private CollapsingToolbarLayout toolbar;
    private String title=null, image=null;
    private final static String FILENAME = "saved_notes";
    private HashMap<String, String> hashNotes=null;
    private String tempContent=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        FloatingActionButton imageBack = findViewById(R.id.back_btn);
        toolbar=findViewById(R.id.collapseToolbar);
        editContent=findViewById(R.id.edit_content);
        imageNotes=findViewById(R.id.image_notes);

        //get title and image from extra
        title=getIntent().getExtras().getString("title");
        image=getIntent().getExtras().getString("image");

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        openNotes();
    }


    private void openNotes(){

        //read from internal file
        try {
            //get hashmap as object
            FileInputStream inputStream = openFileInput(FILENAME);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            hashNotes = (HashMap) objectInputStream.readObject();
            objectInputStream.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        toolbar.setTitle(title);
        toolbar.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD);
        Picasso.with(this).load(image).fit().centerCrop().into(imageNotes);

        //open notes only if file and note exists
        if (hashNotes != null&&hashNotes.get(title) != null&&!hashNotes.get(title).isEmpty()) {
            tempContent=hashNotes.get(title);

            if(tempContent!=null&&!tempContent.isEmpty()){
                editContent.setText(tempContent, TextView.BufferType.EDITABLE);
            }
        }

        if(tempContent==null||tempContent.isEmpty())
            tempContent="";

    }


    @Override
    protected void onPause() {
        super.onPause();

        saveAction();
    }

    @Override
    protected void onStop() {
        super.onStop();

        saveAction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        saveAction();
    }

    private void saveAction(){
        //checks if user has made any changes to content
        String content;
        content=editContent.getText().toString().trim();
        if (!tempContent.equals(content)) {
            tempContent=content;
            //if hashmap does not exist(user's first save), it creates a new
            if (hashNotes == null) {
                hashNotes = new HashMap<>();
                hashNotes.put(title, content);
            } else {
                hashNotes.put(title, content);
            }

            //write to internal file, save hashmap as object
            try {
                FileOutputStream outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(hashNotes);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}