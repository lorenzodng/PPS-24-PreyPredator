package model.entities

import model.Position

case class Grass(id: String, position: Position, energy: Double = 0, mass: Int = 100) extends Entity
