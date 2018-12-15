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
		System.out.println( getName() + " started");
		subscribeBroadcast(TickBroadcast.class , tickBroadCast ->{
			System.out.println( getName() + " Recieved Tick: "+tickBroadCast.getTick());
			currentTick = tickBroadCast.getTick();
			if(tickBroadCast.isFinalTick())
				finishOperation();
			}
		);
    subscribeEvent(BookOrderEvent.class,bookOrderEvent -> {
    	int processTick = currentTick; System.out.println(this.getName() + "Recieved BookOrderEvent And Current tick is:" + currentTick);
		Future<Integer> availableBookPrice = sendEvent(new CheckAvailabilityAndPriceEvent(bookOrderEvent.getBookName()));System.out.println(this.getName() + "sent CheckAvilabilityAndPriceEvent And Current tick is:" + currentTick);
		if(availableBookPrice!=null){ System.out.println(this.getName() + "Recieved Future for CheckAvilabilityAndPriceEvent And its not null  And as well  Current tick is:" + currentTick);
			Integer bookPrice = availableBookPrice.get();

			if(bookPrice!=null&&bookPrice!=-1){
				System.out.println(this.getName() + "there is an avilable book at the moment Current tick is:" + currentTick);
				synchronized (bookOrderEvent.getCustomer()){
					if(bookOrderEvent.getCustomer().getAvailableCreditAmount() >= bookPrice) {System.out.println(this.getName() + "Have enough Money to Buy A book And Current tick is:" + currentTick);
						Future<Boolean> beenTaken = sendEvent(new TakeBookEvent(bookOrderEvent.getBookName()));
						if(beenTaken!=null && beenTaken.get()!=null && beenTaken.get()) {
							System.out.println(this.getName() + "Has managed to take a book And Current tick is:" + currentTick);
							OrderReceipt reciept = new OrderReceipt(0,
									getName(),
									bookOrderEvent.getCustomer().getId(),
									bookOrderEvent.getBookName(),
									bookPrice,
									currentTick,
									bookOrderEvent.getOrderTick(),
									processTick);

							moneyRegister.file(reciept);
							moneyRegister.chargeCreditCard(bookOrderEvent.getCustomer(), bookPrice);
							System.out.println(this.getName() + "Has issued a reciept and charged customer And Current tick is:" + currentTick);
							//bookOrderEvent.getCustomer().addReceipt(reciept);
							sendEvent(new DeliveryEvent(bookOrderEvent.getBookName(), bookOrderEvent.getCustomer().getAddress(), bookOrderEvent.getCustomer().getDistance()));System.out.println(this.getName() + "sent a DeliveryEvent And Current tick is:" + currentTick);
							complete(bookOrderEvent,reciept);System.out.println(this.getName() + "completed BookOrderEvent And Current tick is:" + currentTick);
						}
						else{
							System.out.println(this.getName() + "book has been taken before we managed And Current tick is:" + currentTick);
							complete(bookOrderEvent,null);
						}

					}
					else
					{
						System.out.println(this.getName() + "we DONT Have enough Money to Buy A book And Current tick is:" + currentTick);
						complete(bookOrderEvent,null);
					}

				}
			}
			else{
				System.out.println(this.getName() + "there is NO avilable book at the moment Current tick is:" + currentTick);
				complete(bookOrderEvent,null);
			}

		}
		else{
			System.out.println(this.getName() + "Recieved null instead of Future for CheckAvilabilityAndPriceEvent  Current tick is:" + currentTick);
			complete(bookOrderEvent,null);
        }
    }



	);

	}

	private void finishOperation() {
		System.out.println(this.getName()+"terminated At " + this.currentTick);
		terminate();
	}

}
