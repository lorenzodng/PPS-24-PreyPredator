package model.entities

import model.Position

case class Grass(id: EntityId.Type, position: Position, energy: Double = 0, mass: Int = 300, speed: Double = 0) extends Entity

object Grass:
  
  def generateRandomGrass(grassCount: Int, worldWidth: Double, worldHeight: Double): Seq[Grass] =
    (1 to grassCount).map: _ => 
      Grass(id = EntityId.random, position = Position(x = Math.random() * worldWidth, y = Math.random() * worldHeight))
