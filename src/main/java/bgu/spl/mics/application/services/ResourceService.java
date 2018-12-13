package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.Broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.Events.GetMeMyVehicleEvent;
import bgu.spl.mics.application.messages.Events.ReleaseMyVehicleEvent;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService{
	private int lastTick;
	private ResourcesHolder resourcesHolder;
	private ConcurrentLinkedQueue<Future<DeliveryVehicle>> returnedFutreVehicles;
	public ResourceService(String name) {
		super("ResourceService " +name);
		lastTick = -1;
		resourcesHolder = ResourcesHolder.getInstance();
		returnedFutreVehicles = new ConcurrentLinkedQueue();
	}

	@Override
	protected void initialize() {
		System.out.println( getName() + " started");
		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
			System.out.println( getName() + " Recieved Tick: "+tickBroadcast.getTick());
			this.lastTick = tickBroadcast.getTick();
			if (tickBroadcast.isFinalTick()) {
				finishOperations();
			}
		});
		subscribeEvent(GetMeMyVehicleEvent.class,getMeMyVehicleEvent->{
			System.out.println( getName() + " Recieved getMeMyVehicleEvent: "+" on tick "+lastTick);
			Future<DeliveryVehicle> vehicleFuture = resourcesHolder.acquireVehicle();
			if(vehicleFuture!=null) {
				System.out.println( getName() + " Recieved Futre of vehicle "+" on tick "+lastTick);
				returnedFutreVehicles.offer(vehicleFuture);
				complete(getMeMyVehicleEvent,vehicleFuture);
			}

		});
		subscribeEvent(ReleaseMyVehicleEvent.class, ReleaseMyVehicleEvent->{
			System.out.println(getName() + " just recieved a ReleaseMyVehicleEvent and Currenttick is" + lastTick);
			resourcesHolder.releaseVehicle(ReleaseMyVehicleEvent.getVehicle());
			complete(ReleaseMyVehicleEvent,null);
		});
	}

	private void finishOperations() {
		System.out.println( getName() + "GraceFully Called Terminate");
		for (Future<DeliveryVehicle> f :
				returnedFutreVehicles) {
			f.resolve(null);
		}
		terminate();
	}

}
