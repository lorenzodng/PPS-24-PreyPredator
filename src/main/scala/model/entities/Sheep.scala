package model.entities

import model.*
import model.managers.EcosystemManager
import zio.{UIO, ZIO}

/**
 * Represents a sheep entity in the ecosystem.
 *
 * Inherits properties from [[Entity]].
 */
case class Sheep(id: EntityId.Type, position: Position, energy: Double = 50, mass: Int = 1300, speed: Double = 1.5) extends Entity with MovableEntity[Sheep]:

  /**
   * Moves the sheep towards the nearest grass if available.
   *
   * @param ecosystemManager the manager to interact with the ecosystem state
   * @return a ZIO effect representing the move operation
   */
  def move(ecosystemManager: EcosystemManager): UIO[Unit] =
    for
      world <- ecosystemManager.getWorld
      sheepOpt = world.sheepById(id)
      grassOpt = nearestGrass(id, world)
      _ <- (sheepOpt, grassOpt) match
        case (Some(sheep), Some(food)) => sheep.position.directionTo(food.position) match
          case Some((dx, dy)) => ecosystemManager.updateEntityDirection(id, dx, dy)
          case None => ZIO.unit
        case _ => ZIO.unit
    yield ()

  /**
   * Increases the sheep's energy by eating.
   *
   * @return a new sheep instance with increased energy
   */
  def eat(): Sheep =
    val gain = 10
    copy(energy = energy + gain)

  /**
   * Finds the nearest grass to the sheep in the world.
   *
   * @param sheep the ID of the sheep
   * @param world the current state of the world
   * @return an option containing the nearest grass if any
   */
  private def nearestGrass(sheep: EntityId.Type, world: World): Option[Grass] =
    world.grass.sortBy(grass => world.sheepById(sheep).map(s => s.position.distanceTo(grass)).getOrElse(Double.MaxValue)).headOption

  /**
   * Returns a new sheep instance with updated position.
   *
   * @param newPos the new position of the sheep
   * @return a new sheep with the updated position
   */
  override def newPosition(newPos: Position): Sheep = this.copy(position = newPos)
