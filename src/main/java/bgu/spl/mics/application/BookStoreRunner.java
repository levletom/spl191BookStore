package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {

    private static InputJsonReciver g;
    private static Queue<Thread> allThreadsExeptTimer;
    private static Thread timerThread;
    private static HashMap<Integer,Customer> customerHashMap;
    private static String customerHashMapFile;
    private static String booksHashMapFile;
    private static String listOfOrderReceiptsFile;
    private static String moneyRegisterFile;
    public static void main(String[] args) {
        customerHashMap = new HashMap<>();
        allThreadsExeptTimer = new ConcurrentLinkedQueue<>();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = args[0];
        customerHashMapFile= args[1];
        booksHashMapFile = args[2];
        listOfOrderReceiptsFile = args[3];
        moneyRegisterFile = args[4];

        try(Reader reader = new InputStreamReader(new FileInputStream(json))){
             g = gson.fromJson(reader,InputJsonReciver.class);

        }  catch (IOException e) {
            e.printStackTrace();
            System.out.println("InputFile not found or not valid");
            return;
        }

        loadResourcesAndInventory();
        createServicesThreads();
        createAPIThreads();
        checkThreadsAreReady();
        createTimerThread();
        joinAll();
        System.out.println("doneJoin");
       // printAlObjects();


        int numOfTest = Integer.parseInt(args[0].replace(new File(args[0]).getParent(), "").replace("/", "").replace(".json", ""));
        String dir = new File(args[1]).getParent() + "/" + numOfTest + " - ";
        Customer[] customers1 = customerHashMap.values().toArray(new Customer[0]);
        Arrays.sort(customers1, Comparator.comparing(Customer::getName));
        String str_custs = customers2string(customers1);
        str_custs = str_custs.replaceAll(", ", "\n---------------------------\n").replace("[", "").replace("]", "");
        Print(str_custs, dir + "Customers");

        String str_books = Arrays.toString(Inventory.getInstance().getBooksInventory().values().toArray());
        str_books = str_books.replaceAll(", ", "\n---------------------------\n").replace("[", "").replace("]", "");
        Print(str_books, dir + "Books");

        List<OrderReceipt> receipts_lst = MoneyRegister.getInstance().getOrderReceipts();
        receipts_lst.sort(Comparator.comparing(OrderReceipt::getOrderId));
        receipts_lst.sort(Comparator.comparing(OrderReceipt::getOrderTick));
        OrderReceipt[] receipts = receipts_lst.toArray(new OrderReceipt[0]);
        String str_receipts = receipts2string(receipts);
        str_receipts = str_receipts.replaceAll(", ", "\n---------------------------\n").replace("[", "").replace("]", "");
        Print(str_receipts, dir + "Receipts");

        Print(MoneyRegister.getInstance().getTotalEarnings() + "", dir + "Total");

        System.out.println("done");

    }

    private static void checkThreadsAreReady() {
        Queue<Thread> temp = new ConcurrentLinkedQueue<>();
        while(!allThreadsExeptTimer.isEmpty()){
            Thread t = allThreadsExeptTimer.poll();
            if(t.getState()== Thread.State.WAITING)
                temp.add(t);
            else
                allThreadsExeptTimer.offer(t);
        }
        allThreadsExeptTimer = temp;
    }

    private static void printAlObjects() {
        FileOutputStream fCustOut = null;
        try {
            fCustOut = new FileOutputStream(customerHashMapFile);
            ObjectOutputStream oos = new ObjectOutputStream(fCustOut);
            oos.writeObject(customerHashMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Inventory.getInstance().printInventoryToFile(booksHashMapFile);
        MoneyRegister.getInstance().printOrderReceipts(listOfOrderReceiptsFile);
        FileOutputStream fMoneyRegOut = null;
        try {
            fMoneyRegOut = new FileOutputStream(moneyRegisterFile);
            ObjectOutputStream oos = new ObjectOutputStream(fMoneyRegOut);
            oos.writeObject(MoneyRegister.getInstance());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void joinAll() {
        try {
            timerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Thread t: allThreadsExeptTimer){
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadResourcesAndInventory() {
        Inventory.getInstance().load(g.getInitialInventory());
        ResourcesHolder.getInstance().load(g.getInitialResources()[0].getVehicles());
    }

    private static void createTimerThread() {

        timerThread = new Thread(new TimeService(g.getServices().getTime().getSpeed(),g.getServices().getTime().getDuration()));
        timerThread.start();
    }

    private static void createAPIThreads() {
        InputJsonReciver.JCustomer[] customers = g.getServices().getCustomers();
        for(int i=0; i<customers.length;i++){
            Customer customer =new Customer(
                    customers[i].getName(),
                    customers[i].getId(),
                    customers[i].getAddress(),
                    customers[i].getDistance(),
                    customers[i].getCreditCard().getNumber(),
                    new AtomicInteger(customers[i].getCreditCard().getAmount()));
            Thread t = new Thread(new APIService(customer,customers[i].getOrderSchedule()));
            customerHashMap.put(customer.getId(),customer);
            t.start();
            allThreadsExeptTimer.add(t);
        }
    }

    private static void createServicesThreads() {
        for(int i=0; i<g.getServices().getSelling();i++){
            Thread t = new Thread(new SellingService(i));
            t.start();
            allThreadsExeptTimer.add(t);
        }
        for(int i=0; i<g.getServices().getInventoryService();i++){
            Thread t = new Thread(new InventoryService(i));
            t.start();
            allThreadsExeptTimer.add(t);
        }
        for(int i=0; i<g.getServices().getLogistics();i++){
            Thread t = new Thread(new LogisticsService(""+i));
            t.start();
            allThreadsExeptTimer.add(t);
        }
        for(int i=0; i<g.getServices().getResourcesService();i++){
            Thread t = new Thread(new ResourceService(""+i));
            t.start();
            allThreadsExeptTimer.add(t);
        }
    }


    public static String customers2string(Customer[] customers) {
        String str = "";
        for (Customer customer : customers)
            str += customer2string(customer) + "\n---------------------------\n";
        return str;
    }

    public static String customer2string(Customer customer) {
        String str = "id    : " + customer.getId() + "\n";
        str += "name  : " + customer.getName() + "\n";
        str += "addr  : " + customer.getAddress() + "\n";
        str += "dist  : " + customer.getDistance() + "\n";
        str += "card  : " + customer.getCreditNumber() + "\n";
        str += "money : " + customer.getAvailableCreditAmount();
        return str;
    }

    public static String books2string(BookInventoryInfo[] books) {
        String str = "";
        for (BookInventoryInfo book : books)
            str += book2string(book) + "\n---------------------------\n";
        return str;
    }

    public static String book2string(BookInventoryInfo book) {
        String str = "";
        str += "title  : " + book.getBookTitle() + "\n";
        str += "amount : " + book.getAmountInInventory() + "\n";
        str += "price  : " + book.getPrice();
        return str;
    }


    public static String receipts2string(OrderReceipt[] receipts) {
        String str = "";
        for (OrderReceipt receipt : receipts)
            str += receipt2string(receipt) + "\n---------------------------\n";
        return str;
    }
    public static String receipt2string(OrderReceipt receipt) {
        String str = "";
        str += "customer   : " + receipt.getCustomerId() + "\n";
        str += "order tick : " + receipt.getOrderTick() + "\n";
        str += "id         : " + receipt.getOrderId() + "\n";
        str += "price      : " + receipt.getPrice() + "\n";
        str += "seller     : " + receipt.getSeller();
        return str;
    }

    public static void Print(String str, String filename) {
        try {
            try (PrintStream out = new PrintStream(new FileOutputStream(filename))) {
                out.print(str);
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e.getClass().getSimpleName());
        }
    }

}

