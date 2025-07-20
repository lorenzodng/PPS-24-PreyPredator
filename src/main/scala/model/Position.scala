package model

import model.entities.Entity

/**
 * Represents a position in 2D space with x and y coordinates.
 */
case class Position(x: Double, y: Double):

  /**
   * Calculates the distance to another entity's position.
   *
   * @param other the entity to which distance is measured
   * @return the distance as a Double
   */
  def distanceTo(other: Entity): Double =
    val dx = x - other.position.x
    val dy = y - other.position.y
    math.hypot(dx, dy)

/**
 * Companion object for the [[Position]] case class.
 *
 * Provides a utility method related to position calculations within the simulation world.
 */
object Position:

  /**
   * Determines if two entities collide based on the distance between their positions and the sum of their radius.
   *
   * @param e1 the first entity
   * @param e2 the second entity
   * @return true if the entities collide, false otherwise
   */
  def collides(e1: Entity, e2: Entity): Boolean =
    e1.position.distanceTo(e2) < (e1.radius + e2.radius)
