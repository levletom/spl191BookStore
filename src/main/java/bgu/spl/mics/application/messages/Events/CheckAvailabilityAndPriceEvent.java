package bgu.spl.mics.application.messages.Events;



public class CheckAvailabilityAndPriceEvent {
    private String bookName;

    public CheckAvailabilityAndPriceEvent(String bookName) {
        this.bookName = bookName;
    }

    public String getBookName() {
        return bookName;
    }
}
