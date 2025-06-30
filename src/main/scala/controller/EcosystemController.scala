package controller

import model.managers.EcosystemManager
import java.awt.Window
import java.util.{Timer, TimerTask}
import scala.swing.Swing.onEDT

class EcosystemController(val ecosystemManager: EcosystemManager, var stopFlag: Flag.type):

  private var timer: Option[Timer] = None

  def startSimulation(nWolves: Int, nSheep: Int, nGrass: Int): Unit =
    stopFlag.reset()
    val newTimer = new Timer()
    val task = new TimerTask:
      override def run(): Unit =

        if ecosystemManager.world.entities.isEmpty then
          ecosystemManager.world = ecosystemManager.world.generateGrass(nGrass).generateSheep(nSheep).generateWolves(nWolves)

        for sheep <- ecosystemManager.world.sheep do
          sheep.move(ecosystemManager)
        for wolf <- ecosystemManager.world.wolves do
          wolf.move(ecosystemManager)

        if !stopFlag.isSet then
          ecosystemManager.tick()
          onEDT(Window.getWindows.foreach(_.repaint()))

    newTimer.scheduleAtFixedRate(task, 0, 30)
    timer = Some(newTimer)

  def stopSimulation(): Unit =
    timer.foreach(_.cancel())
    timer = None
    stopFlag.set()

  def resetSimulation(): Unit =
    ecosystemManager.world = ecosystemManager.world.deleteEntities()

  
  
