package bgu.spl.mics.application.passiveObjects;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive data-object representing a customer of the store.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Customer implements Serializable {
    private String customerName;
    private int customerId;
    private String address;
    private int distance;
    private List<OrderReceipt> listOfReciepts;
    private int creditCard;
    private AtomicInteger availableAmountInCreditCard;


	public Customer(String customerName, int customerId, String address, int distance, int creditCard, AtomicInteger availableAmountInCreditCard) {
		this.customerName = customerName;
		this.customerId = customerId;
		this.address = address;
		this.distance = distance;
		this.creditCard = creditCard;
		this.availableAmountInCreditCard = availableAmountInCreditCard;
		listOfReciepts = new Vector<>();
	}


	/**
     * Retrieves the name of the customer.
     */
	public String getName() {

		return customerName;
	}

	/**
     * Retrieves the ID of the customer  . 
     */
	public int getId() {
		return customerId;
	}
	
	/**
     * Retrieves the address of the customer.  
     */
	public String getAddress() {
		return address;
	}
	
	/**
     * Retrieves the distance of the customer from the store.  
     */
	public int getDistance() {
		return distance;
	}

	
	/**
     * Retrieves a list of receipts for the purchases this customer has made.
     * <p>
     * @return A list of receipts.
     */
	public List<OrderReceipt> getCustomerReceiptList() {
		return listOfReciepts;
	}
	
	/**
     * Retrieves the amount of money left on this customers credit card.
     * <p>
     * @return Amount of money left.   
     */
	public int getAvailableCreditAmount() {
		return availableAmountInCreditCard.get();
	}
	
	/**
     * Retrieves this customers credit card serial number.    
     */
	public int getCreditNumber() {
		return creditCard;
	}

	/**
	 * Charges the customer's credit card the given amount , always succeeds.
	 * @param amount
	 */
    public void charge(int amount) {
    	int currentVal;
    	int nextVal;
    	do{
    		currentVal = availableAmountInCreditCard.get();
    		nextVal = currentVal - amount;
		}while(!availableAmountInCreditCard.compareAndSet(currentVal,nextVal));
    }

	public void addReceipt(OrderReceipt receipt) {
    	this.listOfReciepts.add(receipt);
	}
}
