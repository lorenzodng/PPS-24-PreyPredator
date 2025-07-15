package model.entities

import model.*
import model.managers.{EcosystemManager, MovableEntity}
import zio.{UIO, ZIO}

case class Wolf(id: EntityId.Type, position: Position, energy: Double = 50, mass: Int = 1300, speed: Double = 2) extends Entity with MovableEntity[Wolf]:
  
  def move(ecosystemManager: EcosystemManager): UIO[Unit] =
    for
      world <- ecosystemManager.getWorld
      wolfOpt = world.wolfById(id)
      sheepOpt = nearestSheep(id, world)
      _ <- (wolfOpt, sheepOpt) match
        case (Some(wolf), Some(food)) => wolf.position.directionTo(food.position) match
          case Some((dx, dy)) => ecosystemManager.moveEntityDirection(id, dx, dy)
          case None => ZIO.unit
        case _ => ZIO.unit
    yield ()
  
  def eat: Wolf =
    val gain = 10
    copy(energy = energy + gain)
    
  private def nearestSheep(wolf: EntityId.Type, world: World): Option[Sheep] =
    world.sheep.sortBy(sheep => world.wolfById(wolf).map(w => w.position.distanceTo(sheep)).getOrElse(Double.MaxValue)).headOption

  extension (from: Position)
    private def directionTo(to: Position): Option[(Double, Double)] =
      val dx = to.x - from.x
      val dy = to.y - from.y
      val distance = math.hypot(dx, dy)
      if distance > 0 then
        Some((dx / distance, dy / distance))
      else None
      
  //è necessario per non duplicare separateEntities
  override def newPosition(newPos: Position): Wolf = this.copy(position = newPos)