package controller

import model.World
import model.managers.EcosystemManager
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import zio.{Ref, Runtime, Unsafe, ZIO}

class EcosystemControllerTest extends AnyFunSuite with Matchers:

  val Width = 100
  val Height = 100
  
  val world: World = World(Width, Height, Seq.empty, Seq.empty, Seq.empty)
  val runtime: Runtime[Any] = Runtime.default
  val refWorld: Ref[World] = Unsafe.unsafe:
    implicit u =>
      runtime.unsafe.run(Ref.make(world)).getOrThrowFiberFailure()
  val ecosystemManager = new EcosystemManager(refWorld)
  val flag: Flag.type = Flag
  val controller = new EcosystemController(ecosystemManager, Flag)
  val numWolves = 1
  val numSheep = 1
  val numGrass = 1

  test("Start the simulation"):
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- controller.startSimulation(numWolves, numSheep, numGrass)
            _ = controller.isRunning shouldBe true
            _ = flag.isSet shouldBe false
          yield ()
        }.getOrThrowFiberFailure()

  test("Stop the simulation"):
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- controller.startSimulation(numWolves, numSheep, numGrass)
            _ <- controller.stopSimulation()
            _ <- ZIO.succeed(controller.isRunning shouldBe true)
            _ <- ZIO.succeed(flag.isSet shouldBe true)
          yield ()
        }.getOrThrowFiberFailure()

  test("Reset the simulation") :
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- controller.startSimulation(numWolves, numSheep, numGrass)
            _ <- controller.stopSimulation()
            _ <- controller.resetSimulation()
            world <- controller.ecosystemManager.getWorld
            _ <- ZIO.succeed(controller.isRunning shouldBe false)
            _ <- ZIO.succeed(flag.isSet shouldBe true)
            _ <- ZIO.succeed(world.entities.isEmpty shouldBe true)
          yield ()
        }.getOrThrowFiberFailure()
