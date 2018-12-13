package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import sun.security.provider.NativePRNG;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {
     ConcurrentLinkedQueue<DeliveryVehicle> availableVehicles;
     ConcurrentLinkedQueue<Future<DeliveryVehicle>> futuresOfDeliveryVehicles;
     Semaphore sem;
	private ResourcesHolder(){
		availableVehicles = new ConcurrentLinkedQueue<>();
		sem = new Semaphore(0);
		futuresOfDeliveryVehicles = new ConcurrentLinkedQueue<>();
	}
	/**
     * Retrieves the single instance of this class.
     */
	public static ResourcesHolder getInstance() {
		return SingletonHolder.instance;
	}
	
	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	public Future<DeliveryVehicle> acquireVehicle() {
		Future<DeliveryVehicle> ans = new Future<>();
		try {
			futuresOfDeliveryVehicles.offer(ans);
			return ans;
		}
		finally {
			supplyVehicle();
		}
	}

	private void supplyVehicle() {
		if(sem.tryAcquire()){
			Future<DeliveryVehicle> fut = futuresOfDeliveryVehicles.poll();
			if(!fut.isDone()) {
			   fut.resolve(availableVehicles.poll());
			}
		}
	}

	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
		availableVehicles.offer(vehicle);
		supplyVehicle();
	}
	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		for(int i=0;i<vehicles.length;i++){
			availableVehicles.offer(vehicles[i]);
		}
		sem = new Semaphore(vehicles.length-1);

	}

	private static class SingletonHolder {
		private static ResourcesHolder instance = new ResourcesHolder();
	}

}
