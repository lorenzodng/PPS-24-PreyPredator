package controller

import model.World
import model.managers.EcosystemManager
import view.SimulationView
import zio.Unsafe.unsafe
import zio.{Ref, Runtime}
import java.awt.Toolkit

@main def runSimulation(): Unit =

  val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  val width = screenSize.width
  val height = screenSize.height
  val initialWorld = World(width, height, Seq.empty, Seq.empty, Seq.empty)

  unsafe:
    implicit u =>
      val worldRef = Runtime.default.unsafe.run(Ref.make(initialWorld)).getOrThrowFiberFailure()
      val ecosystemManager = new EcosystemManager(worldRef)
      val stopFlag = Flag
      val ecosystemController = new EcosystemController(ecosystemManager, stopFlag)
      val simulationView = new SimulationView(ecosystemController)
      ecosystemController.setUpdateViewCallback(() => simulationView.updateView())

      simulationView.open()
  