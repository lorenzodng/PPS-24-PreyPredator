package controller

import java.awt.Window
import java.util.{Timer, TimerTask}
import scala.swing.Swing.onEDT

object EcosystemController:
  
  private val timer = new Timer()
  
  private val task: TimerTask = new TimerTask: 
    override def run(): Unit =
      AIMovement.moveAI("p1", manager) 
      manager.tick() 
      onEDT(Window.getWindows.foreach(_.repaint())) 
  timer.scheduleAtFixedRate(task, 0, 30) 
  
  
