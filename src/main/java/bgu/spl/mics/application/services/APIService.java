package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.Broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.Events.BookOrderEvent;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.Order;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;
import com.sun.org.apache.xpath.internal.operations.Or;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class APIService extends MicroService {

    ConcurrentHashMap<Integer, Queue<String>> tickToBookMap;
    Customer customer;

    public APIService(Customer c, Order[] orderSchedule) {
        super("Customer: " + c.getId() + " APIService");
        this.customer = c;
        tickToBookMap = new ConcurrentHashMap<>();
        for (int i = 0; i < orderSchedule.length; i++) {
            Queue<String> tickQueue = tickToBookMap.get(orderSchedule[i].getTimeTick());
            if (tickQueue == null) {
                LinkedBlockingQueue<String> A = new LinkedBlockingQueue<>();
                A.offer(orderSchedule[i].getBookName());
                tickToBookMap.put(orderSchedule[i].getTimeTick(), A);
            } else {
                tickQueue.offer(orderSchedule[i].getBookName());
            }
        }

    }

    @Override
    protected void initialize() {

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {

            if (tickBroadcast.isFinalTick()) {
                finishOperations();
                return;
            }
            Queue<String> ordersForTick = tickToBookMap.get(tickBroadcast.getTick());
            if (ordersForTick != null) {
                ConcurrentLinkedQueue<Future<OrderReceipt>> futures = new ConcurrentLinkedQueue<>();

                //proccess all orders for this tick
                while (!ordersForTick.isEmpty()) {
                    String bookName = ordersForTick.remove();
                    Future<OrderReceipt> fut = sendEvent(new BookOrderEvent(customer, bookName, tickBroadcast.getTick()));

                    //there is a registered SellingService
                    if (fut != null) {
                        futures.add(fut);

                    }

                }
                //get all future results.
                while (!futures.isEmpty()) {
                    Future<OrderReceipt> fut = futures.poll();
                    if (fut.isDone()) {
                        OrderReceipt receipt = fut.get();
                        //order completed
                        if (receipt != null) {
                            customer.addReceipt(receipt);
                        }
                    } else
                        futures.offer(fut);

                }
                /*for (Future<OrderReceipt> fut :
                        futures) {
                    OrderReceipt receipt = fut.get();
                    //order completed
                    if (receipt != null) {
                        customer.addReceipt(receipt);

                    }
                }
                */


            }

        });
    }

    /**
     * finil operations
     */
    private void finishOperations() {

        terminate();
    }

}
