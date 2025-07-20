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
   * Calculates the normalized direction vector from the current position to a target position.
   *
   * @param to the target position to calculate the direction towards
   * @return an option containing a tuple representing the unit direction vector, or `None` if the target position coincides with the current position
   */
  def directionTo(to: Position): Option[(Double, Double)] =
    val dx = to.x - x
    val dy = to.y - y
    val distance = math.hypot(dx, dy)
    if distance > 0 then Some((dx / distance, dy / distance)) else None

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
