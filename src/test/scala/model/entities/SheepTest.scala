package model.entities

import model.*
import model.entities.{EntityId, Grass, Sheep}
import model.managers.EcosystemManager
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import zio.*

class SheepTest extends AnyFunSuite with Matchers:

  val Width = 100
  val Height = 100
  val SheepPosition: Position = Position(50, 50)
  val GrassPosition: Position = Position(60, 60)
  
  val runtime: Runtime[Any] = Runtime.default
  val sheep: Sheep = Sheep(EntityId.random, SheepPosition)
  val grass: Grass = Grass(EntityId.random, GrassPosition)
  val world: World = World(Width, Height, Seq.empty, Seq(sheep), Seq(grass))
  val refWorld: Ref[World] = Unsafe.unsafe:
    implicit u =>
      runtime.unsafe.run(Ref.make(world)).getOrThrowFiberFailure()
  val ecosystemManager = new EcosystemManager(refWorld)

  test("Sheep movement"):
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- sheep.move(ecosystemManager)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            movedSheepOpt <- ZIO.succeed(updatedWorld.sheepById(sheep.id))
            _ <- movedSheepOpt match
              case Some(movedSheep) =>
                ZIO.succeed(movedSheep.position should not equal sheep.position)
              case None =>
                ZIO.fail(new Exception("Sheep not found"))
          yield ()
        }.getOrThrowFiberFailure()

  test("Sheep eats"):
    val sheepAfterEat = sheep.eat
    val expectedEnergy = 60.0
    sheepAfterEat.energy should be (60.0)


