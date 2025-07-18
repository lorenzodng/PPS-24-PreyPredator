package model.entities

import model.Position

/**
 * Container object for the opaque type `EntityId.Type` representing unique entity identifiers.
 * Provides utility methods related to EntityId, such as random ID generation.
 */
object EntityId:

  /**
   * Opaque type alias for a `String` used as a unique identifier for entities.
   * This hides the underlying representation (String) to enforce type safety and prevent accidental misuse.
   */
  opaque type Type = String

  /**
   * Generates a new random unique EntityId as a UUID string.
   * @return a new random EntityId.Type
   */
  def random: Type = java.util.UUID.randomUUID().toString

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
  /** Radius of the entity */
  val radius: Double = math.sqrt(mass / math.Pi)
