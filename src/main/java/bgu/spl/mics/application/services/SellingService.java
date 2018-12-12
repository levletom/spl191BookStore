package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.Broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.Events.BookOrderEvent;
import bgu.spl.mics.application.messages.Events.CheckAvailabilityAndPriceEvent;
import bgu.spl.mics.application.messages.Events.DeliveryEvent;
import bgu.spl.mics.application.messages.Events.TakeBookEvent;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;

/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{
	private MoneyRegister moneyRegister;
	private int currentTick;

	public SellingService(int id) {
		super("SellingService_"+id);
		moneyRegister = MoneyRegister.getInstance();
		currentTick=0;
	}

	@Override
	protected void initialize() {
	subscribeBroadcast(TickBroadcast.class , tickBroadCast ->{
		currentTick = tickBroadCast.getTick();
		if(tickBroadCast.isFinalTick())
			finishOperation();
			}
	);
    subscribeEvent(BookOrderEvent.class,bookOrderEvent -> {
    	int processTick = currentTick;
		Future<Integer> availableBookPrice = sendEvent(new CheckAvailabilityAndPriceEvent(bookOrderEvent.getBookName()));
		if(availableBookPrice!=null){
			Integer bookPrice = availableBookPrice.get();
			if(bookPrice!=-1){
				synchronized (bookOrderEvent.getCustomer()){
					if(bookOrderEvent.getCustomer().getAvailableCreditAmount() >= bookPrice) {
						Future<Boolean> beenTaken = sendEvent(new TakeBookEvent(bookOrderEvent.getBookName()));
						if(beenTaken!=null&&beenTaken.get()) {
							OrderReceipt receipt = new OrderReceipt(0,
									bookOrderEvent.getCustomer().getName(),
									bookOrderEvent.getCustomer().getId(),
									bookOrderEvent.getBookName(),
									bookPrice,
									currentTick,
									processTick,
									bookOrderEvent.getOrderTick());
							moneyRegister.file(receipt);
							moneyRegister.chargeCreditCard(bookOrderEvent.getCustomer(), bookPrice);
							sendEvent(new DeliveryEvent(bookOrderEvent.getBookName(), bookOrderEvent.getCustomer().getAddress(), bookOrderEvent.getCustomer().getDistance()));
							complete(bookOrderEvent,receipt);
						}
						else
							complete(bookOrderEvent,null);
					}
					else
						complete(bookOrderEvent,null);
				}
			}
			else
				complete(bookOrderEvent,null);
		}
		else
			complete(bookOrderEvent,null);
    }


	);

	}

	private void finishOperation() {
	}

}
