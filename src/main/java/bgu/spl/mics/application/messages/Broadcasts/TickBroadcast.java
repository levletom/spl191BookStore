package bgu.spl.mics.application.messages.Broadcasts;

public class TickBroadcast {
    private int tick;
    public TickBroadcast(int tick){
        this.tick=tick;
    }

    public int getTick() {
        return tick;
    }
}
