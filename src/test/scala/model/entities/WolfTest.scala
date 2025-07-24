package model.entities

import model.*
import model.managers.EcosystemManager
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import zio.*

class WolfTest extends AnyFunSuite with Matchers:
  
  val runtime: Runtime[Any] = Runtime.default
  val wolf: Wolf = Wolf(EntityId.random, Position(50, 50))
  val sheep: Sheep = Sheep(EntityId.random, Position(60, 60))
  val world: World = World(100, 100, Seq(wolf), Seq(sheep), Seq.empty)
  val refWorld: Ref[World] = Unsafe.unsafe:
    implicit u =>
      runtime.unsafe.run(Ref.make(world)).getOrThrowFiberFailure()
  val ecosystemManager = new EcosystemManager(refWorld)
  
  def runUnsafe[E, A](zio: ZIO[Any, E, A]): A =
    Unsafe.unsafe:
      implicit u => runtime.unsafe.run(zio).getOrThrowFiberFailure()

  test("Wolf movement"):
    runUnsafe:
      for
        _ <- wolf.move(ecosystemManager)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        movedWolfOpt <- ZIO.succeed(updatedWorld.wolfById(wolf.id))
        _ <- movedWolfOpt match
          case Some(movedWolf) => ZIO.succeed(movedWolf.position should not equal wolf.position)
          case None => ZIO.fail(new Exception("Wolf not found"))
      yield ()

  test("Wolf eats"):
    val wolfAfterEat = wolf.eat()
    val expectedEnergy = 60.0
    wolfAfterEat.energy should be (expectedEnergy)


