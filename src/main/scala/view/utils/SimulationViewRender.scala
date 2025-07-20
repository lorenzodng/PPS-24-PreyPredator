package view.utils

import model.World
import view.utils.SimulationIcons.*
import java.awt.Graphics2D

/**
 * Utility object responsible for rendering the simulation world onto a graphical context.
 *
 * <p>
 * This object contains rendering logic to visually represent entities such as wolves, sheep, and grass using preloaded icons. 
 * It is intended to be used by the view layer of the application to display the current state of the simulation.
 */
object SimulationViewRender:

  /**
   * Renders all entities of the simulation world onto the given Graphics2D context.
   *
   * @param g       the Graphics2D object used to draw on the screen
   * @param world   the current state of the simulation world to be rendered
   * @param offsetX horizontal offset applied to all entities (used for panning)
   * @param offsetY vertical offset applied to all entities (used for panning)
   */
  def drawWorld(g: Graphics2D, world: World, offsetX: Double = 0, offsetY: Double = 0): Unit =

    def toScreenTopLeft(x: Double, y: Double, radius: Int, offsetX: Double, offsetY: Double): (Int, Int) =
      ((x - offsetX - radius).toInt, (y - offsetY - radius).toInt)

    world.grass.foreach: grass =>
      val radius = grass.radius().toInt
      val (x, y) = toScreenTopLeft(grass.position.x, grass.position.y, radius, offsetX, offsetY)
      g.drawImage(grassIcon, x, y, radius * 2, radius * 2, null)

    world.sheep.foreach: sheep =>
      val radius = sheep.radius().toInt
      val (x, y) = toScreenTopLeft(sheep.position.x, sheep.position.y, radius, offsetX, offsetY)
      g.drawImage(sheepIcon, x, y, radius * 2, radius * 2, null)

    world.wolves.foreach: wolf =>
      val radius = wolf.radius().toInt
      val (x, y) = toScreenTopLeft(wolf.position.x, wolf.position.y, radius, offsetX, offsetY)
      g.drawImage(wolfIcon, x, y, radius * 2, radius * 2, null)