package bgu.spl.mics.application.messages.Events;

import bgu.spl.mics.Event;

public class TakeBookEvent implements Event<Boolean> {
    private String bookName;
    private int customerOrderTick;
    private int processTick;

    public TakeBookEvent(String bookName) {
        this.bookName = bookName;
        this.customerOrderTick = customerOrderTick;
        this.processTick = processTick;
    }

    public String getBookName() {
        return bookName;
    }

    public int getCustomerOrderTick() {
        return customerOrderTick;
    }

    public int getProcessTick() {
        return processTick;
    }
}
