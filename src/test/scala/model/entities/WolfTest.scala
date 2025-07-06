package model.entities

import model.*
import model.entities.{EntityId, Sheep, Wolf}
import model.managers.EcosystemManager
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import zio.*

class WolfTest extends AnyFunSuite with Matchers:

  val WorldWidth = 100
  val WorldHeight = 100
  val WolfPosition: Position = Position(50, 50)
  val SheepPosition: Position = Position(60, 60)
  
  val runtime: Runtime[Any] = Runtime.default
  val wolf: Wolf = Wolf(EntityId.random, WolfPosition)
  val sheep: Sheep = Sheep(EntityId.random, SheepPosition)
  val world: World = World(WorldWidth, WorldHeight, Seq(wolf), Seq(sheep), Seq.empty)
  val refWorld: Ref[World] = Unsafe.unsafe:
    implicit u =>
      runtime.unsafe.run(Ref.make(world)).getOrThrowFiberFailure()
  val ecosystemManager = new EcosystemManager(refWorld)

  test("Wolf movement"):
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- wolf.move(ecosystemManager)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            movedWolfOpt <- ZIO.succeed(updatedWorld.wolfById(wolf.id))
            _ <- movedWolfOpt match
              case Some(movedWolf) =>
                ZIO.succeed(movedWolf.position should not equal wolf.position)
              case None =>
                ZIO.fail(new Exception("Wolf not found"))
          yield ()
        }.getOrThrowFiberFailure()

  test("Wolf eats"):
    val wolfAfterEat = wolf.eat
    val expectedEnergy = 60.0
    wolfAfterEat.energy should be (expectedEnergy)


