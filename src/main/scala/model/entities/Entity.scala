package model.entities

import model.Position

import java.util.UUID

/**
 * Singleton object that defines and manages unique identifiers for entities in the ecosystem.
 */
object EntityId:

  /**
   * Opaque type alias for a `String` used as a unique identifier for entities.
   * This hides the underlying representation (String) to enforce type safety and prevent accidental misuse.
   */
  opaque type Type = String

  /**
   * Generates a new random unique EntityId as a UUID string.
   *
   * @return a new random EntityId.Type
   */
  def random: Type = UUID.randomUUID().toString

/**
 * Trait representing a generic entity in the ecosystem.
 */
trait Entity:
  /** Unique identifier of the entity */
  val id: EntityId.Type
  /** Position of the entity in the world */
  val position: Position
  /** Current energy level of the entity */
  val energy: Double
  /** Mass of the entity */
  val mass: Int
  /** Movement speed of the entity */
  val speed: Double

/**
 * Companion object for the [[Entity]] trait.
 *
 * Provides a utility method for entities.
 */
object Entity:
  /** Extension method to compute the radius from mass */
  extension (e: Entity)
    def radius(): Double = math.sqrt(e.mass / math.Pi)
