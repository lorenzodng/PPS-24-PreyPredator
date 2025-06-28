package controller

import model.{Sheep, SimulationManager, World}

object WolfMovement:
  
  def moveWolf(id: String, ecosystemManager: SimulationManager): Unit =
    val world = ecosystemManager.getWorld 
    val wolfEntity = world.wolfById(id) 
    val sheepOpt = nearestSheep(id, world) 
    (wolfEntity, sheepOpt) match
      case (Some(wolf), Some(prey)) => 
        val dx = prey.position.x - wolf.position.x 
        val dy = prey.position.y - wolf.position.y 
        val distance = math.hypot(dx, dy)
        if (distance > 0) 
          val normalizedDx = dx / distance 
          val normalizedDy = dy / distance 
          ecosystemManager.moveEntityDirection(id, normalizedDx, normalizedDy) 
      case _ =>
  
  private def nearestSheep(wolf: String, world: World): Option[Sheep] =
    world.sheep
      .sortBy(sheep => world.wolfById(wolf).map(w => w.distanceTo(sheep)).getOrElse(Double.MaxValue))
      .headOption      
