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
		Inventory.getInstance();
	}

	@Override
	protected void initialize() {
		System.out.println( getName() + " started");
		subscribeBroadcast(TickBroadcast.class , tickBroadCast ->{
			currentTick = tickBroadCast.getTick();
			if(tickBroadCast.isFinalTick())
				finishOperation();
		});
		subscribeEvent(CheckAvailabilityAndPriceEvent.class , checkAvailabilityAndPrice->{ System.out.println(this.getName() + " Recieved CheckAvilabiltyAndPriceEvent and currentTick is: " + currentTick);
			String bookNameToCheck = checkAvailabilityAndPrice.getBookName(); System.out.println(this.getName() + " bookNameToWorkWith is " + bookNameToCheck + " and currentTick is: " + currentTick);
			Integer price = inventory.checkAvailabiltyAndGetPrice(bookNameToCheck); System.out.println(this.getName() + " Book price to Work With" + price + " and currentTick is: " + currentTick);
			complete(checkAvailabilityAndPrice,price);
		});
		subscribeEvent(TakeBookEvent.class,takeBookEvent->{ System.out.println(this.getName() + " Recieved TakeBookEvent with  " + takeBookEvent.getBookName() + " and currentTick is: " + currentTick);
			String bookNameToTake = takeBookEvent.getBookName();
			if(inventory.take(bookNameToTake) == OrderResult.SUCCESSFULLY_TAKEN) {
				System.out.println(this.getName() + " SUCCESSFULLY TAKEN and currentTick is: " + currentTick);
				complete(takeBookEvent, true);
			}
			else {
				System.out.println(this.getName() + " NOT TAKEN and currentTick is: " + currentTick);
				complete(takeBookEvent, false);
			}
		});

	}

	private void finishOperation(){
		System.out.println(this.getName() + " is terminating and currentTick is: " + currentTick);
		terminate();
	}

}
