package model.managers

import model.entities.{Sheep, Wolf}
import model.{GrassGenerator, Position, World}

trait MovableEntity[T]:
  def position: Position
  def withPosition(position: Position): T

class EcosystemManager(var world: World, val speed: Double = 1.5):

  private var directions: Map[String, (Double, Double)] = Map.empty
  private var tickCounter = 0
  private val LOST_ENERGY = 0.2
  private val GRASS_FREQUENCY = 100
  private val GRASS_AMOUNT = 50
  
  def moveEntityDirection(id: String, dx: Double, dy: Double): Unit =
    directions = directions.updated(id, (dx, dy))

  def tick(): Unit =
    directions.foreach:
      case (id, (dx, dy)) =>
        world.wolfById(id) match
          case Some(wolf) =>
            world = updateWorldAfterWolfMovement(updateWolfPosition(wolf, dx, dy))
          case None =>
        world.sheepById(id) match
          case Some(sheep) =>
            world = updateWorldAfterSheepMovement(updateSheepPosition(sheep, dx, dy))
          case None =>

    tickCounter += 1
    if tickCounter >= GRASS_FREQUENCY then
      val newGrass = GrassGenerator.generateRandomGrass(GRASS_AMOUNT, world.width, world.height)
      world = world.addGrass(newGrass)
      tickCounter = 0

    directions = directions.filter(entry => world.wolfById(entry._1).isDefined || world.sheepById(entry._1).isDefined)

  private def updateWolfPosition(wolfEntity: Wolf, dx: Double, dy: Double): Wolf =
    val newX = (wolfEntity.position.x + dx * speed + world.width) % world.width
    val newY = (wolfEntity.position.y + dy * speed + world.height) % world.height
    val newPosition = wolfEntity.position.copy(x = newX, y = newY)
    val newEnergy = wolfEntity.energy - LOST_ENERGY
    wolfEntity.copy(position = newPosition, energy = newEnergy)

  private def updateSheepPosition(sheepEntity: Sheep, dx: Double, dy: Double): Sheep =
    val newX = (sheepEntity.position.x + dx * speed + world.width) % world.width
    val newY = (sheepEntity.position.y + dy * speed + world.height) % world.height
    val newPosition = sheepEntity.position.copy(x = newX, y = newY)
    val newEnergy = sheepEntity.energy - LOST_ENERGY
    sheepEntity.copy(position = newPosition, energy = newEnergy)

  private def updateWorldAfterWolfMovement(wolfEntity: Wolf): World =
    val sheepEaten = world.sheep.filter(sheep => EatingManager.canEatSheep(wolfEntity, sheep))
    val wolfEatsSheep = sheepEaten.foldLeft(wolfEntity)((w, sheep) => w.eat)
    val afterEating = world.updateWolf(wolfEatsSheep).removeSheep(sheepEaten)
    val wolvesCanReproduce = world.wolves.filter(wolf => LifeManager.canBornEntity(wolfEntity, wolf))
    val afterReproduction = wolvesCanReproduce.foldLeft(afterEating): (wAcc, w) =>
      val newWolf = createNewWolf(wolfEntity, w)
      directions = directions.updated(newWolf.id, randomDirection())
      val (sep1, sep2) = separateEntities(wolfEntity, w)
      val separatedUpdated = wAcc.updateWolf(sep1).updateWolf(sep2)
      separatedUpdated.copy(wolves = separatedUpdated.wolves :+ newWolf)

    val updatedWolves =
      if LifeManager.canDieEntity(wolfEatsSheep) then
        afterReproduction.wolves.filterNot(_.id == wolfEatsSheep.id)
      else afterReproduction.wolves

    afterReproduction.copy(wolves = updatedWolves)

  private def updateWorldAfterSheepMovement(sheepEntity: Sheep): World =
    val grassEaten = world.grass.filter(grass => EatingManager.canEatGrass(sheepEntity, grass))
    val sheepEatsGrass = grassEaten.foldLeft(sheepEntity)((s, grass) => s.eat)
    val afterEating = world.updateSheep(sheepEatsGrass).removeGrass(grassEaten)
    val sheepCanReproduce = world.sheep.filter(sheep => LifeManager.canBornEntity(sheepEntity, sheep))
    val afterReproduction = sheepCanReproduce.foldLeft(afterEating): (wAcc, s) =>
      val newSheep = createNewSheep(sheepEntity, s)
      directions = directions.updated(newSheep.id, randomDirection())
      val (sep1, sep2) = separateEntities(sheepEntity, s)
      val separatedUpdated = wAcc.updateSheep(sep1).updateSheep(sep2)
      separatedUpdated.copy(sheep = separatedUpdated.sheep :+ newSheep)

    println(s"Number of sheep after eating: ${afterReproduction.sheep.size}")
    val updatedSheep =
      if LifeManager.canDieEntity(sheepEatsGrass) then
        afterReproduction.sheep.filterNot(_.id == sheepEatsGrass.id)
      else afterReproduction.sheep

    afterReproduction.copy(sheep = updatedSheep)

  private def createNewWolf(parent1: Wolf, parent2: Wolf): Wolf =
    val newId = java.util.UUID.randomUUID().toString
    val newPosition = Position((parent1.position.x + parent2.position.x) / 2, (parent1.position.y + parent2.position.y) / 2)
    Wolf(newId, newPosition)

  private def createNewSheep(parent1: Sheep, parent2: Sheep): Sheep =
    val newId = java.util.UUID.randomUUID().toString
    val newPosition = Position((parent1.position.x + parent2.position.x) / 2, (parent1.position.y + parent2.position.y) / 2)
    Sheep(newId, newPosition)

  private def separateEntities[T <: MovableEntity[T]](e1: T, e2: T, separationDistance: Double = 20.0): (T, T) =
    val dx = e1.position.x - e2.position.x
    val dy = e1.position.y - e2.position.y
    val distance = math.sqrt(dx * dx + dy * dy)

    val separationVector = if (distance == 0) (1.0, 0.0) else (dx / distance, dy / distance)

    val newPos1 = e1.position.copy(
      x = (e1.position.x + separationVector._1 * separationDistance).max(0).min(world.width),
      y = (e1.position.y + separationVector._2 * separationDistance).max(0).min(world.height)
    )

    val newPos2 = e2.position.copy(
      x = (e2.position.x - separationVector._1 * separationDistance).max(0).min(world.width),
      y = (e2.position.y - separationVector._2 * separationDistance).max(0).min(world.height)
    )

    (e1.withPosition(position = newPos1), e2.withPosition(position = newPos2))

  private def randomDirection(): (Double, Double) =
    val angle = Math.random() * 2 * Math.PI
    (Math.cos(angle), Math.sin(angle))