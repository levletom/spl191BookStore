package bgu.spl.mics.application.messages.Events;


import bgu.spl.mics.Event;

public class CheckAvailabilityAndPriceEvent implements Event<Integer> {
    private String bookName;

    public CheckAvailabilityAndPriceEvent(String bookName) {
        this.bookName = bookName;
    }

    public String getBookName() {
        return bookName;
    }
}
