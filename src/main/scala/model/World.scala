package model

import model.entities.{Entity, Grass, Sheep, Wolf}

case class World(width: Int, height: Int, wolves: Seq[Wolf], sheep: Seq[Sheep], grass: Seq[Grass]):

  def entities: Seq[Entity] = wolves ++ sheep ++ grass

  def generateWolves(nWolves: Int): World =
    val newWolves = (1 to nWolves).map: _ =>
      val randomId = java.util.UUID.randomUUID().toString
      val randomPosition = Position(x = math.random() * width, y = math.random() * height)
      Wolf(randomId, randomPosition)
    copy(wolves = wolves ++ newWolves)

  def generateSheep(nSheep: Int): World =
    val newSheep = (1 to nSheep).map: _ =>
      val randomId = java.util.UUID.randomUUID().toString
      val randomPosition = Position(x = math.random() * width, y = math.random() * height)
      Sheep(randomId, randomPosition)
    copy(sheep = sheep ++ newSheep)

  def generateGrass(nGrass: Int): World =
    val newGrass = (1 to nGrass).map: _ =>
      val randomId = java.util.UUID.randomUUID().toString
      val randomPosition = Position(x = math.random() * width, y = math.random() * height)
      Grass(randomId, randomPosition)
    copy(grass = grass ++ newGrass)

  def wolfById(id: String): Option[Wolf] =
    wolves.find(_.id == id)

  def sheepById(id: String): Option[Sheep] =
    sheep.find(_.id == id)

  def updateWolf(wolfEntity: Wolf): World =
    copy(wolves = wolves.map(w => if (w.id == wolfEntity.id) wolfEntity else w))

  def updateSheep(sheepEntity: Sheep): World =
    copy(sheep = sheep.map(s => if (s.id == sheepEntity.id) sheepEntity else s))

  def removeSheep(ids: Seq[Sheep]): World =
    copy(sheep = sheep.filterNot(s => ids.map(_.id).contains(s.id)))

  def removeGrass(ids: Seq[Grass]): World =
    copy(grass = grass.filterNot(g => ids.contains(g)))

  def addGrass(newGrass: Seq[Grass]): World =
    copy(grass = grass ++ newGrass)

  def deleteEntities(): World =
    copy(wolves = Seq.empty, sheep = Seq.empty, grass = Seq.empty)