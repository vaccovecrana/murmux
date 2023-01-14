package io.vacco.murmux.filter;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Worker module for FilterTasks.
 */
public class MxWorker extends TimerTask {

  private final MxFilterTask middlewareWorker;
  private Timer timer;

  public MxWorker(MxFilterTask middlewareWorker) {
    this.middlewareWorker = middlewareWorker;
  }

  public void start() {
    if (this.timer == null) {
      middlewareWorker.onStart();
      this.timer = new Timer();
      this.timer.scheduleAtFixedRate(this, 0, middlewareWorker.getDelay());
    }
  }

  public void stop() {
    if (timer != null) {
      middlewareWorker.onStop();
      this.timer.cancel();
      this.timer = null;
    }
  }

  public boolean isActive() {
    return timer != null;
  }

  @Override
  public void run() {
    middlewareWorker.onUpdate();
  }
}
