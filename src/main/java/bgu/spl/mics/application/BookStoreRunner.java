package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.InputJsonReciver;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.*;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {

    private static InputJsonReciver g;
    private static Vector<Thread> allThreadsExeptTimer;
    private static Thread timerThread;
    public static void main(String[] args) {
        allThreadsExeptTimer = new Vector<>();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = args[0];
        File file = new File("input.json");
        try(Reader reader = new InputStreamReader(new FileInputStream(json))){
             g = gson.fromJson(reader,InputJsonReciver.class);

        }  catch (IOException e) {
            e.printStackTrace();
        }

        Inventory.getInstance().load(g.getInitialInventory());
        ResourcesHolder.getInstance().load(g.getInitialResources()[0].getVehicles());
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
        InputJsonReciver.JCustomer[] customers = g.getServices().getCustomers();
        for(int i=0; i<customers.length;i++){
            Thread t = new Thread(new APIService(new Customer(customers[i].getName(),
                    customers[i].getId(),
                    customers[i].getAddress(),
                    customers[i].getDistance(),
                    customers[i].getCreditCard().getNumber(),
                    new AtomicInteger(customers[i].getCreditCard().getAmount())),customers[i].getOrderSchedule()));
            t.start();
            allThreadsExeptTimer.add(t);
        }

        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timerThread = new Thread(new TimeService(g.getServices().getTime().getSpeed(),g.getServices().getTime().getDuration()));
        timerThread.start();

    }
}

