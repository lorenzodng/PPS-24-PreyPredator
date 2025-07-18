package model.entities

import model.Position

/**
 * Represents a grass entity in the ecosystem.
 *
 * Inherits properties from [[Entity]].
 * Grass is a static entity that does not move and serves as food for sheep.
 */
case class Grass(id: EntityId.Type, position: Position, energy: Double = 0, mass: Int = 300, speed: Double = 0) extends Entity

object Grass:

  /**
   * Generates a sequence of Grass entities with random positions within the world bounds.
   *
   * @param grassCount  the number of grass entities to generate
   * @param worldWidth  the width of the simulation world
   * @param worldHeight the height of the simulation world
   * @return a sequence of randomly positioned Grass entities
   */
  def generateRandomGrass(grassCount: Int, worldWidth: Double, worldHeight: Double): Seq[Grass] =
    (1 to grassCount).map: _ =>
      Grass(id = EntityId.random, position = Position(x = Math.random() * worldWidth, y = Math.random() * worldHeight))
