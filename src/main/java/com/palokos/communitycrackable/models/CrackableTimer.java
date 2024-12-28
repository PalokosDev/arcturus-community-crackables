package com.palokos.communitycrackable.models;

import com.eu.habbo.Emulator;
import com.palokos.communitycrackable.CommunityCrackable;
import com.palokos.communitycrackable.CommunityCrackablePlugin;

import java.util.Timer;
import java.util.TimerTask;

public class CrackableTimer {
    private final CommunityCrackable crackable;
    private final Timer timer;
    private final int timeInMinutes;
    private final long startTime;
    private boolean running;
    
    public CrackableTimer(CommunityCrackable crackable, int timeInMinutes) {
        this.crackable = crackable;
        this.timer = new Timer(true); // Daemon timer
        this.timeInMinutes = timeInMinutes;
        this.startTime = System.currentTimeMillis();
        this.running = false;
    }
    
    public void start() {
        running = true;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Emulator.getThreading().run(() -> {
                    stop();
                    // Broadcast time expired message to room
                    Emulator.getGameEnvironment().getRoomManager()
                        .getRoom(crackable.getId())
                        .sendComposer(new WhisperComposer(
                            -1,
                            "Die Zeit ist abgelaufen! Das Community Crackable verschwindet.",
                            2
                        ).compose());
                });
            }
        }, timeInMinutes * 60 * 1000);

        // Schedule periodic updates
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (running) {
                    sendTimeUpdate();
                }
            }
        }, 60000, 60000); // Update every minute
    }
    
    public void stop() {
        running = false;
        timer.cancel();
        CommunityCrackablePlugin.INSTANCE.removeCrackable(crackable.getId());
    }
    
    private void sendTimeUpdate() {
        long remainingTime = timeInMinutes * 60 * 1000 - (System.currentTimeMillis() - startTime);
        if (remainingTime > 0) {
            int remainingMinutes = (int) (remainingTime / 60000);
            Emulator.getGameEnvironment().getRoomManager()
                .getRoom(crackable.getId())
                .sendComposer(new WhisperComposer(
                    -1,
                    "Verbleibende Zeit: " + remainingMinutes + " Minuten",
                    2
                ).compose());
        }
    }
    
    public boolean isRunning() {
        return running;
    }

    public int getRemainingMinutes() {
        if (!running) return 0;
        long remainingTime = timeInMinutes * 60 * 1000 - (System.currentTimeMillis() - startTime);
        return (int) (remainingTime / 60000);
    }
}
