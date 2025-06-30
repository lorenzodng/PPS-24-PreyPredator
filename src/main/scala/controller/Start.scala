package controller

import model.World
import model.managers.EcosystemManager
import view.SimulationView

@main def runSimulation(): Unit =

  val width = 1000
  val height = 1000

  val world = World(width, height, Seq.empty, Seq.empty, Seq.empty)
  val ecosystemManager = new EcosystemManager(world)
  val stopFlag = Flag
  val ecosystemController = new EcosystemController(ecosystemManager, stopFlag)
  new SimulationView(ecosystemController).open()