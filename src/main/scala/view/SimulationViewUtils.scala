package view

import model.World
import view.SimulationIcons.*

import java.awt.Graphics2D

object SimulationViewUtils:

  def drawWorld(g: Graphics2D, world: World, offsetX: Double = 0, offsetY: Double = 0): Unit =

    def toScreenTopLeft(x: Double, y: Double, radius: Int, offsetX: Double, offsetY: Double): (Int, Int) =
      ((x - offsetX - radius).toInt, (y - offsetY - radius).toInt)

    world.grass.foreach: grass =>
      val radius = grass.radius.toInt
      val (x, y) = toScreenTopLeft(grass.position.x, grass.position.y, radius, offsetX, offsetY)
      g.drawImage(grassIcon, x, y, radius * 2, radius * 2, null)

    world.sheep.foreach: sheep =>
      val radius = sheep.radius.toInt
      val (x, y) = toScreenTopLeft(sheep.position.x, sheep.position.y, radius, offsetX, offsetY)
      g.drawImage(sheepIcon, x, y, radius * 2, radius * 2, null)

    world.wolves.foreach: wolf =>
      val radius = wolf.radius.toInt
      val (x, y) = toScreenTopLeft(wolf.position.x, wolf.position.y, radius, offsetX, offsetY)
      g.drawImage(wolfIcon, x, y, radius * 2, radius * 2, null)




