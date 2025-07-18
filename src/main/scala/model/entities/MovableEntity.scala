package model.entities

import model.Position

/**
 * Trait that represents an entity capable of movement in the simulation.
 *
 * @tparam T the concrete type of the entity implementing this trait
 */
trait MovableEntity[T]:

  /**
   * The current position of the entity in the simulation world.
   *
   * @return the current Position of the entity
   */
  def position: Position

  /**
   * Creates a new instance of the entity with the specified position.
   * Used to represent movement while preserving immutability.
   *
   * @param position the new Position of the entity
   * @return a new instance of the entity with updated position
   */
  def newPosition(position: Position): T
