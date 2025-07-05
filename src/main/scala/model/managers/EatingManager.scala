package model.managers

import model.Position
import model.entities.{Grass, Sheep, Wolf}

object EatingManager:

  private val MASS_MARGIN = 1.1

  def canEatSheep(wolfEntity: Wolf, sheepEntity: Sheep): Boolean =
    Position.collides(wolfEntity, sheepEntity)

  def canEatGrass(sheepEntity: Sheep, grassEntity: Grass): Boolean =
    Position.collides(sheepEntity, grassEntity)
