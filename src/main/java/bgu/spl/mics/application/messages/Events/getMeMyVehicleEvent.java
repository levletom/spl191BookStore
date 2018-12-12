package bgu.spl.mics.application.messages.Events;

import bgu.spl.mics.Event;

public class getMeMyVehicleEvent implements Event {
    private String bookName;
    private String address;
    private int distance;

    public getMeMyVehicleEvent(String bookName, String address, int distance) {
        this.bookName = bookName;
        this.address = address;
        this.distance = distance;
    }

    public String getBookName() {
        return bookName;
    }

    public String getAddress() {
        return address;
    }

    public int getDistance() {
        return distance;
    }
}
