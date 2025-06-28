package model

object LifeManager:

  def canBornEntity[T <: Entity](entity1: T, entity2: T): Boolean =
    collides(entity1, entity2) && (entity1.energy > 50 && entity2.energy > 50)

  def canDieEntity(entity: Entity): Boolean =
    entity.energy == 0

  private def collides(e1: Entity, e2: Entity): Boolean =
    e1.distanceTo(e2) < (e1.radius + e2.radius)