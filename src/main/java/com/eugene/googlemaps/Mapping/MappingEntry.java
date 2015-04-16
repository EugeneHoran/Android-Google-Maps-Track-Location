package com.eugene.googlemaps.Mapping;

public class MappingEntry {
    private Long id;
    private int inputType;
    private int activityType;
    private double distance;

    public MappingEntry() {
        this.inputType = -1;
        this.activityType = -1;
        this.distance = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }


    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
