package java.bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.application.passiveObjects.Inventory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class InventoryTest {

    Inventory inv;
    @Before
    public void setUp() throws Exception {
        inv = Inventory.getInstance();
    }

    @After
    public void tearDown() throws Exception {

        inv.DestroyAllFieldsForTestPurposeOnly();
    }

    @Test
    public void getInstance_CHECK_RETURNED_OBJECT_INSTANCE_OF_INVENTORY(){

        assertTrue("null or not inventory returned",inv instanceof Inventory);
    }
    @Test
    public void getInstance_CHECK_2_CALLS_GET_SAME_INSTANCE() {

        Inventory inv2 = Inventory.getInstance();
        assertTrue("2 returned inventorys arnet the same instance",inv==inv2);
    }

    @Test
    public void load_Check_Empty_Array_Loads_Empty(){

        BookInventoryInfo[] bookArr = new BookInventoryInfo[0];
        inv.load(bookArr);
        assertTrue(Arrays.asList(inv.getBookInventoryForTestPurposeOnly()).isEmpty());

    }
    @Test
    public void load_Check_after_load_same_Inventory() {

        //create BookinventoryInfoInterFace
        BookInventoryInfo[]  bookArr= createBookInventoryInfoArrayOfSize(100);
        inv.load(bookArr);
        BookInventoryInfo[] retArr = inv.getBookInventoryForTestPurposeOnly();
        assertArrayEquals(bookArr,retArr);
    }

    @Test
    public void take_Not_In_Inventory() {
        BookInventoryInfo[] bookArr = createBookInventoryInfoArrayOfSize(3);
        OrderResult oR = inv.take("Not_HERE");
        assertTrue(oR==OrderResult.NOT_IN_STOCK);
    }
    @Test
    public void take_Not_In_stock_doesnt_affect_other_book_ammount_in_inventory() {
        BookInventoryInfo[] bookArr = createBookInventoryInfoArrayOfSize(10);
        inv.take("Not_Here");
        BookInventoryInfo[] afterTakeArr = inv.getBookInventoryForTestPurposeOnly();
        for (int i = 0; i < afterTakeArr.length; i++) {
            String returnedBookName = afterTakeArr[i].getBookTitle();
            int returnedBookAmmount = afterTakeArr[i].getAmountInInventory();
            int beforeTakeAmount;
            for (BookInventoryInfo b :
                    bookArr) {
                if (b.getBookTitle().equals(returnedBookName)) {
                    beforeTakeAmount = b.getAmountInInventory();
                    assertEquals(beforeTakeAmount, returnedBookAmmount);
                }

            }
        }
    }
    @Test
    public void take_In_inventory_returns_SUCCESSFULLY_TAKEN(){
        BookInventoryInfo[] bookArr = createBookInventoryInfoArrayOfSize(10);
        inv.load(bookArr);
        OrderResult oR = inv.take("Name 5");
        assertTrue(oR==OrderResult.SUCCESSFULLY_TAKEN);
    }
    @Test
    public  void take_In_inventory_removes_1_from_inventory(){
        BookInventoryInfo[] bookArr = createBookInventoryInfoArrayOfSize(10);
        inv.load(bookArr);
        inv.take("Name 5");
        BookInventoryInfo[] retArr = inv.getBookInventoryForTestPurposeOnly();
        for (BookInventoryInfo b :
                retArr) {
            if (b.getBookTitle().equals("Name 5")) {
             assertEquals(4,b.getAmountInInventory());
            }
        }

    }

    @Test
    public void take_In_inventory_does_not_affect_other_book_inventory(){
        BookInventoryInfo[] bookArr = createBookInventoryInfoArrayOfSize(50);
        inv.load(bookArr);
        inv.take("Name 5");
        BookInventoryInfo[] retArr = inv.getBookInventoryForTestPurposeOnly();
        for (BookInventoryInfo b :
                retArr) {
            if(!b.getBookTitle().equals("Name 5")){
                String retName = b.getBookTitle();
                int retAmount = b.getAmountInInventory();
                int initialAmount;
                for (int i = 0; i < bookArr.length; i++) {
                    if(bookArr[i].getBookTitle().equals(retName)){
                        initialAmount = bookArr[i].getAmountInInventory();
                        assertEquals(initialAmount,retAmount);
                    }
                }
            }
        }
    }

    private BookInventoryInfo[] createBookInventoryInfoArrayOfSize(int i) {
        BookInventoryInfo[] bookArr = new BookInventoryInfo[i];
        for (int k = 0; k < i; k++) {
            final int j = k;
            bookArr[k] = new BookInventoryInfo() {


                @Override
                public String getBookTitle() {
                    return "Name " + j;
                }

                @Override
                public int getAmountInInventory() {
                    return j;
                }

                @Override
                public int getPrice() {
                    return j;
                }
            };
        }
        return bookArr;
    }

    @Test
    public void checkAvailabiltyAndGetPrice_returns_minus_one_when_not_in_inventory() {
        BookInventoryInfo[] bookArr = createBookInventoryInfoArrayOfSize(10);
        inv.load(bookArr);
        int returnedVal = inv.checkAvailabiltyAndGetPrice("not Here");
        assertEquals(-1,returnedVal);

    }
    @Test
    public void checkAvailabiltyAndGetPrice_returns_minus_one_when_not_in_stock() {
        BookInventoryInfo[] bookArr = createBookInventoryInfoArrayOfSize(10);
        inv.load(bookArr);
        int returnedVal = inv.checkAvailabiltyAndGetPrice("Name 0");
        assertEquals(-1,returnedVal);
    }

    @Test
    public void  checkAvailabiltyAndGetPrice_returns_actual_price_when_in_stock(){
        BookInventoryInfo[] bookArr = createBookInventoryInfoArrayOfSize(10);
        inv.load(bookArr);
        for (int i = 1; i < 10; i++) {
            int returnedVal = inv.checkAvailabiltyAndGetPrice("Name "+i);
            assertEquals(i,returnedVal);
        }
    }




}