package model.entities

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GrassTest extends AnyFunSuite with Matchers:
  
  val grasses: Seq[Grass] = Grass.generateRandomGrass(10, 100, 100)

  test("Generates the correct number of grass entities"):
    grasses.length should be (10)
  
  test("Generated grass entities have unique IDs"):
    val ids = grasses.map(_.id).toSet
    ids.size should be (grasses.size)
  
  test("Generated grass positions are within world bounds"):
    all(grasses.map(_.position.x)) should (be >= 0.0 and be <= 100.0)
    all(grasses.map(_.position.y)) should (be >= 0.0 and be <= 100.0)



