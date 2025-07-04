package model.entities

import model.Position

trait Entity:
  val id: String
  val position: Position
  val energy: Double
  val mass: Int
  val radius: Double = math.sqrt(mass / math.Pi)
  val speed: Double 
  def distanceTo(other: Entity): Double =
    val dx = position.x - other.position.x
    val dy = position.y - other.position.y
    math.hypot(dx, dy)





