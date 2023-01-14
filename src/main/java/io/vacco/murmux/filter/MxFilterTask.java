package io.vacco.murmux.filter;

/**
 * Interface for filter tasks.
 */
public interface MxFilterTask {

  /**
   * Returns the delay between the updates
   *
   * @return Update delay in milliseconds
   */
  long getDelay();

  /**
   * Will be fired between the delays
   */
  void onUpdate();

  /**
   * Will be fired on instance start
   */
  void onStart();

  /**
   * Will be fired on instance stop
   */
  void onStop();
}
