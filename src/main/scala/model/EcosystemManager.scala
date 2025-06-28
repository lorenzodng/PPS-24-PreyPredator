package model

trait SimulationManager:

  def getWorld: World
  def moveEntityDirection(id: String, dx: Double, dy: Double): Unit

class EcosystemManager(var world: World, val speed: Double = 10.0) extends SimulationManager:

  private var directions: Map[String, (Double, Double)] = Map.empty
  private val LOST_ENERGY = 5

  def getWorld: World = world

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

  private def updateWolfPosition(wolfEntity: Wolf, dx: Double, dy: Double): Wolf =
    val newX = (wolfEntity.position.x + dx * speed).max(0).min(world.width)
    val newY = (wolfEntity.position.y + dy * speed).max(0).min(world.height)
    val newPosition = wolfEntity.position.copy(x = newX, y = newY)
    val newEnergy = wolfEntity.energy - LOST_ENERGY
    wolfEntity.copy(position = newPosition, energy = newEnergy)

  private def updateSheepPosition(sheepEntity: Sheep, dx: Double, dy: Double): Sheep =
    val newX = (sheepEntity.position.x + dx * speed).max(0).min(world.width)
    val newY = (sheepEntity.position.y + dy * speed).max(0).min(world.height)
    val newPosition = sheepEntity.position.copy(x = newX, y = newY)
    val newEnergy = sheepEntity.energy - LOST_ENERGY
    sheepEntity.copy(position = newPosition, energy = newEnergy)

  private def updateWorldAfterWolfMovement(wolfEntity: Wolf): World =
    val sheepEaten = world.sheep.filter(sheep => EatingManager.canEatSheep(wolfEntity, sheep))
    val wolfEatsSheep = sheepEaten.foldLeft(wolfEntity)((w, sheep) => w.eat)
    val wolfsCanReproduce = world.wolfs.filter(wolf => LifeManager.canBornEntity(wolfEntity, wolf))
    val newWolfs = wolfsCanReproduce.map(w => createNewWolf(wolfEntity, w))
    world.updateWolf(wolfEatsSheep).removeSheep(sheepEaten)
    
  private def updateWorldAfterSheepMovement(sheepEntity: Sheep): World =
    val grassEaten = world.grass.filter(grass => EatingManager.canEatGrass(sheepEntity, grass)) 
    val sheepEatsGrass = grassEaten.foldLeft(sheepEntity)((s, grass) => s.eat(grass))
    val sheepCanReproduce = world.sheep.filter(sheep => LifeManager.canBornEntity(sheepEntity, sheep))
    val newSheep = sheepCanReproduce.map(s => createNewSheep(sheepEntity, s))
    world.updateSheep(sheepEatsGrass).removeGrass(grassEaten)

  private def createNewWolf(parent1: Wolf, parent2: Wolf): Wolf =
    val newId = java.util.UUID.randomUUID().toString
    val newPosition = Position((parent1.position.x + parent2.position.x) / 2, (parent1.position.y + parent2.position.y) / 2)
    Wolf(newId, newPosition, 50)

  private def createNewSheep(parent1: Sheep, parent2: Sheep): Sheep =
    val newId = java.util.UUID.randomUUID().toString
    val newPosition = Position((parent1.position.x + parent2.position.x) / 2, (parent1.position.y + parent2.position.y) / 2)
    Sheep(newId, newPosition, 50)
