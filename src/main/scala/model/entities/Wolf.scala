package model.entities

import model.*
import model.managers.{EcosystemManager, MovableEntity}

case class Wolf(id: String, position: Position, energy: Double = 50, mass: Int = 350) extends Entity with MovableEntity[Wolf]:
  
  def move(ecosystemManager: EcosystemManager): Unit =
    val world = ecosystemManager.world 
    
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
  
  def eat: Wolf =
    val gain = 10
    copy(energy = energy + gain)
    
  private def nearestSheep(wolf: String, world: World): Option[Sheep] =
    world.sheep
      .sortBy(sheep => world.wolfById(wolf).map(w => w.distanceTo(sheep)).getOrElse(Double.MaxValue))
      .headOption

  override def withPosition(newPos: Position): Wolf = this.copy(position = newPos)