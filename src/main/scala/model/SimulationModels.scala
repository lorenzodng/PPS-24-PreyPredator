package model

sealed trait Entity:
  def id: String
  def position: Position
  def energy: Int
  def mass: Int
  def radius: Double = math.sqrt(mass / math.Pi)
  def distanceTo(other: Entity): Double =
    val dx = position.x - other.position.x
    val dy = position.y - other.position.y
    math.hypot(dx, dy)
    
case class Wolf(id: String, position: Position, energy: Int, mass: Int = 200) extends Entity:

  def eat: Wolf =
    val gain = 10
    copy(energy = energy + gain)

case class Sheep(id: String, position: Position, energy: Int, mass: Int = 250) extends Entity:
  
  def eat(grass: Grass): Sheep =
    copy(energy = energy + grass.energy)

case class Grass(id: String, position: Position, energy: Int = 0, mass: Int = 100) extends Entity
  
case class Position(x: Double, y: Double)

case class World(width: Int, height: Int, wolfs: Seq[Wolf], sheep: Seq[Sheep], grass: Seq[Grass]):
  
  def wolfExcludingSelf(wolfEntity: Wolf): Seq[Wolf] =
    wolfs.filterNot(_.id == wolfEntity.id)

  def sheepExcludingSelf(sheepEntity: Sheep): Seq[Sheep] =
    sheep.filterNot(_.id == sheepEntity.id)  
  
  def wolfById(id: String): Option[Wolf] =
    wolfs.find(_.id == id)
   
  def sheepById(id: String): Option[Sheep] = 
    sheep.find(_.id == id)
  
  def updateWolf(wolfEntity: Wolf): World =
    copy(wolfs = wolfs.map(w => if (w.id == wolfEntity.id) wolfEntity else w)) 

  def updateSheep(sheepEntity: Sheep): World =
    copy(sheep = sheep.map(s => if (s.id == sheepEntity.id) sheepEntity else s))
    
  def removeWolfs(ids: Seq[Wolf]): World =
    copy(wolfs = wolfs.filterNot(w => ids.map(_.id).contains(w.id)))

  def removeSheep(ids: Seq[Sheep]): World =
    copy(sheep = sheep.filterNot(s => ids.map(_.id).contains(s.id)))  
  
  def removeGrass(ids: Seq[Grass]): World =
    copy(grass = grass.filterNot(g => ids.contains(g)))

  def addWolf(newWolf: Wolf): World =
    copy(wolfs = wolfs :+ newWolf)