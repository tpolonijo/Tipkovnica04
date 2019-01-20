package com.example.toni.tipkovnica04;

public class Podatak {
    private String first;
    private String second;
    private float percentage;

    public Podatak(){

    }

    /*public Podatak(String first, String second, float percentage) {

        this.first = first;
        this.second = second;
        this.percentage = percentage;
    }*/

    //getters
    public String getFirst() { return first; }
    public String getSecond() { return second; }
    public float getPercentage() { return percentage; }

    //setters
    public void setFirst(String first) { this.first = first; }
    public void setSecond(String second) { this.second = second; }
    public void setPercentage(float percentage) { this.percentage = percentage; }

}