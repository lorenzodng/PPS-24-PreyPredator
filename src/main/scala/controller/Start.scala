package controller

import model.World
import model.managers.EcosystemManager
import view.SimulationView
import zio.Unsafe.unsafe
import zio.{Ref, Runtime}

/**
 * Main entry point to run the Ecosystem Simulation application.
 *
 * <p>
 * Initializes an empty world and creates the necessary components to run the simulation. 
 * Sets up callbacks to update the view upon simulation state changes and opens the simulation window.
 * </p>
 *
 * This function is annotated with '@main' and executed when the application starts.
 */
@main def runSimulation(): Unit =

  val emptyWorld = World(0, 0, Seq.empty, Seq.empty, Seq.empty)

  unsafe:
    implicit u =>
      val runtime = Runtime.default

      val worldRef = runtime.unsafe.run(Ref.make(emptyWorld)).getOrThrowFiberFailure()
      val ecosystemManager = new EcosystemManager(worldRef)
      val stopFlag = Flag
      val ecosystemController = new EcosystemController(ecosystemManager, stopFlag)

      val simulationView = new SimulationView(ecosystemController)

      ecosystemController.setUpdateViewCallback(() =>
        simulationView.viewManager.updateView())

      ecosystemController.setExtinctionCallback(() =>
        simulationView.viewManager.updateView()
        simulationView.viewManager.updateButtons())

      simulationView.open()