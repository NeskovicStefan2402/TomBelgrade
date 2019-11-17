package com.example.tombelgradeapp;

public class Uredjaj {
    Long id;
    String bleName;
    String objectName;
    int connected;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public int getConnected() {
        return connected;
    }

    public void setConnected(int connected) {
        this.connected = connected;
    }
}
