package model.managers

import model.Position
import model.entities.Entity

object LifeManager:

  def canBornEntity[T <: Entity](entity1: T, entity2: T): Boolean =
    Position.collides(entity1, entity2) && (!entity1.id.equals(entity2.id)) && (entity1.energy > 50 && entity2.energy > 50) 

  def canDieEntity(entity: Entity): Boolean =
    entity.energy <= 0