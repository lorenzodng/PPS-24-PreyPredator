package model.managers

import model.entities.{EntityId, Grass, Sheep, Wolf}
import model.*
import zio.*

trait MovableEntity[T]:
  def position: Position
  def newPosition(position: Position): T

class EcosystemManager(refWorld: Ref[World]):

  private var directions: Map[EntityId.Type, (Double, Double)] = Map.empty
  private var tickCounter: Int = 0
  private val EnergyLostForMovement = 0.2
  private val EnergyLostForReproduction = 20
  private val GrassFrequency = 100
  private val GrassAmount = 100

  def tick(): UIO[Unit] = 
    for 
      world <- refWorld.get
      updatedWorld <- directions.toList.foldLeft(ZIO.succeed(world)): (accZIO, entry) =>
        accZIO.flatMap: accWorld =>
          val (id, (dx, dy)) = entry
          accWorld.wolfById(id) match
            case Some(wolf) =>
              val updatedWolf = updateWolfPosition(accWorld, wolf, dx, dy)
              updateWorldAfterWolfMovement(accWorld, updatedWolf)
            case None =>
              accWorld.sheepById(id) match
                case Some(sheep) =>
                  val updatedSheep = updateSheepPosition(accWorld, sheep, dx, dy)
                  updateWorldAfterSheepMovement(accWorld, updatedSheep)
                case None => ZIO.succeed(accWorld)
                
      _ <- ZIO.succeed: 
        tickCounter += 1
      finalWorld <- if tickCounter >= GrassFrequency then
        for
          _ <- ZIO.succeed:
            tickCounter = 0
          newGrass = Grass.generateRandomGrass(GrassAmount, updatedWorld.width, updatedWorld.height)
        yield updatedWorld.addGrass(newGrass)
      else ZIO.succeed(updatedWorld)
      _ <- refWorld.set(finalWorld)
      _ <- ZIO.succeed:
        directions = directions.filter(entry => finalWorld.wolfById(entry._1).isDefined || finalWorld.sheepById(entry._1).isDefined)
    yield ()

  private def updateWolfPosition(world: World, wolfEntity: Wolf, dx: Double, dy: Double): Wolf =
    val newX = (wolfEntity.position.x + dx * wolfEntity.speed + world.width) % world.width
    val newY = (wolfEntity.position.y + dy * wolfEntity.speed + world.height) % world.height
    val newPosition = wolfEntity.position.copy(x = newX, y = newY)
    val newEnergy = wolfEntity.energy - EnergyLostForMovement
    wolfEntity.copy(position = newPosition, energy = newEnergy)

  private def updateSheepPosition(world: World, sheepEntity: Sheep, dx: Double, dy: Double): Sheep =
    val newX = (sheepEntity.position.x + dx * sheepEntity.speed + world.width) % world.width
    val newY = (sheepEntity.position.y + dy * sheepEntity.speed  + world.height) % world.height
    val newPosition = sheepEntity.position.copy(x = newX, y = newY)
    val newEnergy = sheepEntity.energy - EnergyLostForMovement
    sheepEntity.copy(position = newPosition, energy = newEnergy)

  private def updateWorldAfterWolfMovement(world: World, wolfEntity: Wolf): UIO[World] =
    val sheepEaten = world.sheep.filter(sheep => EatingManager.canEatSheep(wolfEntity, sheep))
    val wolfEatsSheep = sheepEaten.foldLeft(wolfEntity)((w, sheep) => w.eat)
    val afterEating = world.updateWolf(wolfEatsSheep).removeSheep(sheepEaten)
    val wolvesCanReproduce = afterEating.wolves.filter(wolf => LifeManager.canBornEntity(wolfEntity, wolf))
    ZIO.foldLeft(wolvesCanReproduce)(afterEating): (acc, w) =>
      val newWolf = createNewWolf(wolfEntity, w)
      val parent1 = wolfEntity.copy(energy = wolfEntity.energy - EnergyLostForReproduction)
      val parent2 = w.copy(energy = w.energy - EnergyLostForReproduction)
      for
        _ <- ZIO.succeed:
          directions = directions.updated(newWolf.id, randomDirection())
        sepPair <- separateEntities(parent1, parent2)
        (sep1, sep2) = sepPair
        separatedUpdated = acc.updateWolf(sep1).updateWolf(sep2)
      yield separatedUpdated.copy(wolves = separatedUpdated.wolves :+ newWolf)
    .map: afterReproduction =>
      val updatedWolves =
        if LifeManager.canDieEntity(wolfEatsSheep) then
          afterReproduction.wolves.filterNot(_.id == wolfEatsSheep.id)
        else afterReproduction.wolves
      afterReproduction.copy(wolves = updatedWolves)

  private def updateWorldAfterSheepMovement(world: World, sheepEntity: Sheep): UIO[World] =
    val grassEaten = world.grass.filter(grass => EatingManager.canEatGrass(sheepEntity, grass))
    val sheepEatsGrass = grassEaten.foldLeft(sheepEntity)((s, grass) => s.eat)
    val afterEating = world.updateSheep(sheepEatsGrass).removeGrass(grassEaten)
    val sheepCanReproduce = afterEating.sheep.filter(sheep => LifeManager.canBornEntity(sheepEntity, sheep))
    ZIO.foldLeft(sheepCanReproduce)(afterEating): (acc, s) =>
      val newSheep = createNewSheep(sheepEntity, s)
      val parent1 = sheepEntity.copy(energy = sheepEntity.energy - EnergyLostForReproduction)
      val parent2 = s.copy(energy = s.energy - EnergyLostForReproduction)
      for
        _ <- ZIO.succeed:
          directions = directions.updated(newSheep.id, randomDirection())
        sepPair <- separateEntities(parent1, parent2)
        (sep1, sep2) = sepPair
        separatedUpdated = acc.updateSheep(sep1).updateSheep(sep2)
      yield separatedUpdated.copy(sheep = separatedUpdated.sheep :+ newSheep)
    .map: afterReproduction =>
      val updatedSheep =
        if LifeManager.canDieEntity(sheepEatsGrass) then
          afterReproduction.sheep.filterNot(_.id == sheepEatsGrass.id)
        else afterReproduction.sheep
      afterReproduction.copy(sheep = updatedSheep)
  
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
      val dx = e1.position.x - e2.position.x
      val dy = e1.position.y - e2.position.y
      val distance = math.sqrt(dx * dx + dy * dy)
      val separationVector = if (distance == 0) (1.0, 0.0) else (dx / distance, dy / distance)
      val newPos1 = e1.position.copy(x = (e1.position.x + separationVector._1 * separationDistance).max(0).min(world.width), y = (e1.position.y + separationVector._2 * separationDistance).max(0).min(world.height))
      val newPos2 = e2.position.copy(x = (e2.position.x - separationVector._1 * separationDistance).max(0).min(world.width), y = (e2.position.y - separationVector._2 * separationDistance).max(0).min(world.height))
      (e1.newPosition(newPos1), e2.newPosition(newPos2)) // non posso fare direttamente e1.copy(position = newPos) perchè copy ha bisogno di conoscere il tipo di entità e1.

  private def randomDirection(): (Double, Double) =
    val angle = Math.random() * 2 * Math.PI
    (Math.cos(angle), Math.sin(angle))
  
  def moveEntityDirection(id: EntityId.Type, dx: Double, dy: Double): UIO[Unit] =
    ZIO.succeed:
      directions = directions.updated(id, (dx, dy))
    
  def getWorld: UIO[World] =
    refWorld.get

  def setWorld(newWorld: World): UIO[Unit] =
    refWorld.set(newWorld)
    
  //per testing  
  def getDirections: Map[EntityId.Type, (Double, Double)] =
    directions