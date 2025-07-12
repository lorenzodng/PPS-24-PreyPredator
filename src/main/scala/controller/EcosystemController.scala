package controller

import model.World
import model.managers.EcosystemManager
import zio.*

class EcosystemController(val ecosystemManager: EcosystemManager, var stopFlag: Flag.type):

  private val SleepTime = 30
  private var fiber: Option[Fiber.Runtime[Throwable, Unit]] = None
  private var updateViewCallback: () => Unit = () => ()
  private var extinctionCallback: () => Unit = () => ()

  def setUpdateViewCallback(cb: () => Unit): Unit =
    updateViewCallback = cb

  def setExtinctionCallback(cb: () => Unit): Unit =
    extinctionCallback = cb

  def startSimulation(nWolves: Int, nSheep: Int, nGrass: Int, nGrassInterval: Int, nGrassGenerated: Int, widthSimulation: Int, heightSimulation: Int): UIO[Unit] =
    def loop: UIO[Unit] =
      for
        start <- Clock.nanoTime
        _ <- for
          world <- ecosystemManager.getWorld
          updatedWorld <- if world.entities.isEmpty
          then
            val newWorld = World(widthSimulation, heightSimulation, Seq.empty, Seq.empty, Seq.empty).generateSheep(nSheep).generateWolves(nWolves).generateGrass(nGrass)
            for
              _ <- ecosystemManager.setWorld(newWorld)
              _ <- ZIO.foreachDiscard(newWorld.sheep.map(_.id) ++ newWorld.wolves.map(_.id)): id =>
                val (dx, dy) = ecosystemManager.randomDirection()
                ecosystemManager.moveEntityDirection(id, dx, dy)
            yield newWorld
          else ZIO.succeed(world)
          _ <- ZIO.foreachParDiscard(updatedWorld.sheep)(_.move(ecosystemManager))
          _ <- ZIO.foreachParDiscard(updatedWorld.wolves)(_.move(ecosystemManager))
          extinct <- ecosystemManager.tick()
          _ <- if extinct then
            stopSimulation() *> ZIO.succeed(extinctionCallback())
          else
            ZIO.succeed(updateViewCallback())
        yield ()
        end <- Clock.nanoTime
        elapsed = zio.Duration.fromNanos(end - start)
        sleepDuration = zio.Duration.fromMillis(SleepTime).minus(elapsed).max(zio.Duration.Zero)
        _ <- ZIO.sleep(sleepDuration)
        _ <- if stopFlag.isSet then ZIO.unit else loop
      yield ()

    for
      _ <- ecosystemManager.setGrassInterval(nGrassInterval, SleepTime)
      _ <- ecosystemManager.setGrassGenerated(nGrassGenerated)
      _ <- ZIO.succeed(stopFlag.reset())
      f <- loop.forkDaemon
      _ = fiber = Some(f)
    yield ()

  def stopSimulation(): UIO[Unit] =
    ZIO.succeed(stopFlag.set())

  def resetSimulation(): UIO[Unit] =
    for
      _ <- fiber match
      case Some(f) => f.interrupt.unit
      case None    => ZIO.unit
      _ = fiber = None
      world <- ecosystemManager.getWorld
      cleanWorld = world.deleteEntities()
      _ <- ecosystemManager.setWorld(cleanWorld)
    yield ()

  def resizeWorld(width: Int, height: Int): UIO[Unit] =
    for
      oldWorld <- ecosystemManager.getWorld
      newWorld = oldWorld.copy(width = width, height = height)
      _ <- ecosystemManager.setWorld(newWorld)
    yield ()

  //per testing
  def isRunning: Boolean = fiber.isDefined