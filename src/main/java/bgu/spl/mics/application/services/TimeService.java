package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.Broadcasts.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link Tick Broadcast}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {
	private int numberOfMillieSecondsForEachClockTick;
	private int numberOfTicksBeforeTermination;
	Timer timer;
	TimerTask ourTimerTask;
	private AtomicInteger tick;

	public TimeService(int numberOfMillieSecondsForEachClockTick, int numberOfTicksBeforeTermination) {
		super("TimeService");
		this.numberOfMillieSecondsForEachClockTick = numberOfMillieSecondsForEachClockTick;
		this.numberOfTicksBeforeTermination = numberOfTicksBeforeTermination;
		this.tick = new AtomicInteger(0);
		timer = new Timer();
	}

	@Override
	protected void initialize() {

			timer.schedule(new TimerTask(){
				public void run(){
					tick.incrementAndGet();
					if(tick.get() < numberOfTicksBeforeTermination)
					    sendBroadcast(new TickBroadcast(tick.get(),false));
					    else{
					    	sendBroadcast(new TickBroadcast(tick.get(),true));
					    timer.cancel();
					    }
				}
		}, 1000*numberOfMillieSecondsForEachClockTick);

	}




}