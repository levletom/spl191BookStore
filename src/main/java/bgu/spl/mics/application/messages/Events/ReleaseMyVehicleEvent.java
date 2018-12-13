package bgu.spl.mics.application.messages.Events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class ReleaseMyVehicleEvent implements Event {
    private DeliveryVehicle vehicle;


    public ReleaseMyVehicleEvent(DeliveryVehicle vehicle) {
        this.vehicle = vehicle;
    }

    public DeliveryVehicle getVehicle() {
        return vehicle;
    }
}
