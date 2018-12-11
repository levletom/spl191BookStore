package bgu.spl.mics.application.messages.Events;

import bgu.spl.mics.Event;

public class TakeBookEvent implements Event {
    private String bookName;

    public TakeBookEvent(String bookName) {
        this.bookName = bookName;
    }

    public String getBookName() {
        return bookName;
    }
}
