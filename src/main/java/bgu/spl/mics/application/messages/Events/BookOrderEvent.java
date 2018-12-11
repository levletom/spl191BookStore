package bgu.spl.mics.application.messages.Events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;

public class BookOrderEvent implements Event {
    private Customer customer;
    private String bookName;
    private int orderTick;

    public BookOrderEvent(Customer customer, String bookName, int orderTick) {
        this.customer = customer;
        this.bookName = bookName;
        this.orderTick = orderTick;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getBookName() {
        return bookName;
    }

    public int getOrderTick() {
        return orderTick;
    }
}
