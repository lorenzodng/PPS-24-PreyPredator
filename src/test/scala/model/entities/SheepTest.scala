package model.entities

import model.*
import model.managers.EcosystemManager
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import zio.*

class SheepTest extends AnyFunSuite with Matchers:

  val runtime: Runtime[Any] = Runtime.default
  val sheep: Sheep = Sheep(EntityId.random, Position(50, 50))
  val grass: Grass = Grass(EntityId.random, Position(60, 60))
  val world: World = World(100, 100, Seq.empty, Seq(sheep), Seq(grass))
  val refWorld: Ref[World] = Unsafe.unsafe:
    implicit u =>
      runtime.unsafe.run(Ref.make(world)).getOrThrowFiberFailure()
  val ecosystemManager = new EcosystemManager(refWorld)

  def runUnsafe[E, A](zio: ZIO[Any, E, A]): A =
    Unsafe.unsafe:
      implicit u => runtime.unsafe.run(zio).getOrThrowFiberFailure()
  
  test("Sheep movement"):
    runUnsafe:
      for
        _ <- sheep.move(ecosystemManager)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        movedSheepOpt <- ZIO.succeed(updatedWorld.sheepById(sheep.id))
        _ <- movedSheepOpt match
          case Some(movedSheep) => ZIO.succeed(movedSheep.position should not equal sheep.position)
          case None => ZIO.fail(new Exception("Sheep not found"))
      yield ()

  test("Sheep eats"):
    val sheepAfterEat = sheep.eat()
    val expectedEnergy = 60.0
    sheepAfterEat.energy should be (60.0)


