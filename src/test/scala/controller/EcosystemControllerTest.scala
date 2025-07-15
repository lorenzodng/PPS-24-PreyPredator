package controller

import model.World
import model.managers.EcosystemManager
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import zio.{Ref, Runtime, UIO, Unsafe, ZIO}

class EcosystemControllerTest extends AnyFunSuite with Matchers:

  val width = 100
  val height = 100
  val numWolves = 1
  val numSheep = 1
  val numGrass = 1
  val grassInterval = 500
  val grassGenerated = 20
  val world: World = World(width, height, Seq.empty, Seq.empty, Seq.empty)
  val runtime: Runtime[Any] = Runtime.default
  val refWorld: Ref[World] = Unsafe.unsafe:
    implicit u =>
      runtime.unsafe.run(Ref.make(world)).getOrThrowFiberFailure()
  val ecosystemManager = new EcosystemManager(refWorld)
  val flag: Flag.type = Flag
  val controller = new EcosystemController(ecosystemManager, Flag)

  def runUnsafe[A](zio: UIO[A]): A =
    Unsafe.unsafe:
      implicit u => runtime.unsafe.run(zio).getOrThrowFiberFailure()

  test("Start simulation"):
    runUnsafe:
      for
        _ <- controller.startSimulation(numWolves, numSheep, numGrass, grassInterval, grassGenerated, width, height)
        _ <- ZIO.succeed(controller.isRunning shouldBe true)
        _ <- ZIO.succeed(flag.isSet shouldBe false)
      yield ()

  test("Stop simulation"):
    runUnsafe:
      for
        _ <- controller.startSimulation(numWolves, numSheep, numGrass, grassInterval, grassGenerated, width, height)
        _ <- controller.stopSimulation()
        _ <- ZIO.succeed(controller.isRunning shouldBe true)
        _ <- ZIO.succeed(flag.isSet shouldBe true)
      yield ()

  test("Reset simulation") :
    runUnsafe:
      for
        _ <- controller.startSimulation(numWolves, numSheep, numGrass, grassInterval, grassGenerated, width, height)
        _ <- controller.stopSimulation()
        _ <- controller.resetSimulation()
        world <- controller.ecosystemManager.getWorld
        _ <- ZIO.succeed(controller.isRunning shouldBe false)
        _ <- ZIO.succeed(flag.isSet shouldBe true)
        _ <- ZIO.succeed(world.entities.isEmpty shouldBe true)
      yield ()