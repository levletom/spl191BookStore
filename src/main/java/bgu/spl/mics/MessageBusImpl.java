package bgu.spl.mics;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private ConcurrentHashMap<MicroService,BlockingQueue<Message>> microServiceToBlockingQueue;
	private ConcurrentHashMap<Class<? extends Event>,BlockingQueue<MicroService>> eventToMicroServiceRoundRobinQueue;
	private ConcurrentHashMap<Class<? extends Broadcast>,BlockingQueue<MicroService>> broadcastToMicroServiceRoundRobinQueue;
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
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	//amit
	public static MessageBusImpl getInstance(){
		//TODO implement
		throw new NotImplementedException();
	}

	

}
