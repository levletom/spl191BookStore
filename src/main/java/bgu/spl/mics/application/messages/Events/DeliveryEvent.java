package bgu.spl.mics.application.messages.Events;

public class DeliveryEvent {
    private String bookName;
    private String address;
    private int distance;

    public DeliveryEvent(String bookName, String address, int distance) {
        this.bookName = bookName;
        this.address = address;
        this.distance = distance;
    }

    public String getBookName() {
        return bookName;
    }

    public String getAddress() {
        return address;
    }

    public int getDistance() {
        return distance;
    }
}
