package model.entities

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GrassTest extends AnyFunSuite with Matchers:

  test("Grass generation"):
    val grassCount = 10
    val worldWidth = 100
    val worldHeight = 100
    val grasses = Grass.generateRandomGrass(grassCount, worldWidth, worldHeight)
    grasses.length should be (grassCount)
    val ids = grasses.map(_.id).toSet
    ids.size should be (grasses.size)
    all(grasses.map(_.position.x)) should be >= 0.0
    all(grasses.map(_.position.x)) should be <= worldWidth.toDouble
    all(grasses.map(_.position.y)) should be >= 0.0
    all(grasses.map(_.position.y)) should be <= worldHeight.toDouble


