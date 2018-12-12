package bgu.spl.mics.application.messages.Events;

import bgu.spl.mics.Event;

public class DeliveryEvent implements Event {

    private String address;
    private int distance;

    public DeliveryEvent(String bookName, String address, int distance) {

        this.address = address;
        this.distance = distance;
    }

   

    public String getAddress() {
        return address;
    }

    public int getDistance() {
        return distance;
    }
}
