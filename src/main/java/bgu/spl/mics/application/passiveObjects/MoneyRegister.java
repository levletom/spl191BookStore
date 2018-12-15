package bgu.spl.mics.application.passiveObjects;


import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the store finance management. 
 * It should hold a list of receipts issued by the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class MoneyRegister implements Serializable {

  List<OrderReceipt> listOfAllOrderReceipt;
  AtomicInteger totalEarnings;
	/**
	 * constructor
	 */
	private MoneyRegister(){
		listOfAllOrderReceipt = new Vector<>();
		totalEarnings = new AtomicInteger(0);
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static MoneyRegister getInstance() {
		return SingletonHolder.instance;
	}
	
	/**
     * Saves an order receipt in the money register.
     * <p>   
     * @param r		The receipt to save in the money register.
     */
	public void file (OrderReceipt r) {
		int currentVal;
		int nextVal;
		if(r!=null){
			listOfAllOrderReceipt.add(r);
			do{
				currentVal = totalEarnings.get();
				nextVal = currentVal + r.getPrice();
			}while(!totalEarnings.compareAndSet(currentVal,nextVal));
		}

	}

	/**
     * Retrieves the current total earnings of the store.  
     */
	public int getTotalEarnings() {

		return totalEarnings.get();
	}
	
	/**
     * Charges the credit card of the customer a certain amount of money.
     * <p>
     * @param amount 	amount to charge
     */
	public void chargeCreditCard(Customer c, int amount) {
		if(c!=null){
			c.charge(amount);
		}
	}
	
	/**
     * Prints to a file named @filename a serialized object List<OrderReceipt> which holds all the order receipts 
     * currently in the MoneyRegister
     * This method is called by the main method in order to generate the output.. 
     */
	public void printOrderReceipts(String filename) {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(listOfAllOrderReceipt);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * //TODO DELETE THIS - FOR TEST ONLY
	 * @return
	 */
	public List<OrderReceipt> getOrderReceiptsForTestsONLY() {
		return listOfAllOrderReceipt;
	}

	private static class SingletonHolder {
		private static MoneyRegister instance = new MoneyRegister();
	}
}
