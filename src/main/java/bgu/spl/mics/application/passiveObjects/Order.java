package bgu.spl.mics.application.passiveObjects;

public class Order implements Comparable<Order> {
    /**
     * This class represents a desired Order
     * each class holds the desired book and a corresponding time Tick
     * on witch to make the OrderBookEvent
     * Note: this class has a natural ordering that is inconsistent with equals
     */
    private String bookTitle;
    private int tick;

    public Order(String bookName, int timeTick) {
        this.bookTitle = bookName;
        this.tick = timeTick;
    }

    public String getBookName() {
        return bookTitle;
    }

    public int getTimeTick() {
        return tick;
    }

    /**
     *
     * returns a negative integer, zero, or a positive integer as this Order Tick
     * is less than, equal to, or greater than the specified object.
     * @param o
     * @return
     */
    @Override
    public int compareTo(Order o) {
        return this.tick -o.tick;
    }
}
