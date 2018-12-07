package bgu.spl.mics;

import bgu.spl.mics.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {

    private Future<String> testFut;

    @Before
    public void setUp() throws Exception {
        testFut = new Future<>();

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void get_CHECK_IF_WAITS_WHEN_NULL() {
        Thread t = new Thread(() -> testFut.get());
        t.start();
        //give t a chance to execute get
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {

        }
        assertEquals(Thread.State.WAITING,t.getState());

    }
    @Test
    public void get_CHECK_IF_WORKS() {
        testFut.resolve("resolved");
        assertEquals("resolved",testFut.get());
    }
    @Test
    public void get_CHECK_IF_WAITS_AND_WORKS() {

        Thread t = new Thread (new Runnable() {
            String result=null;

            @Override
            public void run() {
                result = testFut.get();
                //so it will keep in RUNNABLE state after retrieving result as aposed to TERMINATED
                while (result!=null);
            }
        }
        );
        t.start();
        //give t a chance to execute get()
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {

        }
        assertEquals(Thread.State.WAITING,t.getState());
        testFut.resolve("resolved");
        //give t a chance to get result
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {

        }
        assertEquals(Thread.State.RUNNABLE,t.getState());
    }
    @Test
    public void isDone_FALSE_IN_INI() {
        assertFalse(testFut.isDone());
    }
    @Test
    public void isDone_CHECK_IF_WORKS() {
        testFut.resolve("resolved");
        assertTrue(testFut.isDone());
    }

    @Test
    public void get1_CHECK_IF_WORKS_WHEN_NULL() {
        assertNull(testFut.get(10,TimeUnit.SECONDS));

    }
    @Test
    public void get1_CHECK_IF_WORKS_WHEN_NULL_AT_BEGINNING() {
       Thread t = new Thread(() ->
       {
           try {
               Thread.sleep(3000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           testFut.resolve("resolved");
       });
       t.start();
       String s = testFut.get(10,TimeUnit.SECONDS);
       assertEquals("resolved",s);
    }

}