package view

import model.World
import java.awt.{Color, Graphics2D}

object SimulationViewUtils:

  private val labelOffsetX = 10
  private val labelOffsetY = 0
  private val innerOffset = 2
  private val innerBorder = 4

  def drawWorld(g: Graphics2D, world: World, offsetX: Double = 0, offsetY: Double = 0): Unit =

    def toScreenCenter(x: Double, y: Double, radius: Int, offsetX: Double, offsetY: Double): (Int, Int) =
      ((x - offsetX - radius).toInt, (y - offsetY - radius).toInt)

    def toScreenLabel(x: Double, y: Double, offsetX: Double, offsetY: Double): (Int, Int) =
      ((x - offsetX - labelOffsetX).toInt, (y - offsetY - labelOffsetY).toInt)
      
    g.setColor(Color.green)
    world.grass.foreach: grass =>
      val radius = grass.radius.toInt
      val diameter = radius * 2
      val (x, y) = toScreenCenter(grass.position.x, grass.position.y, radius, offsetX, offsetY)
      g.fillOval(x, y, diameter, diameter)

    world.wolfs.foreach: wolf =>
      val radius = wolf.radius.toInt
      val diameter = radius * 2
      val (borderX, borderY) = toScreenCenter(wolf.position.x, wolf.position.y, radius, offsetX, offsetY)
      g.setColor(Color.black)
      g.drawOval(borderX, borderY, diameter, diameter)
      val (innerX, innerY) = toScreenCenter(wolf.position.x, wolf.position.y, radius - innerOffset, offsetX, offsetY)
      g.setColor(Color.black)
      g.fillOval(innerX, innerY, diameter - innerBorder, diameter - innerBorder)
      val (labelX, labelY) = toScreenLabel(wolf.position.x, wolf.position.y, offsetX, offsetY)
      g.setColor(Color.white)
      g.drawString(wolf.id, labelX, labelY)

    world.sheep.foreach: sheep =>
      val radius = sheep.radius.toInt
      val diameter = radius * 2
      val (borderX, borderY) = toScreenCenter(sheep.position.x, sheep.position.y, radius, offsetX, offsetY)
      g.setColor(Color.white)
      g.drawOval(borderX, borderY, diameter, diameter)
      val (innerX, innerY) = toScreenCenter(sheep.position.x, sheep.position.y, radius - innerOffset, offsetX, offsetY)
      g.setColor(Color.white)
      g.fillOval(innerX, innerY, diameter - innerBorder, diameter - innerBorder)
      val (labelX, labelY) = toScreenLabel(sheep.position.x, sheep.position.y, offsetX, offsetY)
      g.setColor(Color.black)
      g.drawString(sheep.id, labelX, labelY)

