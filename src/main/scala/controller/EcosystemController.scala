package controller

import model.World
import model.managers.EcosystemManager
import zio.*

/**
 * Controller class that manages the lifecycle and logic of the ecosystem simulation.
 * It interacts with the EcosystemManager and controls the simulation flow.
 */
class EcosystemController(val ecosystemManager: EcosystemManager, var stopFlag: Flag.type):

  /**
   * Time to sleep (in milliseconds) between each simulation step.
   * Controls the speed of the simulation loop.
   */
  private val sleepTime = 30

  /**
   * Optional reference to the fiber running the simulation loop.
   * Used to start, monitor, or interrupt the simulation asynchronously.
   */
  private var fiber: Option[Fiber.Runtime[Throwable, Unit]] = None

  /**
   * Callback function invoked to update the view/UI after each simulation step.
   */
  private var updateViewCallback: () => Unit = () => ()

  /**
   * Callback function invoked when an extinction event occurs (i.e., no wolves or sheep remain in the simulation).
   */
  private var extinctionCallback: () => Unit = () => ()

  /**
   * Sets the callback function to update the view/UI.
   *
   * @param cb callback function to be invoked after each simulation step
   */
  def setUpdateViewCallback(cb: () => Unit): Unit =
    updateViewCallback = cb

  /**
   * Sets the callback function to be called on extinction.
   *
   * @param cb callback function to be invoked when all wolves and sheep are extinct
   */
  def setExtinctionCallback(cb: () => Unit): Unit =
    extinctionCallback = cb

  /**
   * Starts the simulation with the specified parameters.
   * Initializes grass growth intervals and entity counts, then starts the loop asynchronously (on a fiber).
   *
   * @param nWolves          number of wolves to generate
   * @param nSheep           number of sheep to generate
   * @param nGrass           number of grass patches to generate initially
   * @param nGrassInterval   interval for grass regrowth
   * @param nGrassGenerated  amount of grass generated each interval
   * @param widthSimulation  width of the simulation world
   * @param heightSimulation height of the simulation world
   * @return a ZIO effect representing the asynchronous start operation
   */
  def startSimulation(nWolves: Int, nSheep: Int, nGrass: Int, nGrassInterval: Int, nGrassGenerated: Int, widthSimulation: Int, heightSimulation: Int): UIO[Unit] =
    for
      _ <- ecosystemManager.setGrassInterval(nGrassInterval, sleepTime)
      _ <- ecosystemManager.setGrassGenerated(nGrassGenerated)
      _ <- ZIO.succeed(stopFlag.reset())
      f <- loop(widthSimulation, heightSimulation, nWolves, nSheep, nGrass).forkDaemon
      _ <- ZIO.succeed:
        fiber = Some(f)
    yield ()

  /**
   * Stops the simulation by setting the stop flag.
   *
   * @return a ZIO effect representing the stop operation
   */
  def stopSimulation(): UIO[Unit] =
    ZIO.succeed(stopFlag.set())

  /**
   * Resets the simulation by interrupting the running fiber and clearing the world entities.
   *
   * @return a ZIO effect representing the reset operation
   */
  def resetSimulation(): UIO[Unit] =
    for
      _ <- interruptSimulationFiber()
      _ <- resetWorld()
    yield ()

  /**
   * Main simulation loop that repeatedly performs simulation steps until stopped.
   *
   * @param width   simulation world width
   * @param height  simulation world height
   * @param nSheep  initial number of sheep
   * @param nWolves initial number of wolves
   * @param nGrass  initial number of grass patches
   * @return a ZIO effect representing the ongoing simulation loop
   */
  private def loop(width: Int, height: Int, nWolves: Int, nSheep: Int, nGrass: Int): UIO[Unit] =
    for
      start <- Clock.nanoTime
      updatedWorld <- prepareWorld(width, height, nWolves, nSheep, nGrass)
      _ <- simulateStep(updatedWorld)
      _ <- waitNextFrame(start)
      _ <- if stopFlag.isSet then ZIO.unit
      else loop(width, height, nWolves, nSheep, nGrass)
    yield ()

  /**
   * Prepares the simulation world, generating entities if none exist.
   *
   * @param width   simulation world width
   * @param height  simulation world height
   * @param nSheep  number of sheep to generate if world is empty
   * @param nWolves number of wolves to generate if world is empty
   * @param nGrass  number of grass patches to generate if world is empty
   * @return a ZIO effect containing the updated world instance
   */
  private def prepareWorld(width: Int, height: Int, nWolves: Int, nSheep: Int, nGrass: Int): UIO[World] =
    for
      world <- ecosystemManager.getWorld
      updatedWorld <- if world.entities.isEmpty then
        val newWorld = generateEntities(width, height, nWolves, nSheep, nGrass)
        moveEntitiesRandomly(newWorld)
      else ZIO.succeed(world)
    yield updatedWorld

  /**
   * Generates a new world with the specified numbers of wolves, sheep, and grass.
   *
   * @param width   world width
   * @param height  world height
   * @param nWolves number of wolves to generate
   * @param nSheep  number of sheep to generate
   * @param nGrass  number of grass patches to generate
   * @return a new World instance populated with entities
   */
  private def generateEntities(width: Int, height: Int, nWolves: Int, nSheep: Int, nGrass: Int): World =
    World(width, height, Seq.empty, Seq.empty, Seq.empty)
      .generateWolves(nWolves)
      .generateSheep(nSheep)
      .generateGrass(nGrass)

  /**
   * Moves all entities randomly in the new world.
   *
   * @param newWorld the world with generated entities
   * @return a ZIO effect containing the updated world after moving entities
   */
  private def moveEntitiesRandomly(newWorld: World): UIO[World] =
    for
      _ <- ecosystemManager.setWorld(newWorld)
      _ <- ZIO.foreachDiscard(newWorld.sheep.map(_.id) ++ newWorld.wolves.map(_.id)) { id =>
        val (dx, dy) = ecosystemManager.randomDirection()
        ecosystemManager.moveEntityDirection(id, dx, dy)
      }
    yield newWorld

  /**
   * Performs one simulation step by moving all animals in the ecosystem.
   * Calls appropriate callbacks based on extinction or update.
   *
   * @param world current world state
   * @return a ZIO effect representing the simulation step
   */
  private def simulateStep(world: World): UIO[Unit] =
    for
      _ <- ZIO.foreachParDiscard(world.sheep)(_.move(ecosystemManager))
      _ <- ZIO.foreachParDiscard(world.wolves)(_.move(ecosystemManager))
      extinct <- ecosystemManager.simulateStep()
      _ <- if extinct then
        stopSimulation() *> ZIO.succeed(extinctionCallback())
      else ZIO.succeed(updateViewCallback())
    yield ()

  /**
   * Waits the remaining time to maintain a fixed frame rate.
   *
   * @param start timestamp when the step started
   * @return a ZIO effect representing the sleep period
   */
  private def waitNextFrame(start: Long): UIO[Unit] =
    for
      end <- Clock.nanoTime
      elapsed = zio.Duration.fromNanos(end - start)
      sleepDuration = zio.Duration.fromMillis(sleepTime).minus(elapsed).max(zio.Duration.Zero)
      _ <- ZIO.sleep(sleepDuration)
    yield ()

  /**
   * Interrupts the fiber running the simulation loop if it exists.
   *
   * @return a ZIO effect representing the interruption operation
   */
  private def interruptSimulationFiber(): UIO[Unit] =
    for
      _ <- fiber match
        case Some(f) => f.interrupt.unit
        case None    => ZIO.unit
      _ <- ZIO.succeed:
        fiber = None
    yield ()

  /**
   * Resets the world by deleting all entities.
   *
   * @return a ZIO effect representing the reset operation
   */
  private def resetWorld(): UIO[Unit] =
    for
      world <- ecosystemManager.getWorld
      _ <- ecosystemManager.setWorld(world.deleteEntities())
    yield ()

  /**
   * Resizes the simulation world to the specified dimensions.
   *
   * @param width  new width of the world
   * @param height new height of the world
   * @return a ZIO effect representing the resize operation
   */
  def resizeWorld(width: Int, height: Int): UIO[Unit] =
    for
      oldWorld <- ecosystemManager.getWorld
      newWorld <- ZIO.succeed(oldWorld.copy(width = width, height = height))
      _ <- ecosystemManager.setWorld(newWorld)
    yield ()

  /**
   * Checks if the simulation is currently running.
   *
   * @return true if simulation fiber is active, false otherwise
   */
  def isRunning: Boolean = fiber.isDefined
