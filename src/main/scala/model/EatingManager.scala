package model

object EatingManager:

  private val MASS_MARGIN = 1.1

  def canEatSheep(wolfEntity: Wolf, sheepEntity: Sheep): Boolean =
    collides(wolfEntity, sheepEntity)

  def canEatGrass(sheepEntity: Sheep, grassEntity: Grass): Boolean =
    collides(sheepEntity, grassEntity)
    
  private def collides(e1: Entity, e2: Entity): Boolean =
    e1.distanceTo(e2) < (e1.radius + e2.radius)

  
    
    
    
    
