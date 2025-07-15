package model.managers

import model.entities.{EntityId, Grass, Sheep, Wolf}
import model.*
import zio.*

trait MovableEntity[T]:
  def position: Position
  def newPosition(position: Position): T

class EcosystemManager(refWorld: Ref[World]):

  private val energyLostForMovement = 0.2
  private val energyLostForReproduction = 20
  private var directions: Map[EntityId.Type, (Double, Double)] = Map.empty
  private var tickCounter: Int = 0
  private var grassAmount: Int = _
  private var grassFrequency: Int = _

  def tick(): UIO[Boolean] =
    for 
      world <- refWorld.get
      updatedWorld <- updateEntitiesPositions(world)
      _ <- ZIO.succeed:
        tickCounter += 1
      finalWorld <- generateGrass(updatedWorld)
      isExtinct <- updateStateAfterTick(finalWorld)
    yield isExtinct

  private def updateEntitiesPositions(initialWorld: World): UIO[World] =
    directions.toList.foldLeft(ZIO.succeed(initialWorld)): (accZIO, entry) =>
      val (id, (dx, dy)) = entry
      accZIO.flatMap(world => updateEntityPosition(world, id, dx, dy))

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

  private def updateWolfPosition(world: World, wolfEntity: Wolf, dx: Double, dy: Double): Wolf =
    val newX = (wolfEntity.position.x + dx * wolfEntity.speed + world.width) % world.width
    val newY = (wolfEntity.position.y + dy * wolfEntity.speed + world.height) % world.height
    val newPosition = wolfEntity.position.copy(x = newX, y = newY)
    val newEnergy = wolfEntity.energy - energyLostForMovement
    wolfEntity.copy(position = newPosition, energy = newEnergy)

  private def updateSheepPosition(world: World, sheepEntity: Sheep, dx: Double, dy: Double): Sheep =
    val newX = (sheepEntity.position.x + dx * sheepEntity.speed + world.width) % world.width
    val newY = (sheepEntity.position.y + dy * sheepEntity.speed  + world.height) % world.height
    val newPosition = sheepEntity.position.copy(x = newX, y = newY)
    val newEnergy = sheepEntity.energy - energyLostForMovement
    sheepEntity.copy(position = newPosition, energy = newEnergy)

  private def updateWorldAfterWolfMovement(world: World, wolfEntity: Wolf): UIO[World] =
    val sheepEaten = world.sheep.filter(sheep => EatingManager.canEatSheep(wolfEntity, sheep))
    val wolfEatsSheep = sheepEaten.foldLeft(wolfEntity)((w, sheep) => w.eat)
    val afterEating = world.updateWolf(wolfEatsSheep).removeSheep(sheepEaten)
    val wolvesCanReproduce = afterEating.wolves.filter(wolf => LifeManager.canBornEntity(wolfEatsSheep, wolf))
    for
      afterReproduction <- handleWolfReproduction(afterEating, wolfEntity, wolvesCanReproduce)
      finalWorld <- ZIO.succeed(handleWolfDeath(afterReproduction, wolfEatsSheep))
    yield finalWorld

  private def handleWolfReproduction(world: World, wolfEntity: Wolf, wolvesCanReproduce: Seq[Wolf]): UIO[World] =
    ZIO.foldLeft(wolvesCanReproduce)(world): (acc, w) =>
      for
        newWolf <- ZIO.succeed(createNewWolf(wolfEntity, w))
        _ <- ZIO.succeed:
          directions = directions.updated(newWolf.id, randomDirection())
        parentsUpdated <- updateWolfParents(acc, wolfEntity, w)
        updatedWorld <- ZIO.succeed(parentsUpdated.copy(wolves = parentsUpdated.wolves :+ newWolf))
      yield updatedWorld

  private def updateWolfParents(world: World, parent1: Wolf, parent2: Wolf): UIO[World] =
    for
      sepPair <- separateEntities(parent1.copy(energy = parent1.energy - energyLostForReproduction), parent2.copy(energy = parent2.energy - energyLostForReproduction))
      (sep1, sep2) = sepPair
    yield world.updateWolf(sep1).updateWolf(sep2)

  private def handleWolfDeath(world: World, wolf: Wolf): World =
    if LifeManager.canDieEntity(wolf) then
      world.copy(wolves = world.wolves.filterNot(_.id == wolf.id))
    else world

  private def updateWorldAfterSheepMovement(world: World, sheepEntity: Sheep): UIO[World] =
    val grassEaten = world.grass.filter(grass => EatingManager.canEatGrass(sheepEntity, grass))
    val sheepEatsGrass = grassEaten.foldLeft(sheepEntity)((s, grass) => s.eat)
    val afterEating = world.updateSheep(sheepEatsGrass).removeGrass(grassEaten)
    val sheepCanReproduce = afterEating.sheep.filter(sheep => LifeManager.canBornEntity(sheepEatsGrass, sheep))
    for
      afterReproduction <- handleSheepReproduction(afterEating, sheepEntity, sheepCanReproduce)
      finalWorld <- ZIO.succeed(handleSheepDeath(afterReproduction, sheepEatsGrass))
    yield finalWorld

  private def handleSheepReproduction(world: World, sheepEntity: Sheep, sheepCanReproduce: Seq[Sheep]): UIO[World] =
    ZIO.foldLeft(sheepCanReproduce)(world): (acc, s) =>
      for
        newSheep <- ZIO.succeed(createNewSheep(sheepEntity, s))
        _ <- ZIO.succeed:
          directions = directions.updated(newSheep.id, randomDirection())
        parentsUpdated <- updateSheepParents(acc, sheepEntity, s)
        updatedWorld <- ZIO.succeed(parentsUpdated.copy(sheep = parentsUpdated.sheep :+ newSheep))
      yield updatedWorld

  private def updateSheepParents(world: World, parent1: Sheep, parent2: Sheep): UIO[World] =
    for
      sepPair <- separateEntities(parent1.copy(energy = parent1.energy - energyLostForReproduction), parent2.copy(energy = parent2.energy - energyLostForReproduction))
      (sep1, sep2) = sepPair
    yield world.updateSheep(sep1).updateSheep(sep2)

  private def handleSheepDeath(world: World, sheep: Sheep): World =
    if LifeManager.canDieEntity(sheep) then
      world.copy(sheep = world.sheep.filterNot(_.id == sheep.id))
    else world
  
  private def createNewWolf(parent1: Wolf, parent2: Wolf): Wolf =
    val newId = EntityId.random
    val newPosition = Position((parent1.position.x + parent2.position.x) / 2, (parent1.position.y + parent2.position.y) / 2)
    Wolf(newId, newPosition)

  private def createNewSheep(parent1: Sheep, parent2: Sheep): Sheep =
    val newId = EntityId.random
    val newPosition = Position((parent1.position.x + parent2.position.x) / 2, (parent1.position.y + parent2.position.y) / 2)
    Sheep(newId, newPosition)

  private def separateEntities[T <: MovableEntity[T]](e1: T, e2: T, separationDistance: Double = 20.0): UIO[(T, T)] =
    for
      world <- refWorld.get
    yield
      val (newPos1, newPos2) = calculateSeparationPositions(e1.position, e2.position, separationDistance, world.width, world.height)
      (e1.newPosition(newPos1), e2.newPosition(newPos2)) // non posso fare direttamente e1.copy(position = newPos) perchè copy ha bisogno di conoscere il tipo di entità e1.

  private def calculateSeparationPositions(pos1: Position, pos2: Position, separationDistance: Double, maxWidth: Double, maxHeight: Double): (Position, Position) =
    val dx = pos1.x - pos2.x
    val dy = pos1.y - pos2.y
    val distance = math.sqrt(dx * dx + dy * dy)
    val separationVector = if (distance == 0) (1.0, 0.0) else (dx / distance, dy / distance)
    val newPos1 = Position((pos1.x + separationVector._1 * separationDistance).max(0).min(maxWidth), (pos1.y + separationVector._2 * separationDistance).max(0).min(maxHeight))
    val newPos2 = Position((pos2.x - separationVector._1 * separationDistance).max(0).min(maxWidth), (pos2.y - separationVector._2 * separationDistance).max(0).min(maxHeight))
    (newPos1, newPos2)

  def randomDirection(): (Double, Double) =
    val angle = Math.random() * 2 * Math.PI
    (Math.cos(angle), Math.sin(angle))

  private def generateGrass(updatedWorld: World): UIO[World] =
    if tickCounter >= grassFrequency then
      for
        _ <- ZIO.succeed:
          tickCounter = 0
        newGrass = Grass.generateRandomGrass(grassAmount, updatedWorld.width, updatedWorld.height)
      yield updatedWorld.addGrass(newGrass)
    else ZIO.succeed(updatedWorld)

  private def updateStateAfterTick(finalWorld: World): UIO[Boolean] =
    for
      _ <- refWorld.set(finalWorld)
      _ <- ZIO.succeed:
        directions = directions.filter(entry => finalWorld.wolfById(entry._1).isDefined || finalWorld.sheepById(entry._1).isDefined)
      isExtinct <- ZIO.succeed(finalWorld.wolves.isEmpty && finalWorld.sheep.isEmpty)
    yield isExtinct
  
  def moveEntityDirection(id: EntityId.Type, dx: Double, dy: Double): UIO[Unit] =
    ZIO.succeed:
      directions = directions.updated(id, (dx, dy))
    
  def getWorld: UIO[World] =
    refWorld.get

  def setWorld(newWorld: World): UIO[Unit] =
    refWorld.set(newWorld)

  def setGrassInterval(grassInterval: Int, sleepTime: Int): UIO[Unit] =
    ZIO.succeed:
      grassFrequency = grassInterval / sleepTime

  def setGrassGenerated(grassGenerated: Int): UIO[Unit] =
    ZIO.succeed:
      grassAmount = grassGenerated

  //per testing
  def getDirections: Map[EntityId.Type, (Double, Double)] =
      directions