package view

import model.World
import java.awt.{Color, Graphics2D}

object SimulationViewUtils:

  def drawWorld(g: Graphics2D, world: World, offsetX: Double = 0, offsetY: Double = 0): Unit =

    def toScreenCenter(x: Double, y: Double, radius: Int, offsetX: Double, offsetY: Double): (Int, Int) =
      ((x - offsetX - radius).toInt, (y - offsetY - radius).toInt)
      
    g.setColor(Color.green)
    world.grass.foreach: grass =>
      val radius = grass.radius.toInt
      val diameter = radius * 2
      val (x, y) = toScreenCenter(grass.position.x, grass.position.y, radius, offsetX, offsetY)
      g.fillOval(x, y, diameter, diameter)

    world.sheep.foreach: sheep =>
      val radius = sheep.radius.toInt
      val diameter = radius * 2
      val (x, y) = toScreenCenter(sheep.position.x, sheep.position.y, radius, offsetX, offsetY)
      g.setColor(Color.blue)
      g.fillOval(x, y, diameter, diameter)

    world.wolves.foreach: wolf =>
      val radius = wolf.radius.toInt
      val diameter = radius * 2
      val (x, y) = toScreenCenter(wolf.position.x, wolf.position.y, radius, offsetX, offsetY)
      g.setColor(Color.black)
      g.fillOval(x, y, diameter, diameter)




