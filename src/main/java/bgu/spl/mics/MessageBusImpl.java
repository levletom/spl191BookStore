package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * The {@link MessageBusImpl }class is the implementation of the MessageBus interface.
 * the class contract is that if a microservice wants to use the message bus it must first register to the messageBus
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
    private ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServiceToBlockingQueue;
    private ConcurrentHashMap<Class<? extends Event>, BlockingQueue<MicroService>> eventToMicroServiceRoundRobinQueue;
    private ConcurrentHashMap<Class<? extends Broadcast>, BlockingQueue<MicroService>> broadcastToMicroServiceQueue;
    private ConcurrentHashMap<Event, Future> eventToItsReturnedFuture;
    private ConcurrentHashMap<MicroService, BlockingQueue<Class<? extends Event>>> microserviceToTypeOfEventItsRegisteredTo;
    private ConcurrentHashMap<MicroService, BlockingQueue<Class<? extends Broadcast>>> microserviceToTypeOfBroadcastItsRegisteredTo;

    private static MessageBusImpl instance = null;


    private MessageBusImpl() {
        microServiceToBlockingQueue = new ConcurrentHashMap<>();
        eventToMicroServiceRoundRobinQueue = new ConcurrentHashMap<>();
        broadcastToMicroServiceQueue = new ConcurrentHashMap<>();
        eventToItsReturnedFuture = new ConcurrentHashMap<>();
        microserviceToTypeOfBroadcastItsRegisteredTo = new ConcurrentHashMap<>();
        microserviceToTypeOfEventItsRegisteredTo = new ConcurrentHashMap<>();

    }

    /**
     * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
     * locks if event type was never subscribed to, on the type of the event.
     * <p>
     *
     * @param <T>  The type of the result expected by the completed event.
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     */
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        if (m != null && type != null) {
            BlockingQueue<MicroService> specificEventListOfServices = eventToMicroServiceRoundRobinQueue.get(type);
            if (specificEventListOfServices == null) {
                synchronized (type) {
                    eventToMicroServiceRoundRobinQueue.putIfAbsent(type, new LinkedBlockingQueue<>());
                }
            }
            specificEventListOfServices = eventToMicroServiceRoundRobinQueue.get(type);
            if (!specificEventListOfServices.contains(m) && microServiceToBlockingQueue.get(m) != null) {
                microserviceToTypeOfEventItsRegisteredTo.get(m).offer(type);
                eventToMicroServiceRoundRobinQueue.get(type).offer(m);
            }
        }
    }


    /**
     * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}
     * locks if Brodcast was never subscribed to, on the type of the broadcast
     * <p>
     *
     * @param type The type to subscribe to.
     * @param m    The subscribing micro-service.
     */
    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        if (type != null && m != null) {
            BlockingQueue<MicroService> specificBroadcastListOfServices = broadcastToMicroServiceQueue.get(type);
            if (specificBroadcastListOfServices == null) {
                synchronized (type) {
                    broadcastToMicroServiceQueue.putIfAbsent(type, new LinkedBlockingQueue<>());
                }
            }
            specificBroadcastListOfServices = broadcastToMicroServiceQueue.get(type);
            if (!specificBroadcastListOfServices.contains(m) && microServiceToBlockingQueue.get(m) != null) {

                specificBroadcastListOfServices.offer(m);
                microserviceToTypeOfBroadcastItsRegisteredTo.get(m).offer(type);

            }
        }
    }


    /**
     * completes the specified event with a result
     * if the event was already completed - does nothing
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     * @param <T>
     */
    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> fut = eventToItsReturnedFuture.get(e);

        if (fut != null&&!fut.isDone()) {
            fut.resolve(result);
        }
    }

    /**
     * Adds the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     *
     * @param b The message to added to the queues.
     */
    @Override
    public void sendBroadcast(Broadcast b) {
        if (b != null) {
            BlockingQueue<MicroService> broadcastTypeQueue = broadcastToMicroServiceQueue.get(b.getClass());
            if (broadcastTypeQueue != null) {

                for (MicroService m :
                        broadcastTypeQueue) {
                    BlockingQueue<Message> microServiceQueue = microServiceToBlockingQueue.get(m);
                    if (microServiceQueue != null)
                        microServiceQueue.add(b);
                }

            }
        }
    }

    /**
     * Adds the {@link Event} {@code e} to the message queue of one of the
     * micro-services subscribed to {@code e.getClass()} in a round-robin
     * fashion. This method should be non-blocking.
     * the method adds the {@link Event} {@code e} to the micro service if at the point of checking it is subscribed to
     * {@code e.getClass()} round roins queue.
     * if it unsubscribes via {@code unregister()} after the check, the event is still added.
     * at the end of the method the event is mapped to its returned future in a private member.
     * <p>
     *
     * @param <T> The type of the result expected by the event and its corresponding future object.
     * @param e   The event to add to the queue.
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * null in case no micro-service has subscribed to {@code e.getClass()}.
     */
    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        BlockingQueue<MicroService> specificEventsRoundRobinQueue = eventToMicroServiceRoundRobinQueue.get(e.getClass());
        if (specificEventsRoundRobinQueue != null && !specificEventsRoundRobinQueue.isEmpty()) {
            MicroService serviceToHandleEvent;

            synchronized (specificEventsRoundRobinQueue) {
                serviceToHandleEvent = specificEventsRoundRobinQueue.poll();
                if (serviceToHandleEvent != null) {
                    specificEventsRoundRobinQueue.offer(serviceToHandleEvent);

                }
            }

            if (serviceToHandleEvent != null) {
                BlockingQueue<Message> specificServiceMessageLoopQueue = microServiceToBlockingQueue.get(serviceToHandleEvent);

                if (specificServiceMessageLoopQueue != null) {
                    Future<T> returnedFuture = new Future<>();
                    eventToItsReturnedFuture.put(e, returnedFuture);
                    specificServiceMessageLoopQueue.offer(e);
                    return returnedFuture;

                }


            }
        }
        return null;
    }

    /**
     * Allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     *
     * @param m the micro-service to create a queue for.
     */
    @Override
    public void register(MicroService m) {
        if (m != null) {
            if (!microServiceToBlockingQueue.containsKey(m)) {
                //creates messages loop queue
                microServiceToBlockingQueue.put(m, new LinkedBlockingQueue<Message>());
                //creates vectors indicating witch type of message the microservice has subscribed to
                microserviceToTypeOfEventItsRegisteredTo.put(m, new LinkedBlockingQueue<>());
                microserviceToTypeOfBroadcastItsRegisteredTo.put(m, new LinkedBlockingQueue<>());
            }
        }
    }

    /**
     * Removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     *
     * @param m the micro-service to unregister.
     */
    @Override
    public void unregister(MicroService m) {
        if (m != null) {
            if (microServiceToBlockingQueue.get(m) != null) {
                BlockingQueue<Class<? extends Broadcast>> typeOfBroadCastVec = microserviceToTypeOfBroadcastItsRegisteredTo.get(m);
                for (Class<? extends Broadcast> bc :
                        typeOfBroadCastVec) {
                    BlockingQueue<MicroService> broadcastTypeQueue = broadcastToMicroServiceQueue.get(bc);
                    if (broadcastTypeQueue != null)
                        broadcastTypeQueue.remove(m);

                }
                typeOfBroadCastVec.clear();
                microserviceToTypeOfBroadcastItsRegisteredTo.remove(m);
                BlockingQueue<Class<? extends Event>> typeOfEventVec = microserviceToTypeOfEventItsRegisteredTo.get(m);
                for (Class<? extends Event> e :
                        typeOfEventVec) {
                    BlockingQueue<MicroService> eventTypeQueue = eventToMicroServiceRoundRobinQueue.get(e);
                    if(eventTypeQueue!=null)
                        eventTypeQueue.remove(m);
                }
                typeOfEventVec.clear();
                BlockingQueue<Message> thisMicroServiceQueue = microServiceToBlockingQueue.get(m);
                for (Message mes :
                        thisMicroServiceQueue) {
                    if (mes instanceof Event) {


                        complete((Event) mes, null);
                    }
                }
                microServiceToBlockingQueue.remove(m);
            }


        }
    }

    //tomer

    /**
     * Using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking meaning that if no messages
     * are available in the micro-service queue it
     * should wait until a message becomes available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     *
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return The next message in the {@code m}'s queue (blocking).
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     */

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        BlockingQueue<Message> specificServiceQueue = microServiceToBlockingQueue.get(m);
        if (specificServiceQueue == null)
            throw new IllegalStateException("Micro-Service: " + m.getName() + " attempted to get a message before registering");
        Message returnedMessage = specificServiceQueue.take();
        return returnedMessage;
    }

    //amit


    public static MessageBusImpl getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }


}
