package timer;

import java.util.Timer;
import java.util.TimerTask;

public class AuctionTimer {
    private final int timeoutInSeconds;
    private final TimerListener listener;
    private int secondsPassed = 0;
    private Timer timer;

    public AuctionTimer(int timeoutInSeconds, TimerListener listener) {
        this.timeoutInSeconds = timeoutInSeconds;
        this.listener = listener;
    }

    public void startTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                secondsPassed++;
                listener.onTimerTick(timeoutInSeconds - secondsPassed);

                if (secondsPassed >= timeoutInSeconds) {
                    listener.onTimeOver();
                    timer.cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 1000);
        listener.onTimerTick(timeoutInSeconds - secondsPassed);
    }

    public void resetTimer() {
        secondsPassed = 0;
    }
}