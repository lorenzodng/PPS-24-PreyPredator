package model.managers

import model.Position
import model.entities.{Grass, Sheep, Wolf}

/**
 * Manager responsible for handling eating interactions between entities.
 */
object EatingManager:

  /**
   * Determines if a wolf can eat a sheep by checking if they collide.
   *
   * @param wolfEntity  the wolf entity attempting to eat
   * @param sheepEntity the sheep entity being targeted
   * @return true if the wolf and the sheep collide, false otherwise
   */
  def canEatSheep(wolfEntity: Wolf, sheepEntity: Sheep): Boolean =
    Position.collides(wolfEntity, sheepEntity)

  /**
   * Determines if a sheep can eat grass by checking if they collide.
   *
   * @param sheepEntity the sheep entity attempting to eat
   * @param grassEntity the grass entity being targeted
   * @return true if the sheep and the grass collide, false otherwise
   */
  def canEatGrass(sheepEntity: Sheep, grassEntity: Grass): Boolean =
    Position.collides(sheepEntity, grassEntity)
