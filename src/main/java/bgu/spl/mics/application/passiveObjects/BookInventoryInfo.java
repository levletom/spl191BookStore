package bgu.spl.mics.application.passiveObjects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive data-object representing a information about a certain book in the inventory.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class BookInventoryInfo{

	private String bookTitle;
	private AtomicInteger amount;
	private int price;

	/**
	 * copy constructor
	 * @param other
	 */
    public BookInventoryInfo(BookInventoryInfo other) {
		this.bookTitle = other.getBookTitle();
		amount = new AtomicInteger(other.getAmountInInventory());
		this.price = other.getPrice();
    }

	/**
	 * defaultConstructor
	 * used in tests as is.
	 */
	public BookInventoryInfo(){

	}

    /**
     * Retrieves the title of this book.
     * <p>
     * @return The title of this book.   
     */
	public String getBookTitle() {
		return bookTitle;
	}

	/**
     * Retrieves the amount of books of this type in the inventory.
     * <p>
     * @return amount of available books.      
     */
	public int getAmountInInventory() {
		return amount.get();
	}

	/**
     * Retrieves the price for  book.
     * <p>
     * @return the price of the book.
     */
	public int getPrice() {
		return price;
	}


	/**
	 * removes 1 from inventory
	 */
	public void removeOne() {
		int currntVal;
		int nexVal;
		do{
			currntVal = amount.get();
			nexVal = currntVal-1;
		}while(!amount.compareAndSet(currntVal,nexVal));

	}

	@Override
	public String toString() {
		String str = "";
		str += "title  : " + getBookTitle() + "\n";
		str += "amount : " + getAmountInInventory() + "\n";
		str += "price  : " + getPrice();
		return str;
	}
}
