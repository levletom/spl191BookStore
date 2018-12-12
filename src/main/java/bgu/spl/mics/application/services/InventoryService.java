package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.Broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.Events.CheckAvailabilityAndPriceEvent;
import bgu.spl.mics.application.messages.Events.TakeBookEvent;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.OrderResult;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class InventoryService extends MicroService{
   private int currentTick;
   private Inventory inventory;
	public InventoryService(int id) {
		super("InventoryService_"+id);
		inventory = Inventory.getInstance();
	}

	@Override
	protected void initialize() {
		System.out.println( getName() + " started");
		subscribeBroadcast(TickBroadcast.class , tickBroadCast ->{
			System.out.println( getName() + " Recieved Tick: "+tickBroadCast.getTick());
			currentTick = tickBroadCast.getTick();
			if(tickBroadCast.isFinalTick())
				finishOperation();
		});
		subscribeEvent(CheckAvailabilityAndPriceEvent.class , checkAvailabilityAndPrice->{
			String bookNameToCheck = checkAvailabilityAndPrice.getBookName();
			Integer price = inventory.checkAvailabiltyAndGetPrice(bookNameToCheck);
			complete(checkAvailabilityAndPrice,price);
		});
		subscribeEvent(TakeBookEvent.class,takeBookEvent->{
			Future<Boolean> toReturn = new Future<>();
			String bookNameToTake = takeBookEvent.getBookName();
			if(inventory.take(bookNameToTake) == OrderResult.SUCCESSFULLY_TAKEN)
				complete(takeBookEvent,true);
			else
				complete(takeBookEvent,false);
		});

	}

	private void finishOperation() {
	}

}
