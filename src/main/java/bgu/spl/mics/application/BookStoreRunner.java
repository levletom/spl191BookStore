package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Vector;
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
        moneyRegisterFile = args[4];
        listOfOrderReceiptsFile = args[3];

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
        printAlObjects();
    }



    /**
     * checks all initiated thread are in waiting state - meaning they finished registering and are now waithing for messages.
     */
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

    /**
     * prints all seralized objects
     */
    private static void printAlObjects() {
        FileOutputStream fCustOut = null;
        try {
            fCustOut = new FileOutputStream(Paths.get(customerHashMapFile).toAbsolutePath().toString());
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
            fMoneyRegOut = new FileOutputStream(Paths.get(moneyRegisterFile).toAbsolutePath().toString());
            ObjectOutputStream oos = new ObjectOutputStream(fMoneyRegOut);
            oos.writeObject(MoneyRegister.getInstance());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * makes all threads join.
     */
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



}

