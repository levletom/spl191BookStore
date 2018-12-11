package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import sun.security.provider.NativePRNG;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
     BlockingQueue<DeliveryVehicle> availableVehicles;
	private ResourcesHolder(){
		availableVehicles = new LinkedBlockingQueue<>();
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
		try{
			return ans;
		}
		finally {
			try {
				ans.resolve(availableVehicles.take());
			} catch (InterruptedException e) {

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
	}

	private static class SingletonHolder {
		private static ResourcesHolder instance = new ResourcesHolder();
	}

}
