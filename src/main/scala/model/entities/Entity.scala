package model.entities

import model.Position

object EntityId:
  
  opaque type Type = String

  def random: Type = java.util.UUID.randomUUID().toString

trait Entity:
  val id: EntityId.Type
  val position: Position
  val energy: Double
  val mass: Int
  val speed: Double
  val radius: Double = math.sqrt(mass / math.Pi)

