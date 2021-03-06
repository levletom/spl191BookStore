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

		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {

			this.lastTick = tickBroadcast.getTick();
			if (tickBroadcast.isFinalTick()) {
				finishOperations();
			}
		});
		subscribeEvent(GetMeMyVehicleEvent.class,getMeMyVehicleEvent->{

			Future<DeliveryVehicle> vehicleFuture = resourcesHolder.acquireVehicle();
			if(vehicleFuture!=null) {

				returnedFutreVehicles.offer(vehicleFuture);
				complete(getMeMyVehicleEvent,vehicleFuture);
			}

		});
		subscribeEvent(ReleaseMyVehicleEvent.class, ReleaseMyVehicleEvent->{

			resourcesHolder.releaseVehicle(ReleaseMyVehicleEvent.getVehicle());
			complete(ReleaseMyVehicleEvent,null);
		});
	}

	private void finishOperations() {

		for (Future<DeliveryVehicle> f :
				returnedFutreVehicles) {
			if(!f.isDone())
				f.resolve(null);
		}
		terminate();
	}

}
