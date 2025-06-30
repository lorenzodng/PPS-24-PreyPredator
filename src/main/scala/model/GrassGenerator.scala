package model

import model.entities.Grass

object GrassGenerator:

  def generateRandomGrass(grassCount: Int, worldWidth: Double, worldHeight: Double): Seq[Grass] =
    (1 to grassCount).map:  _ => 
      Grass(id = java.util.UUID.randomUUID().toString, position = Position(x = Math.random() * worldWidth, y = Math.random() * worldHeight))
    
