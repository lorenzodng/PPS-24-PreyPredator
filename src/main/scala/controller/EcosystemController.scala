package controller

import model.managers.EcosystemManager
import zio.*

class EcosystemController(val ecosystemManager: EcosystemManager, var stopFlag: Flag.type):

  private var fiber: Option[Fiber.Runtime[Throwable, Unit]] = None
  private var updateViewCallback: () => Unit = () => ()

  def setUpdateViewCallback(cb: () => Unit): Unit =
    updateViewCallback = cb

  def startSimulation(nWolves: Int, nSheep: Int, nGrass: Int): UIO[Unit] =
    def loop: UIO[Unit] =
      for
        start <- Clock.nanoTime
        _ <- for
          world <- ecosystemManager.getWorld
          updatedWorld <- if world.entities.isEmpty then
            val newWorld = world.generateSheep(nSheep).generateWolves(nWolves).generateGrass(nGrass)
            ecosystemManager.setWorld(newWorld) *> ZIO.succeed(newWorld)
          else ZIO.succeed(world)
          _ <- ZIO.foreachParDiscard(updatedWorld.sheep)(_.move(ecosystemManager))
          _ <- ZIO.foreachParDiscard(updatedWorld.wolves)(_.move(ecosystemManager))
          _ <- ecosystemManager.tick()
          _ <- ZIO.succeed:
              updateViewCallback()
        yield ()
        end <- Clock.nanoTime
        elapsed = zio.Duration.fromNanos(end - start)
        sleepDuration = zio.Duration.fromMillis(30).minus(elapsed).max(zio.Duration.Zero)
        _ <- ZIO.sleep(sleepDuration)
        _ <- if stopFlag.isSet then ZIO.unit else loop
      yield ()

    for
      _ <- ZIO.succeed(stopFlag.reset())
      f <- loop.forkDaemon
      _ = fiber = Some(f)
    yield ()

  def stopSimulation(): UIO[Unit] =
    for
      _ <- ZIO.succeed(stopFlag.set())
      _ <- fiber match
        case Some(f) => f.interrupt.unit
        case None    => ZIO.unit
      _ = fiber = None
    yield ()

  def resetSimulation(): UIO[Unit] =
    for
      world <- ecosystemManager.getWorld
      cleanWorld = world.deleteEntities()
      _ <- ecosystemManager.setWorld(cleanWorld)
    yield ()
