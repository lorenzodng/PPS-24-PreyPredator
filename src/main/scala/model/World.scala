package model

import model.entities.{Entity, EntityId, Grass, Sheep, Wolf}

/**
 * Represents the simulation world.
 *
 * @param width  width of the world
 * @param height height of the world
 * @param wolves sequence of wolf entities
 * @param sheep  sequence of sheep entities
 * @param grass  sequence of grass entities
 */
case class World(width: Int, height: Int, wolves: Seq[Wolf], sheep: Seq[Sheep], grass: Seq[Grass]):

  /**
   * Returns all entities (wolves, sheep, and grass) in the world.
   *
   * @return sequence of all entities
   */
  def entities: Seq[Entity] = wolves ++ sheep ++ grass

  /**
   * Generates a specified number of wolves at random positions within the world.
   *
   * @param nWolves number of wolves to generate
   * @return new world instance with generated wolves added
   */
  def generateWolves(nWolves: Int): World =
    val newWolves = (1 to nWolves).map(_ =>
      val randomId = EntityId.random
      val randomPosition = Position(x = math.random() * width, y = math.random() * height)
      Wolf(randomId, randomPosition))
    copy(wolves = wolves ++ newWolves)

  /**
   * Generates a specified number of sheep at random positions within the world.
   *
   * @param nSheep number of sheep to generate
   * @return new world instance with generated sheep added
   */
  def generateSheep(nSheep: Int): World =
    val newSheep = (1 to nSheep).map(_ =>
      val randomId = EntityId.random
      val randomPosition = Position(x = math.random() * width, y = math.random() * height)
      Sheep(randomId, randomPosition))
    copy(sheep = sheep ++ newSheep)

  /**
   * Generates a specified number of grass patches at random positions within the world.
   *
   * @param nGrass number of grass patches to generate
   * @return new world instance with generated grass added
   */
  def generateGrass(nGrass: Int): World =
    val newGrass = (1 to nGrass).map(_ =>
      val randomId = EntityId.random
      val randomPosition = Position(x = math.random() * width, y = math.random() * height)
      Grass(randomId, randomPosition))
    copy(grass = grass ++ newGrass)

  /**
   * Finds a wolf by its entity ID.
   *
   * @param id the ID of the wolf to find
   * @return Option containing the wolf if found, otherwise None
   */
  def wolfById(id: EntityId.Type): Option[Wolf] =
    wolves.find(_.id == id)

  /**
   * Finds a sheep by its entity ID.
   *
   * @param id the ID of the sheep to find
   * @return Option containing the sheep if found, otherwise None
   */
  def sheepById(id: EntityId.Type): Option[Sheep] =
    sheep.find(_.id == id)

  /**
   * Updates an existing wolf in the world with a new state.
   *
   * @param wolfEntity the wolf entity to update
   * @return new world instance with the updated wolf
   */
  def updateWolf(wolfEntity: Wolf): World =
    copy(wolves = wolves.map(w => if w.id == wolfEntity.id then wolfEntity else w))

  /**
   * Updates an existing sheep in the world with a new state.
   *
   * @param sheepEntity the sheep entity to update
   * @return new world instance with the updated sheep
   */
  def updateSheep(sheepEntity: Sheep): World =
    copy(sheep = sheep.map(s => if s.id == sheepEntity.id then sheepEntity else s))

  /**
   * Removes specified sheep from the world.
   *
   * @param ids sequence of sheep entities to remove
   * @return new world instance with the sheep removed
   */
  def removeSheep(ids: Seq[Sheep]): World =
    copy(sheep = sheep.filterNot(s => ids.contains(s)))

  /**
   * Removes specified grass patches from the world.
   *
   * @param ids sequence of grass entities to remove
   * @return new world instance with the grass removed
   */
  def removeGrass(ids: Seq[Grass]): World =
    copy(grass = grass.filterNot(g => ids.contains(g)))

  /**
   * Adds new grass patches to the world.
   *
   * @param newGrass sequence of grass entities to add
   * @return new world instance with added grass patches
   */
  def addGrass(newGrass: Seq[Grass]): World =
    copy(grass = grass ++ newGrass)

  /**
   * Deletes all entities (wolves, sheep, and grass) from the world.
   *
   * @return new World instance with no entities
   */
  def deleteEntities(): World =
    copy(wolves = Seq.empty, sheep = Seq.empty, grass = Seq.empty)