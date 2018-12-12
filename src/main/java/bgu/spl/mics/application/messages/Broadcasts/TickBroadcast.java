package bgu.spl.mics.application.messages.Broadcasts;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    private int tick;
    private boolean isFinalTick;

    public TickBroadcast(int tick,boolean isFinalTick){
        this.tick=tick;
        this.isFinalTick = isFinalTick;
    }

    public int getTick() {
        return tick;
    }

    public boolean isFinalTick() {
        return isFinalTick;
    }
}
