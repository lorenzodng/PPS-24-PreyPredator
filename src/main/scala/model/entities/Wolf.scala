package model.entities

import model.*
import model.managers.EcosystemManager
import zio.{UIO, ZIO}

/**
 * Represents a wolf entity in the ecosystem.
 *
 * Inherits properties from [[Entity]].
 */
case class Wolf(id: EntityId.Type, position: Position, energy: Double = 50, mass: Int = 1300, speed: Double = 2) extends Entity with MovableEntity[Wolf]:

  /**
   * Moves the wolf towards the nearest sheep if available.
   *
   * @param ecosystemManager the manager to interact with the ecosystem state
   * @return a ZIO effect representing the move operation
   */
  def move(ecosystemManager: EcosystemManager): UIO[Unit] =
    for
      world <- ecosystemManager.getWorld
      wolfOpt = world.wolfById(id)
      sheepOpt = nearestSheep(id, world)
      _ <- (wolfOpt, sheepOpt) match
        case (Some(wolf), Some(food)) => wolf.position.directionTo(food.position) match
          case Some((dx, dy)) => ecosystemManager.updateEntityDirection(id, dx, dy)
          case None => ZIO.unit
        case _ => ZIO.unit
    yield ()

  /**
   * Increases the wolf's energy by eating.
   *
   * @return a new wolf instance with increased energy
   */
  def eat(): Wolf =
    val gain = 10
    copy(energy = energy + gain)

  /**
   * Finds the nearest sheep to the wolf in the world.
   *
   * @param wolf  the ID of the wolf
   * @param world the current state of the world
   * @return an option containing the nearest sheep if any
   */
  private def nearestSheep(wolf: EntityId.Type, world: World): Option[Sheep] =
    world.sheep.sortBy(sheep => world.wolfById(wolf).map(w => w.position.distanceTo(sheep)).getOrElse(Double.MaxValue)).headOption

  /**
   * Returns a new wolf instance with updated position.
   *
   * @param newPos the new position of the wolf
   * @return a new wolf with the updated position
   */
  override def newPosition(newPos: Position): Wolf = this.copy(position = newPos)
