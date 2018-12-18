package bgu.spl.mics.application.passiveObjects;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.*;

import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;

public class BasicTests {

    private HashMap<Integer,Customer> customers;
    private HashMap<String,Integer> inventory;
    private List<OrderReceipt> receipts;
    private MoneyRegister moneyRegister;
    private InputJsonReciver input;

    private StringBuilder testResults;

    
	public BasicTests(HashMap<Integer, Customer> customers, HashMap<String, Integer> inventory, List<OrderReceipt> receipts, MoneyRegister moneyRegister,InputJsonReciver input) {
		this.customers = customers;
		this.inventory = inventory;
		this.receipts = receipts;
		this.moneyRegister = moneyRegister;
		this.input = input;

		this.testResults = new StringBuilder("");
	}

	public StringBuilder runTests() {
		runBasicTests();
		finishUp();
		return testResults;
	}


	private void runBasicTests() {
		//checkInventory();
		checkInventoryConsistentWithReceipts();
		checkMoneyRegTotalConsitentWithInventory();
		checkAmountChargedFromCustomerConsistentWithReceiptsAndLessThanTotalAmount();

		checkReceipts();

	}

	private void checkAmountChargedFromCustomerConsistentWithReceiptsAndLessThanTotalAmount() {

		for (Customer c :
				customers.values()) {
			int amountChragedByReceipt =0 ;
			for (OrderReceipt o :
					c.getCustomerReceiptList()) {
				amountChragedByReceipt+=o.getPrice();
			}
			int amountChargedByStartMinusEnd = 0;
			int initailAmount =0;
			for (int i=0;i<input.getServices().getCustomers().length;i++){
				if(c.getId()==input.getServices().getCustomers()[i].getId()) {
					initailAmount = input.getServices().getCustomers()[i].getCreditCard().getAmount();
					break;
				}
			}
			amountChargedByStartMinusEnd = initailAmount-c.getAvailableCreditAmount();
			if(amountChargedByStartMinusEnd!=amountChragedByReceipt){
				testResults.append("####BASIC-TESTS-ERROR#####\nAmount charged to customer: ");
				testResults.append(c.getId()+" is: "+amountChargedByStartMinusEnd);
				testResults.append("\n But the amount charged acording to his receipts is: " +amountChragedByReceipt);

			}
			if(c.getAvailableCreditAmount()<0){
				testResults.append("####BASIC-TESTS-ERROR#####\nAmount charged to customer: "+c.getId()+" is more than he had he was left with: ");
				testResults.append(c.getAvailableCreditAmount());
			}
		}
	}

	private void checkMoneyRegTotalConsitentWithInventory() {
		int totalEaringsByInventory=0;
		for(BookInventoryInfo b: input.getInitialInventory()){
			int amountOfBooksremovedFromInventory = b.getAmountInInventory()-inventory.get(b.getBookTitle());
			totalEaringsByInventory+= amountOfBooksremovedFromInventory*b.getPrice();
		}
		if(totalEaringsByInventory!=moneyRegister.getTotalEarnings()){
			testResults.append("####BASIC-TESTS-ERROR#####\nTotal Earnings by moneyreg is: ");
			testResults.append(moneyRegister.getTotalEarnings());
			testResults.append("\n But the amount bought by inventory is: " +totalEaringsByInventory);
		}
	}

	private void checkInventoryConsistentWithReceipts() {
		HashMap<String,Integer> booksOrderedByReceipts = new HashMap<>();
		for (OrderReceipt receipt : receipts) {
			if(booksOrderedByReceipts.get(receipt.getBookTitle())==null)
				booksOrderedByReceipts.put(receipt.getBookTitle(),1);
			else {
				booksOrderedByReceipts.put(receipt.getBookTitle(),booksOrderedByReceipts.get(receipt.getBookTitle())+1);
			}
		}
		for (BookInventoryInfo b :
				input.getInitialInventory()) {
			Integer amountByRec = booksOrderedByReceipts.get(b.getBookTitle());
			if(amountByRec==null)
				amountByRec = 0;
			if(amountByRec!=null){
				if(b.getAmountInInventory()-amountByRec!=inventory.get(b.getBookTitle())){
					testResults.append("####BASIC-TESTS-ERROR#####\nAmount in inventory of book ");
					testResults.append(b.getBookTitle());
					testResults.append(" is " +inventory.get(b.getBookTitle()));
					testResults.append("\n But the amount bought by receipts is " +amountByRec);
					testResults.append("\nand initial amount is " +b.getAmountInInventory());

				}
			}
		}
	}


	private void checkReceipts() {

		for (OrderReceipt receipt : receipts) {
			int customer = receipt.getCustomerId();
			checkReceiptSentToCustomer(receipt, customer);
		}
	}



	private void checkReceiptSentToCustomer(OrderReceipt receipt, int customer) {
		if (!findReceiptInCustomer(receipt, customer)) {
			testResults.append("####BASIC-TESTS-ERROR#####\nYour money register has a receipt for customer ");
			testResults.append(String.valueOf(customer));
			testResults.append(" for his purchase of ");
			testResults.append(receipt.getBookTitle());
			testResults.append(", but the customer himself is missing a receipt with the same information!\n");
		}
	}

	private boolean findReceiptInCustomer(OrderReceipt receipt, int customer) {
		List<OrderReceipt> customerReceipts = customers.get(customer).getCustomerReceiptList();
		for (OrderReceipt customerReceipt : customerReceipts) { 
			if (foundMatchingReceipts(receipt, customerReceipt)) {
				return true;
			}
		}
		return false;
	}
	
	
	private boolean foundMatchingReceipts(OrderReceipt receipt, OrderReceipt customerReceipt) {
		return  customerReceipt.getBookTitle().equals(receipt.getBookTitle()) &&
				customerReceipt.getCustomerId() == receipt.getCustomerId() &&
				customerReceipt.getOrderTick() == receipt.getOrderTick() &&
				customerReceipt.getPrice() == receipt.getPrice();
	}
/*
	private void checkAmountInCreditCard(TesterCustomer expectedCustomer, Customer studentsCustomer) {
		int expectedAmount = expectedCustomer.getAvailableAmountInCreditCard();
		int actualAmount = studentsCustomer.getAvailableCreditAmount();
		if (expectedAmount != actualAmount) {
			testResults.append("####BASIC-TESTS-ERROR#####\n wrong amount of credit card: Customer ");
			testResults.append(String.valueOf(studentsCustomer.getId()));
			testResults.append(" should have: ");
			testResults.append(String.valueOf(expectedAmount));
			testResults.append("$ in his credit card, but has: ");
			testResults.append(String.valueOf(actualAmount));
			testResults.append("$.\n");
		}
		
	}
	*/
/*
	private void checkInventory() {
		for (Entry<String, Integer> inventoryEntry: testerInventory.entrySet()) {
			String name = inventoryEntry.getKey();
			int expectedAmount = inventoryEntry.getValue();
			if (inventory.get(name) == null) {
				testResults.append("####BASIC-TESTS-ERROR#####\n missing books in inventory: The book ");
				testResults.append(name);
				testResults.append(" is missing!\n");
			}else {
				checkBookAmount(name, expectedAmount);
			}
		}
	}
	*/

	private void checkBookAmount(String name, int expectedAmount) {
		int studentsAmount = inventory.get(name);
		if (expectedAmount != studentsAmount) {
			testResults.append("####BASIC-TESTS-ERROR#####\n wrong amount of books in inventory: The book");
			testResults.append(name);
			testResults.append(" should have: ");
			testResults.append(String.valueOf(expectedAmount));
			testResults.append(" copies. But has: ");
			testResults.append(String.valueOf(studentsAmount));
			testResults.append(" copies!\n");
		}
	}
	

	private void finishUp() { 
		if (testResults.toString().isEmpty())
			testResults.append("Nice!! All tests passed! \n");
	}

}
