package model.managers

import model.entities.{EntityId, Grass, MovableEntity, Sheep, Wolf}
import model.*
import zio.*

/**
 * Manager responsible for handling the ecosystem's state, including movement,
 * reproduction, energy management, and grass generation.
 */
class EcosystemManager(refWorld: Ref[World]):

  /**
   * The amount of energy an animal loses each tick when moving.
   */
  private val energyLostForMovement = 0.2

  /**
   * The amount of energy an animal loses when reproducing.
   */
  private val energyLostForReproduction = 20

  /**
   * A map storing the movement direction for each entity.
   */
  private var directions: Map[EntityId.Type, (Double, Double)] = Map.empty

  /**
   * Counter tracking how many ticks have passed since the simulation started.
   */
  private var tickCounter: Int = 0

  /**
   * Number of grass units to generate at each grass generation cycle.
   */
  private var grassAmount: Int = _

  /**
   * Interval (in ticks) between automatic grass generation cycles.
   */
  private var grassFrequency: Int = _

  /**
   * Advances the ecosystem by one tick, updating positions, handling reproduction,
   * energy consumption, death, and grass growth.
   *
   * @return a ZIO effect representing whether all wolves and sheep are extinct
   */
  def tick(): UIO[Boolean] =
    for
      world <- refWorld.get
      updatedWorld <- updateEntitiesPositions(world)
      _ <- ZIO.succeed:
        tickCounter += 1
      finalWorld <- generateGrass(updatedWorld)
      isExtinct <- updateStateAfterTick(finalWorld)
    yield isExtinct

  /**
   * Updates all entity positions based on their current movement directions.
   *
   * @param initialWorld the current ecosystem state
   * @return a ZIO effect containing the new world state with updated positions
   */
  private def updateEntitiesPositions(initialWorld: World): UIO[World] =
    directions.toList.foldLeft(ZIO.succeed(initialWorld)): (accZIO, entry) =>
      val (id, (dx, dy)) = entry
      accZIO.flatMap(world => updateEntityPosition(world, id, dx, dy))

  /**
   * Updates the position of a single entity (wolf or sheep).
   *
   * @param world the current ecosystem state
   * @param id    the entity's ID
   * @param dx    horizontal movement delta
   * @param dy    vertical movement delta
   * @return a ZIO effect containing the updated world
   */
  private def updateEntityPosition(world: World, id: EntityId.Type, dx: Double, dy: Double): UIO[World] =
    world.wolfById(id) match
      case Some(wolf) =>
        val updatedWolf = updateWolfPosition(world, wolf, dx, dy)
        updateWorldAfterWolfMovement(world, updatedWolf)
      case _ => world.sheepById(id) match
        case Some(sheep) =>
          val updatedSheep = updateSheepPosition(world, sheep, dx, dy)
          updateWorldAfterSheepMovement(world, updatedSheep)
        case _ => ZIO.succeed(world)

  /**
   * Computes the new position and energy for a wolf after moving.
   *
   * @param world      the current ecosystem state
   * @param wolfEntity the wolf entity to move
   * @param dx         horizontal movement delta
   * @param dy         vertical movement delta
   * @return the updated wolf entity (pure value)
   */
  private def updateWolfPosition(world: World, wolfEntity: Wolf, dx: Double, dy: Double): Wolf =
    val newX = (wolfEntity.position.x + dx * wolfEntity.speed + world.width) % world.width
    val newY = (wolfEntity.position.y + dy * wolfEntity.speed + world.height) % world.height
    val newPosition = wolfEntity.position.copy(x = newX, y = newY)
    val newEnergy = wolfEntity.energy - energyLostForMovement
    wolfEntity.copy(position = newPosition, energy = newEnergy)

  /**
   * Computes the new position and energy for a sheep after moving.
   *
   * @param world       the current ecosystem state
   * @param sheepEntity the wolf entity to move
   * @param dx          horizontal movement delta
   * @param dy          vertical movement delta
   * @return the updated wolf entity (pure value)
   */
  private def updateSheepPosition(world: World, sheepEntity: Sheep, dx: Double, dy: Double): Sheep =
    val newX = (sheepEntity.position.x + dx * sheepEntity.speed + world.width) % world.width
    val newY = (sheepEntity.position.y + dy * sheepEntity.speed  + world.height) % world.height
    val newPosition = sheepEntity.position.copy(x = newX, y = newY)
    val newEnergy = sheepEntity.energy - energyLostForMovement
    sheepEntity.copy(position = newPosition, energy = newEnergy)

  /**
   * Updates the world after a wolf moves, handling eating, reproduction, and death.
   *
   * @param world      the current ecosystem state
   * @param wolfEntity the moved wolf entity
   * @return a ZIO effect containing the updated world state
   */
  private def updateWorldAfterWolfMovement(world: World, wolfEntity: Wolf): UIO[World] =
    val sheepEaten = world.sheep.filter(sheep => EatingManager.canEatSheep(wolfEntity, sheep))
    val wolfEatsSheep = sheepEaten.foldLeft(wolfEntity)((w, sheep) => w.eat)
    val afterEating = world.updateWolf(wolfEatsSheep).removeSheep(sheepEaten)
    val wolvesCanReproduce = afterEating.wolves.filter(wolf => LifeManager.canBornEntity(wolfEatsSheep, wolf))
    for
      afterReproduction <- handleWolfReproduction(afterEating, wolfEntity, wolvesCanReproduce)
      finalWorld <- ZIO.succeed(handleWolfDeath(afterReproduction, wolfEatsSheep))
    yield finalWorld

  /**
   * Updates the world after a sheep moves, handling eating, reproduction, and death.
   *
   * @param world       the current ecosystem state
   * @param sheepEntity the moved sheep entity
   * @return a ZIO effect containing the updated world state
   */
  private def updateWorldAfterSheepMovement(world: World, sheepEntity: Sheep): UIO[World] =
    val grassEaten = world.grass.filter(grass => EatingManager.canEatGrass(sheepEntity, grass))
    val sheepEatsGrass = grassEaten.foldLeft(sheepEntity)((s, grass) => s.eat)
    val afterEating = world.updateSheep(sheepEatsGrass).removeGrass(grassEaten)
    val sheepCanReproduce = afterEating.sheep.filter(sheep => LifeManager.canBornEntity(sheepEatsGrass, sheep))
    for
      afterReproduction <- handleSheepReproduction(afterEating, sheepEntity, sheepCanReproduce)
      finalWorld <- ZIO.succeed(handleSheepDeath(afterReproduction, sheepEatsGrass))
    yield finalWorld

  /**
   * Handles the reproduction of a wolf with potential partners.
   *
   * @param world              the current ecosystem state
   * @param wolfEntity         the moving wolf entity attempting to reproduce
   * @param wolvesCanReproduce the list of wolves eligible for reproduction with the given wolf
   * @return a ZIO effect containing the updated world with any new wolves added
   */
  private def handleWolfReproduction(world: World, wolfEntity: Wolf, wolvesCanReproduce: Seq[Wolf]): UIO[World] =
    ZIO.foldLeft(wolvesCanReproduce)(world): (acc, w) =>
      for
        newWolf <- ZIO.succeed(createNewWolf(wolfEntity, w))
        _ <- ZIO.succeed:
          directions = directions.updated(newWolf.id, randomDirection())
        parentsUpdated <- updateWolfParents(acc, wolfEntity, w)
        updatedWorld <- ZIO.succeed(parentsUpdated.copy(wolves = parentsUpdated.wolves :+ newWolf))
      yield updatedWorld

  /**
   * Handles the reproduction of a sheep with potential partners.
   *
   * @param world             the current ecosystem state
   * @param sheepEntity       the moving sheep entity attempting to reproduce
   * @param sheepCanReproduce the list of sheep eligible for reproduction with the given sheep
   * @return a ZIO effect containing the updated world with any new sheep added
   */
  private def handleSheepReproduction(world: World, sheepEntity: Sheep, sheepCanReproduce: Seq[Sheep]): UIO[World] =
    ZIO.foldLeft(sheepCanReproduce)(world): (acc, s) =>
      for
        newSheep <- ZIO.succeed(createNewSheep(sheepEntity, s))
        _ <- ZIO.succeed:
          directions = directions.updated(newSheep.id, randomDirection())
        parentsUpdated <- updateSheepParents(acc, sheepEntity, s)
        updatedWorld <- ZIO.succeed(parentsUpdated.copy(sheep = parentsUpdated.sheep :+ newSheep))
      yield updatedWorld

  /**
   * Applies energy cost for reproduction to two wolf parents.
   *
   * @param world   the current ecosystem state
   * @param parent1 the first parent wolf
   * @param parent2 the second parent wolf
   * @return a ZIO effect containing the updated world after modifying both parents
   */
  private def updateWolfParents(world: World, parent1: Wolf, parent2: Wolf): UIO[World] =
    for
      sepPair <- separateEntities(parent1.copy(energy = parent1.energy - energyLostForReproduction), parent2.copy(energy = parent2.energy - energyLostForReproduction))
      (sep1, sep2) = sepPair
    yield world.updateWolf(sep1).updateWolf(sep2)

  /**
   * Applies energy cost for reproduction to two sheep parents.
   *
   * @param world   the current ecosystem state
   * @param parent1 the first parent sheep
   * @param parent2 the second parent sheep
   * @return a ZIO effect containing the updated world after modifying both parents
   */
  private def updateSheepParents(world: World, parent1: Sheep, parent2: Sheep): UIO[World] =
    for
      sepPair <- separateEntities(parent1.copy(energy = parent1.energy - energyLostForReproduction), parent2.copy(energy = parent2.energy - energyLostForReproduction))
      (sep1, sep2) = sepPair
    yield world.updateSheep(sep1).updateSheep(sep2)

  /**
   * Handles the death of a wolf.
   *
   * @param world the current ecosystem state
   * @param wolf  the wolf to check for death
   * @return the updated world with the wolf removed if it has died
   */
  private def handleWolfDeath(world: World, wolf: Wolf): World =
    if LifeManager.canDieEntity(wolf) then
      world.copy(wolves = world.wolves.filterNot(_.id == wolf.id))
    else world

  /**
   * Handles the death of a sheep.
   *
   * @param world the current ecosystem state
   * @param sheep the sheep to check for death
   * @return the updated world with the sheep removed if it has died
   */
  private def handleSheepDeath(world: World, sheep: Sheep): World =
    if LifeManager.canDieEntity(sheep) then
      world.copy(sheep = world.sheep.filterNot(_.id == sheep.id))
    else world

  // Utility methods

  /**
   * Creates a new wolf entity positioned between two parent wolves.
   *
   * @param parent1 the first parent wolf
   * @param parent2 the second parent wolf
   * @return a new wolf instance positioned at the midpoint of the parents
   */
  private def createNewWolf(parent1: Wolf, parent2: Wolf): Wolf =
    val newId = EntityId.random
    val newPosition = Position((parent1.position.x + parent2.position.x) / 2, (parent1.position.y + parent2.position.y) / 2)
    Wolf(newId, newPosition)

  /**
   * Creates a new sheep entity positioned between two parent sheep.
   *
   * @param parent1 the first parent sheep
   * @param parent2 the second parent sheep
   * @return a new sheep instance positioned at the midpoint of the parents
   */
  private def createNewSheep(parent1: Sheep, parent2: Sheep): Sheep =
    val newId = EntityId.random
    val newPosition = Position((parent1.position.x + parent2.position.x) / 2, (parent1.position.y + parent2.position.y) / 2)
    Sheep(newId, newPosition)

  /**
   * Generates new grass entities periodically based on the tick counter.
   *
   * @param updatedWorld the current ecosystem state
   * @return a ZIO effect containing the updated world with newly added grass
   */
  private def generateGrass(updatedWorld: World): UIO[World] =
    if tickCounter >= grassFrequency then
      for
        _ <- ZIO.succeed:
          tickCounter = 0
        newGrass = Grass.generateRandomGrass(grassAmount, updatedWorld.width, updatedWorld.height)
      yield updatedWorld.addGrass(newGrass)
    else ZIO.succeed(updatedWorld)

  /**
   * Separates two entities by a specified distance to avoid overlap.
   *
   * @param e1                 the first entity
   * @param e2                 the second entity
   * @param separationDistance the distance to separate the entities
   * @return a ZIO effect containing a tuple of the two entities with updated positions
   */
  private def separateEntities[T <: MovableEntity[T]](e1: T, e2: T, separationDistance: Double = 20.0): UIO[(T, T)] =
    for
      world <- refWorld.get
    yield
      val (newPos1, newPos2) = calculateSeparationPositions(e1.position, e2.position, separationDistance, world.width, world.height)
      (e1.newPosition(newPos1), e2.newPosition(newPos2))

  /**
   * Calculates new positions for two entities separated by a given distance within world bounds.
   *
   * @param pos1               the position of the first entity
   * @param pos2               the position of the second entity
   * @param separationDistance the target separation distance
   * @param maxWidth           the maximum width of the world
   * @param maxHeight          the maximum height of the world
   * @return a tuple of new positions for the two entities
   */
  private def calculateSeparationPositions(pos1: Position, pos2: Position, separationDistance: Double, maxWidth: Double, maxHeight: Double): (Position, Position) =
    val dx = pos1.x - pos2.x
    val dy = pos1.y - pos2.y
    val distance = math.sqrt(dx * dx + dy * dy)
    val separationVector = if (distance == 0) (1.0, 0.0) else (dx / distance, dy / distance)
    val newPos1 = Position((pos1.x + separationVector._1 * separationDistance).max(0).min(maxWidth), (pos1.y + separationVector._2 * separationDistance).max(0).min(maxHeight))
    val newPos2 = Position((pos2.x - separationVector._1 * separationDistance).max(0).min(maxWidth), (pos2.y - separationVector._2 * separationDistance).max(0).min(maxHeight))
    (newPos1, newPos2)

  /**
   * Generates a random direction vector.
   *
   * @return a tuple (dx, dy) representing a random direction vector
   */
  def randomDirection(): (Double, Double) =
    val angle = Math.random() * 2 * Math.PI
    (Math.cos(angle), Math.sin(angle))

  /**
   * Updates the ecosystem state after each tick, checking for extinction.
   *
   * @param finalWorld the world state after all updates in the current tick
   * @return a UIO containing true if all wolves and sheep are extinct, false otherwise
   */
  private def updateStateAfterTick(finalWorld: World): UIO[Boolean] =
    for
      _ <- refWorld.set(finalWorld)
      _ <- ZIO.succeed:
        directions = directions.filter(entry => finalWorld.wolfById(entry._1).isDefined || finalWorld.sheepById(entry._1).isDefined)
      isExtinct <- ZIO.succeed(finalWorld.wolves.isEmpty && finalWorld.sheep.isEmpty)
    yield isExtinct

  /**
   * Sets the movement direction for an entity.
   *
   * @param id the entity ID
   * @param dx horizontal direction component
   * @param dy vertical direction component
   * @return a ZIO effect representing the direction update
   */
  def moveEntityDirection(id: EntityId.Type, dx: Double, dy: Double): UIO[Unit] =
    ZIO.succeed:
      directions = directions.updated(id, (dx, dy))

  /**
   * Gets the current world state.
   *
   * @return a ZIO effect containing the current world
   */
  def getWorld: UIO[World] =
    refWorld.get

  /**
   * Returns the current movement directions of all entities.
   *
   * @return a map where keys are entity IDs and values are tuples representing movement directions (dx, dy)
   */
  def getDirections: Map[EntityId.Type, (Double, Double)] =
      directions

  /**
   * Replaces the current world state.
   *
   * @param newWorld the new world state
   * @return a ZIO effect representing the state update
   */
  def setWorld(newWorld: World): UIO[Unit] =
    refWorld.set(newWorld)

  /**
   * Sets the interval at which new grass is generated.
   *
   * @param grassInterval the total interval for grass growth in ticks
   * @param sleepTime     the duration of a single tick
   * @return a ZIO effect representing the completion of setting the interval
   */
  def setGrassInterval(grassInterval: Int, sleepTime: Int): UIO[Unit] =
    ZIO.succeed:
      grassFrequency = grassInterval / sleepTime

  /**
   * Sets the amount of grass to be generated each time grass growth occurs.
   *
   * @param grassGenerated the number of grass entities to generate
   * @return a ZIO effect representing the completion of setting the grass amount
   */
  def setGrassGenerated(grassGenerated: Int): UIO[Unit] =
    ZIO.succeed:
      grassAmount = grassGenerated
