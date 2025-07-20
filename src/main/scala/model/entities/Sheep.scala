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
          case Some((dx, dy)) => ecosystemManager.moveEntityDirection(id, dx, dy)
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
   * Extension method on Position to calculate the normalized direction vector to another position.
   *
   * @param to the target position
   * @return an option containing a tuple (dx, dy) representing the unit direction vector, or nothing if positions coincide
   */
  extension (from: Position)
    private def directionTo(to: Position): Option[(Double, Double)] =
      val dx = to.x - from.x
      val dy = to.y - from.y
      val distance = math.hypot(dx, dy)
      if distance > 0 then
        Some((dx / distance, dy / distance))
      else None

  /**
   * Returns a new sheep instance with updated position.
   *
   * @param newPos the new position of the sheep
   * @return a new sheep with the updated position
   */
  override def newPosition(newPos: Position): Sheep = this.copy(position = newPos)
