package model

object GrassGenerator:

  def generateRandomGrass(foodCount: Int, worldWidth: Double, worldHeight: Double): Seq[Grass] =
    (1 to foodCount).map:  _ => 
      Grass(id = java.util.UUID.randomUUID().toString, position = Position(x = Math.random() * worldWidth, y = Math.random() * worldHeight))
    
