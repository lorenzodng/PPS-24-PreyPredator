package controller

import model.World
import model.managers.EcosystemManager
import zio.*

class EcosystemController(val ecosystemManager: EcosystemManager, var stopFlag: Flag.type):

  private val sleepTime = 30
  private var fiber: Option[Fiber.Runtime[Throwable, Unit]] = None
  private var updateViewCallback: () => Unit = () => ()
  private var extinctionCallback: () => Unit = () => ()

  def setUpdateViewCallback(cb: () => Unit): Unit =
    updateViewCallback = cb

  def setExtinctionCallback(cb: () => Unit): Unit =
    extinctionCallback = cb

  def startSimulation(nWolves: Int, nSheep: Int, nGrass: Int, nGrassInterval: Int, nGrassGenerated: Int, widthSimulation: Int, heightSimulation: Int): UIO[Unit] =
    for
      _ <- ecosystemManager.setGrassInterval(nGrassInterval, sleepTime)
      _ <- ecosystemManager.setGrassGenerated(nGrassGenerated)
      _ <- ZIO.succeed(stopFlag.reset())
      f <- loop(widthSimulation, heightSimulation, nWolves, nSheep, nGrass).forkDaemon
      _ <- ZIO.succeed:
        fiber = Some(f)
    yield ()

  def stopSimulation(): UIO[Unit] =
    ZIO.succeed(stopFlag.set())

  def resetSimulation(): UIO[Unit] =
    for
      _ <- interruptSimulationFiber()
      _ <- resetWorld()
    yield ()

  private def loop(width: Int, height: Int, nSheep: Int, nWolves: Int, nGrass: Int): UIO[Unit] =
    for
      start <- Clock.nanoTime
      updatedWorld <- prepareWorld(width, height, nSheep, nWolves, nGrass)
      _ <- simulateStep(updatedWorld)
      _ <- waitNextFrame(start)
      _ <- if stopFlag.isSet then ZIO.unit
      else loop(width, height, nSheep, nWolves, nGrass)
    yield ()

  private def prepareWorld(width: Int, height: Int, nSheep: Int, nWolves: Int, nGrass: Int): UIO[World] =
    for
      world <- ecosystemManager.getWorld
      updatedWorld <- if world.entities.isEmpty then
        val newWorld = generateEntities(width, height, nSheep, nWolves, nGrass)
        moveEntitiesRandomly(newWorld)
      else ZIO.succeed(world)
    yield updatedWorld

  private def generateEntities(width: Int, height: Int, nWolves: Int, nSheep: Int, nGrass: Int): World =
    World(width, height, Seq.empty, Seq.empty, Seq.empty).generateWolves(nWolves).generateSheep(nSheep).generateGrass(nGrass)

  private def moveEntitiesRandomly(newWorld: World): UIO[World] =
    for
      _ <- ecosystemManager.setWorld(newWorld)
      _ <- ZIO.foreachDiscard(newWorld.sheep.map(_.id) ++ newWorld.wolves.map(_.id)): id =>
        val (dx, dy) = ecosystemManager.randomDirection()
        ecosystemManager.moveEntityDirection(id, dx, dy)
    yield newWorld

  private def simulateStep(world: World): UIO[Unit] =
    for
      _ <- ZIO.foreachParDiscard(world.sheep)(_.move(ecosystemManager))
      _ <- ZIO.foreachParDiscard(world.wolves)(_.move(ecosystemManager))
      extinct <- ecosystemManager.tick()
      _ <- if extinct then
        stopSimulation() *> ZIO.succeed(extinctionCallback())
      else ZIO.succeed(updateViewCallback())
    yield ()

  private def waitNextFrame(start: Long): UIO[Unit] =
    for
      end <- Clock.nanoTime
      elapsed = zio.Duration.fromNanos(end - start)
      sleepDuration = zio.Duration.fromMillis(sleepTime).minus(elapsed).max(zio.Duration.Zero)
      _ <- ZIO.sleep(sleepDuration)
    yield ()

  private def interruptSimulationFiber(): UIO[Unit] =
    for
      _ <- fiber match
        case Some(f) => f.interrupt.unit
        case None => ZIO.unit
      _ <- ZIO.succeed:
        fiber = None
    yield ()

  private def resetWorld(): UIO[Unit] =
    for
      world <- ecosystemManager.getWorld
      _ <- ecosystemManager.setWorld(world.deleteEntities())
    yield ()

  def resizeWorld(width: Int, height: Int): UIO[Unit] =
    for
      oldWorld <- ecosystemManager.getWorld
      newWorld <- ZIO.succeed(oldWorld.copy(width = width, height = height))
      _ <- ecosystemManager.setWorld(newWorld)
    yield ()

  //per testing
  def isRunning: Boolean = fiber.isDefined