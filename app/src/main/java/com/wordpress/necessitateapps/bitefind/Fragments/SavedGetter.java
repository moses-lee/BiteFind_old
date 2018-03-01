package com.wordpress.necessitateapps.bitefind.Fragments;

public class SavedGetter {
    public SavedGetter(){}

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getResImage() {
        return resImage;
    }

    public void setResImage(String resImage) {
        this.resImage = resImage;
    }

    private String resName,resImage;

    public SavedGetter(String resName, String resImage) {
        this.resName = resName;
        this.resImage = resImage;
    }


}
