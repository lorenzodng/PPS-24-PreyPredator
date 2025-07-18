package controller

/**
 * A simple mutable flag used to control simulation state
 * Provides methods to set, reset, and check the flag's status.
 */
object Flag:

  /**
   * Internal state of the flag. 
   * `true` if the simulation is running, `false` otherwise.
   */
  private var flag = false

  /**
   * Resets the flag to false.
   */
  def reset(): Unit =
    flag = false

  /**
   * Sets the flag to true.
   */
  def set(): Unit =
    flag = true

  /**
   * Returns whether the flag is set.
   * @return true if the flag is set, false otherwise
   */
  def isSet: Boolean = flag
