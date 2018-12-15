package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.After;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class BookStoreRunnerTest {

    private static HashMap<Integer, Customer> customers;
    private static HashMap<String, Integer> inventory;
    private static List<OrderReceipt> receipts;
    private static MoneyRegister moneyRegister;
    private static String[] args;
    private static InputJsonReciver g;
    private static String testResults;


    public void tearDown() {
        customers = null;
        inventory = null;
        receipts = null;
        moneyRegister = null;
        args = null;
        g = null;
        testResults = null;

    }


    @Test
    public void main() throws IOException {
        int testNum = 6;


        String[] args = {"" + testNum + ".json", "customer" + testNum, "book" + testNum, "receipt" + testNum, "moneyReg" + testNum, "" + testNum};
        this.args = args;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = args[0];
        try (Reader reader = new InputStreamReader(new FileInputStream(json))) {
            g = gson.fromJson(reader, InputJsonReciver.class);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("InputFile not found or not valid");
            return;
        }
        BookStoreRunner.main(args);
        ExtractProgramOutput(args);
        BasicTests basicTests = new BasicTests(customers, inventory, receipts, moneyRegister, g);
        testResults = basicTests.runTests().toString();
        outputTestResults(testResults);
        tearDown();


    }

    private static void outputTestResults(String testResults) throws IOException {
        Files.write(Paths.get("test_result" + args[5] + ".txt"), testResults.getBytes());
    }

    private void checkConsitency() {

    }

    private static void ExtractProgramOutput(String[] srializedObjects) {

        try {
            getCustomers(srializedObjects[1]);
            getInventory(srializedObjects[2]);
            getReceipts(srializedObjects[3]);
            getMoneyRegister(srializedObjects[4]);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void getMoneyRegister(String moneyRegisterObj)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(moneyRegisterObj);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        moneyRegister = (MoneyRegister) in.readObject();
        in.close();
        fileIn.close();
    }

    private static void getReceipts(String receiptsObj)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(receiptsObj);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        receipts = (List<OrderReceipt>) in.readObject();
        in.close();
        fileIn.close();
    }

    private static void getInventory(String inventoryObj)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(inventoryObj);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        inventory = (HashMap<String, Integer>) in.readObject();
        in.close();
        fileIn.close();
    }

    private static void getCustomers(String customersObj)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(customersObj);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        customers = (HashMap<Integer, Customer>) in.readObject();
        in.close();
        fileIn.close();
    }
}