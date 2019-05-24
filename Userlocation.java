package com.newproject.ted.emergencyhealth;

public class Userlocation {

    private double latitude;
    private double longitude;
    private String status;

    public Userlocation(){

    }

public Userlocation(double latitude, double longitude, String status){
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;


}

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
