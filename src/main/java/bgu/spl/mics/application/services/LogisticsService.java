package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.Broadcasts.TickBroadcast;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
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
		System.out.println("LogisticService " + getName() + " started");
		subscribeBroadcast(TickBroadcast.class, tickBroadcast ->{
			this.lastTick = tickBroadcast.getTick();
			if(tickBroadcast.isFinalTick()){
				finishiOperations();
			}
		});
		
	}

	/**
	 * operations to be done upon final tick
	 */
	private void finishiOperations() {
	}

}
