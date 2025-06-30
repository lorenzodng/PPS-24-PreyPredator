package controller

object Flag:

  private var flag = false

  def reset(): Unit = 
    flag = false

  def set(): Unit = 
    flag = true

  def isSet: Boolean = flag

