package model

import model.entities.Entity

case class Position(x: Double, y: Double):

  def distanceTo(other: Entity): Double =
    val dx = x - other.position.x
    val dy = y - other.position.y
    math.hypot(dx, dy)

object Position:

  def collides(e1: Entity, e2: Entity): Boolean =
    e1.position.distanceTo(e2) < (e1.radius + e2.radius)