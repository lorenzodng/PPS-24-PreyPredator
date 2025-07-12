package controller

import model.World
import model.managers.EcosystemManager
import view.SimulationView
import zio.Unsafe.unsafe
import zio.{Ref, Runtime}

@main def runSimulation(): Unit =

  val emptyWorld = World(0, 0, Seq.empty, Seq.empty, Seq.empty)

  unsafe:
    implicit u =>
      val worldRef = Runtime.default.unsafe.run(Ref.make(emptyWorld)).getOrThrowFiberFailure()
      val ecosystemManager = new EcosystemManager(worldRef)
      val stopFlag = Flag
      val ecosystemController = new EcosystemController(ecosystemManager, stopFlag)
      val simulationView = new SimulationView(ecosystemController)
      ecosystemController.setUpdateViewCallback(() => 
        simulationView.updateView()
      )
      ecosystemController.setExtinctionCallback(() => 
        simulationView.updateView()
        simulationView.updateButtons()
      )

      simulationView.open()
  