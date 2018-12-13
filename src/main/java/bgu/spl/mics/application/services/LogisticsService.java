package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.Broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.Events.DeliveryEvent;
import bgu.spl.mics.application.messages.Events.GetMeMyVehicleEvent;
import bgu.spl.mics.application.messages.Events.ReleaseMyVehicleEvent;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {
    private int lastTick;

    public LogisticsService(String name) {
        super("Logisitic Service" + name);
        lastTick = -1;
    }

    @Override
    protected void initialize() {
        System.out.println(getName() + " started");
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            System.out.println(getName() + " Recieved Tick: " + tickBroadcast.getTick());
            this.lastTick = tickBroadcast.getTick();
            if (tickBroadcast.isFinalTick()) {
                finishOperations();
            }
        });
        subscribeEvent(DeliveryEvent.class, deliveryEvent -> {
            System.out.println(getName() + " Recieved DeliveryEvent adress: " + deliveryEvent.getAddress() + " distance" + deliveryEvent.getDistance() + " on tick " + lastTick);
            Future<Future<DeliveryVehicle>> fut = sendEvent(new GetMeMyVehicleEvent());
            if (fut != null) {
                Future<DeliveryVehicle> vehicleFut = fut.get();
                if (vehicleFut != null) {
                    DeliveryVehicle vehicle = vehicleFut.get();
                    if (vehicle != null) {
                        System.out.println(getName() + " Recieved vehicle: " + vehicle.getLicense() + " on tick " + lastTick);
                        vehicle.deliver(deliveryEvent.getAddress(), deliveryEvent.getDistance());
                        System.out.println(getName() + " vehicle: " + vehicle.getLicense() + " finished deliver" + " on tick " + lastTick);
                        sendEvent(new ReleaseMyVehicleEvent(vehicle));
                    }
                }
            }
            // no resource service OR done.
            complete(deliveryEvent, null);
        });
    }

    /**
     * operations to be done upon final tick
     */
    private void finishOperations() {
        System.out.println(getName() + "GraceFully Called Terminate");
        terminate();
    }

}
