package model.managers

import model.*
import model.entities.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class EatingManagerTest extends AnyFunSuite with Matchers:
  
  val defaultPosition: Position = Position(10, 10)

  test("Wolf can eat sheep"):
    val wolf = Wolf(EntityId.random, defaultPosition)
    val sheep = Sheep(EntityId.random, defaultPosition)
    EatingManager.canEatSheep(wolf, sheep) shouldBe true

  test("Wolf cannot eat sheep"):
    val sheepPosition = Position(40, 40)
    val wolf = Wolf(EntityId.random, defaultPosition)
    val sheep = Sheep(EntityId.random, sheepPosition)
    EatingManager.canEatSheep(wolf, sheep) shouldBe false

  test("Sheep can eat grass"):
    val sheep = Sheep(EntityId.random, defaultPosition)
    val grass = Grass(EntityId.random, defaultPosition)
    EatingManager.canEatGrass(sheep, grass) shouldBe true

  test("Sheep cannot eat grass"):
    val grassPosition = Position(40, 40)
    val sheep = Sheep(EntityId.random, defaultPosition)
    val grass = Grass(EntityId.random, grassPosition)
    EatingManager.canEatGrass(sheep, grass) shouldBe false
