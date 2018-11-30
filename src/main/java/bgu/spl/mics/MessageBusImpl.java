package bgu.spl.mics;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private ConcurrentHashMap<MicroService,BlockingQueue<Message>> microServiceToBlockingQueue;
	private ConcurrentHashMap<Class<? extends Event>,BlockingQueue<MicroService>> eventToMicroServiceRoundRobinQueue;
	private ConcurrentHashMap<Class<? extends Broadcast>,BlockingQueue<MicroService>> broadcastToMicroServiceQueue;
	private ConcurrentHashMap<Event<?>,Future<?>> eventToItsReturnedFuture;
	private ConcurrentHashMap<MicroService, Vector<Class<? extends Event>>> microserviceToTypeOfEventItsRegisteredTo;
	private ConcurrentHashMap<MicroService, Vector<Class<? extends Broadcast>>> microserviceToTypeOfBroadcastItsRegisteredTo;
	private Object lockForSubscribeEvent;
	private Object lockForSubscribeBroadcast;

	private static MessageBusImpl instance = null;


	private MessageBusImpl() {
		microServiceToBlockingQueue = new ConcurrentHashMap<>();
		eventToMicroServiceRoundRobinQueue = new ConcurrentHashMap<>();
		broadcastToMicroServiceQueue = new ConcurrentHashMap<>();
		eventToItsReturnedFuture = new ConcurrentHashMap<>();
		microserviceToTypeOfBroadcastItsRegisteredTo = new ConcurrentHashMap<>();
		microserviceToTypeOfEventItsRegisteredTo = new ConcurrentHashMap<>();
		lockForSubscribeEvent = new Object();
		lockForSubscribeBroadcast = new Object();
	}

	/**
	 * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
	 * locks if event was never subscribed to
	 * <p>
	 * @param <T>  The type of the result expected by the completed event.
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service.
	 */
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (m != null && type != null) {
			BlockingQueue<MicroService> specificEventListOfServices = eventToMicroServiceRoundRobinQueue.get(type);
			if(specificEventListOfServices==null){
				synchronized (lockForSubscribeEvent) {
					specificEventListOfServices = eventToMicroServiceRoundRobinQueue.get(type);
					if(specificEventListOfServices==null) {
						eventToMicroServiceRoundRobinQueue.put(type, new LinkedBlockingQueue<MicroService>());
						specificEventListOfServices = eventToMicroServiceRoundRobinQueue.get(type);
					}
				}
			}

			if (  !specificEventListOfServices.contains(m) && microServiceToBlockingQueue.get(m) != null)  {
				microserviceToTypeOfEventItsRegisteredTo.get(m).add(type);
				eventToMicroServiceRoundRobinQueue.get(type).offer(m);
			}
		}
	}



	/**
	 * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
	 * locks on the specific queue that it works on if exists.
	 * locks if Brodcast was never subscribed to
	 * <p>
	 * @param type 	The type to subscribe to.
	 * @param m    	The subscribing micro-service.
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (type != null && m != null) {
			BlockingQueue<MicroService> specificBroadcastListOfServices = broadcastToMicroServiceQueue.get(type);
			if(specificBroadcastListOfServices==null){
				synchronized (lockForSubscribeBroadcast) {
					specificBroadcastListOfServices = broadcastToMicroServiceQueue.get(type);
					if(specificBroadcastListOfServices==null) {
						broadcastToMicroServiceQueue.put(type, new LinkedBlockingQueue<MicroService>());
						specificBroadcastListOfServices = broadcastToMicroServiceQueue.get(type);
					}
				}
			}
			if(!specificBroadcastListOfServices.contains(m)&& microServiceToBlockingQueue.get(m)!=null) {
				synchronized (specificBroadcastListOfServices){
					specificBroadcastListOfServices.offer(m);
					microserviceToTypeOfBroadcastItsRegisteredTo.get(m).add(type);
				}
			}
		}
	}

	//tomer
	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	//tomer
	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}

	//tomer
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Allocates a message-queue for the {@link MicroService} {@code m}.
	 * <p>
	 * @param m the micro-service to create a queue for.
	 */
	@Override
	public void register(MicroService m) {
		if (m != null) {
			if (!microServiceToBlockingQueue.containsKey(m)) {
				//creates messages loop queue
				microServiceToBlockingQueue.put(m, new LinkedBlockingQueue<Message>());
				//creates vectors indicating witch type of message the microservice has subscribed to
				microserviceToTypeOfEventItsRegisteredTo.put(m, new Vector<Class<? extends Event>>());
				microserviceToTypeOfBroadcastItsRegisteredTo.put(m, new Vector<Class<? extends Broadcast>>());
			}
		}
	}

	/**
	 * Removes the message queue allocated to {@code m} via the call to
	 * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
	 * related to {@code m} in this message-bus. If {@code m} was not
	 * registered, nothing should happen.
	 * <p>
	 * @param m the micro-service to unregister.
	 */
	@Override
	public void unregister(MicroService m) {
		if(m!=null){
			Vector<Class<? extends Broadcast>> typeOfBroadCastVec = microserviceToTypeOfBroadcastItsRegisteredTo.get(m);
			for (Class<? extends Broadcast> bc:
				 typeOfBroadCastVec) {
				BlockingQueue<MicroService> broadcastTypeQueue = broadcastToMicroServiceQueue.get(bc);
				synchronized (broadcastTypeQueue){
				 	broadcastTypeQueue.remove(m);
			}
			typeOfBroadCastVec.clear();
			}
			Vector<Class<? extends Event>> typeOfEventVec = microserviceToTypeOfEventItsRegisteredTo.get(m);
			for (Class<? extends Event> e:
				 typeOfEventVec) {
				eventToMicroServiceRoundRobinQueue.get(e).remove(m);
			}
		}
	}

	//tomer
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	//amit


	public static MessageBusImpl getInstance() {
		if(instance == null) {
			instance = new MessageBusImpl();
		}
		return instance;
	}




}
