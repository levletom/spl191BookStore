package bgu.spl.mics.application.messages.Events;

public class TakeBookEvent {
    private String bookName;

    public TakeBookEvent(String bookName) {
        this.bookName = bookName;
    }

    public String getBookName() {
        return bookName;
    }
}
