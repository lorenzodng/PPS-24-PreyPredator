package controller

import model.{Grass, SimulationManager, World}

object SheepMovement:
  
  def moveSheep(id: String, ecosystemManager: SimulationManager): Unit =
    val world = ecosystemManager.getWorld 
    val sheepEntity = world.sheepById(id) 
    val grassOpt = nearestGrass(id, world) 
    (sheepEntity, grassOpt) match
      case (Some(sheep), Some(food)) => 
        val dx = food.position.x - sheep.position.x
        val dy = food.position.y - sheep.position.y
        val distance = math.hypot(dx, dy)
        if (distance > 0) 
          val normalizedDx = dx / distance 
          val normalizedDy = dy / distance
          ecosystemManager.moveEntityDirection(id, normalizedDx, normalizedDy) 
      case _ =>

  private def nearestGrass(sheep: String, world: World): Option[Grass] =
    world.grass
      .sortBy(grass => world.sheepById(sheep).map(w => w.distanceTo(grass)).getOrElse(Double.MaxValue))
      .headOption