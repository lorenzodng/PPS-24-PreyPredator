package model.managers

import model.Position
import model.entities.Entity

/**
 * Manager responsible for handling entity life-cycle rules such as birth and death.
 */
object LifeManager:

  /**
   * Determines whether two entities can produce a new entity.
   *
   * Two entities can reproduce if:
   * - They collide
   * - They are not the same entity
   * - Both have energy greater than 50
   *
   * @param entity1 the first entity
   * @param entity2 the second entity
   * @tparam T a subtype of [[Entity]]
   * @return true if the entities can produce a new entity, false otherwise
   */
  def canBornEntity[T <: Entity](entity1: T, entity2: T): Boolean =
    Position.collides(entity1, entity2) && entity1.id != entity2.id && entity1.energy > 50 && entity2.energy > 50

  /**
   * Determines whether an entity should die due to lack of energy.
   *
   * @param entity the entity to evaluate
   * @return true if the entity has zero or negative energy, false otherwise
   */
  def canDieEntity(entity: Entity): Boolean =
    entity.energy <= 0
