package controller

import model.World
import model.managers.EcosystemManager
import view.SimulationView

import java.awt.Toolkit

@main def runSimulation(): Unit =

  val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  val width = screenSize.width
  val height = screenSize.height

  val world = World(width, height, Seq.empty, Seq.empty, Seq.empty)
  val ecosystemManager = new EcosystemManager(world)
  val stopFlag = Flag
  val ecosystemController = new EcosystemController(ecosystemManager, stopFlag)
  new SimulationView(ecosystemController).open()