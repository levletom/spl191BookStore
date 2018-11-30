package bgu.spl.mics;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The {@link MessageBusImpl }class is the implementation of the MessageBus interface.
 * the class contract is that if a microservice wants to use the message bus it must first register to the messageBus
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private ConcurrentHashMap<MicroService,BlockingQueue<Message>> microServiceToBlockingQueue;
	private ConcurrentHashMap<Class<? extends Event>,BlockingQueue<MicroService>> eventToMicroServiceRoundRobinQueue;
	private ConcurrentHashMap<Class<? extends Broadcast>,BlockingQueue<MicroService>> broadcastToMicroServiceQueue;
	private ConcurrentHashMap<Event,Future> eventToItsReturnedFuture;

	//amit
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	//amit
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	//tomer


	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> fut = eventToItsReturnedFuture.get(e);
		if (fut != null)
			fut.resolve(result);
	}

	/**
	 * Adds the {@link Broadcast} {@code b} to the message queues of all the
	 * micro-services subscribed to {@code b.getClass()}.
	 * locks the specific queue of micro-services subscribed to {@link Broadcast} {@code b}
	 * <p>
	 * @param b 	The message to added to the queues.
	 *
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
		if(b!=null) {
			BlockingQueue<MicroService> broadcastTypeQueue = broadcastToMicroServiceQueue.get(b.getClass());
			synchronized (broadcastTypeQueue) {
				for (MicroService m :
						broadcastTypeQueue) {
					BlockingQueue<Message> microServiceQueue = microServiceToBlockingQueue.get(m);
					if (microServiceQueue != null)
						microServiceQueue.add(b);
				}
			}
		}
	}

	//tomer
	/**
	 * Adds the {@link Event} {@code e} to the message queue of one of the
	 * micro-services subscribed to {@code e.getClass()} in a round-robin
	 * fashion. This method should be non-blocking.
	 * the method adds the {@link Event} {@code e} to the micro service if at the point of checking it is subscribed to
	 * {@code e.getClass()} round roins queue.
	 * if it unsubscribes via {@code unregister()} after the check, the event is still added.
	 * at the end of the method the event is mapped to its returned future in a private member.
	 * <p>
	 * @param <T>    	The type of the result expected by the event and its corresponding future object.
	 * @param e     	The event to add to the queue.
	 * @return {@link Future<T>} object to be resolved once the processing is complete,
	 * 	       null in case no micro-service has subscribed to {@code e.getClass()}.
	 */
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		BlockingQueue<MicroService> specificEventsRoundRobinQueue = eventToMicroServiceRoundRobinQueue.get(e.getClass());
		if(specificEventsRoundRobinQueue!=null && !specificEventsRoundRobinQueue.isEmpty()){
			MicroService servieToHandleEvent = specificEventsRoundRobinQueue.poll();
			if(servieToHandleEvent!=null){
				BlockingQueue<Message> specificServiceMessageLoopQueue = microServiceToBlockingQueue.get(servieToHandleEvent);

					if(specificServiceMessageLoopQueue!=null) {
						specificServiceMessageLoopQueue.offer(e);
						Future<T>  returnedFuture = new Future<>();
						eventToItsReturnedFuture.put(e,returnedFuture);
						return returnedFuture;
					}


			}
		}
		return null;
	}

	//amit
	@Override
	public void register(MicroService m) {
		// TODO Auto-generated method stub

	}

	//amit
	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub

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
	 * @param m The micro-service requesting to take a message from its message
	 *          queue.
	 * @return The next message in the {@code m}'s queue (blocking).
	 * @throws InterruptedException if interrupted while waiting for a message
	 *                              to became available.
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> specificServiceQueue = microServiceToBlockingQueue.get(m);
		if(specificServiceQueue==null)
			throw new IllegalStateException("Micro-Service: " +m.getName()+" attempted to get a message before registering");
		Message returnedMessage = specificServiceQueue.take();
		return returnedMessage;
	}

	//amit
	public static MessageBusImpl getInstance(){
		//TODO implement
		throw new NotImplementedException();
	}

	

}
