package timer;

public interface TimerListener {

    void onTimerTick(int remainingTime);

    void onTimeOver();
}
