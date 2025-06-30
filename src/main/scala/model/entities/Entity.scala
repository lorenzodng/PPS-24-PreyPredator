package model.entities

import model.Position

trait Entity:
  def id: String
  def position: Position
  def energy: Double
  def mass: Int
  def radius: Double = math.sqrt(mass / math.Pi)
  def distanceTo(other: Entity): Double =
    val dx = position.x - other.position.x
    val dy = position.y - other.position.y
    math.hypot(dx, dy)





