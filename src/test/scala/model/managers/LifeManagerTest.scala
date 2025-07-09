package model.managers

import model.*
import model.entities.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class LifeManagerTest extends AnyFunSuite with Matchers:

  val ZeroEnergy = 0
  val LowEnergy = 40
  val HighEnergy = 60
  val DefaultPosition: Position = Position(10, 10)

  test("Entity can be born"):
    val wolf1 = Wolf(EntityId.random, DefaultPosition, energy = HighEnergy)
    val wolf2 = Wolf(EntityId.random, DefaultPosition, energy = HighEnergy)
    LifeManager.canBornEntity(wolf1, wolf2) shouldBe true

  test("Entity cannot be born if low energies"):
    val id = EntityId.random
    val wolf1 = Wolf(id, DefaultPosition, energy = LowEnergy)
    val wolf2 = Wolf(id, DefaultPosition, energy = LowEnergy)
    LifeManager.canBornEntity(wolf1, wolf2) shouldBe false

  test("Entity cannot be born if not colliding"):
    val wolf2Position = Position(40, 40)
    val wolf1 = Wolf(EntityId.random, DefaultPosition, energy = HighEnergy)
    val wolf2 = Wolf(EntityId.random, wolf2Position, energy = HighEnergy)
    LifeManager.canBornEntity(wolf1, wolf2) shouldBe false

  test("Entity cannot be born if low and different energies"):
    val wolf1 = Wolf(EntityId.random, DefaultPosition, energy = HighEnergy)
    val wolf2 = Wolf(EntityId.random, DefaultPosition, energy = LowEnergy)
    LifeManager.canBornEntity(wolf1, wolf2) shouldBe false

  test("Entity can die"):
    val wolf = Wolf(EntityId.random, DefaultPosition, energy = ZeroEnergy)
    LifeManager.canDieEntity(wolf) shouldBe true

  test("Entity cannot die if not null energy"):
    val wolf = Wolf(EntityId.random, DefaultPosition, energy = LowEnergy)
    LifeManager.canDieEntity(wolf) shouldBe false

