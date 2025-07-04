package model.entities

import model.*
import model.managers.{EcosystemManager, MovableEntity}
import zio.{UIO, ZIO}

case class Sheep(id: String, position: Position, energy: Double = 50, mass: Int = 300, speed: Double = 1.5) extends Entity with MovableEntity[Sheep]:

  def move(ecosystemManager: EcosystemManager): UIO[Unit] =
    for
      world <- ecosystemManager.getWorld  
      sheepOpt = world.sheepById(id)
      grassOpt = nearestGrass(id, world)
      _ <- (sheepOpt, grassOpt) match
        case (Some(sheep), Some(food)) =>
          val dx = food.position.x - sheep.position.x
          val dy = food.position.y - sheep.position.y
          val distance = math.hypot(dx, dy)
          if distance > 0 then
            val normalizedDx = dx / distance
            val normalizedDy = dy / distance
            ecosystemManager.moveEntityDirection(id, normalizedDx, normalizedDy)
          else
            ZIO.unit
        case _ => ZIO.unit
    yield ()
  
  def eat: Sheep =
    val gain = 20
    copy(energy = energy + gain)

  private def nearestGrass(sheep: String, world: World): Option[Grass] =
    world.grass
      .sortBy(grass => world.sheepById(sheep).map(w => w.distanceTo(grass)).getOrElse(Double.MaxValue))
      .headOption

  override def withPosition(newPos: Position): Sheep = this.copy(position = newPos)