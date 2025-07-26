package view.panels

import model.World
import view.utils.SimulationViewRender
import java.awt.event.{ComponentAdapter, ComponentEvent}
import java.awt.{Color, Graphics2D}
import scala.swing.*

/**
 * Panel responsible for rendering the simulation world.
 *
 * It listens for resize events and triggers a callback with the new dimensions.
 */
class WorldPanel(onResize: (Int, Int) => Unit) extends Panel:

  /**
   * Holds the current state of the world to be rendered.
   */
  private var currentWorld: Option[World] = None

  /**
   * Sets the background color of the panel to white.
   */
  background = Color.WHITE

  /**
   * Paints the component by delegating to SimulationViewRender to draw the current world.
   *
   * @param g the graphics context to use for painting
   */
  override def paintComponent(g: Graphics2D): Unit =
    super.paintComponent(g)
    currentWorld.foreach(SimulationViewRender.drawWorld(g, _))

  /**
   * Listens to component resize events and calls the callback with updated dimensions.
    */
  peer.addComponentListener(new ComponentAdapter():
    override def componentResized(e: ComponentEvent): Unit =
      onResize(size.width, size.height)
  )

  /**
   * Updates the panel with a new world state and triggers a repaint.
   *
   * @param world the new world state to display
   */
  def updateWorld(world: World): Unit =
    currentWorld = Some(world)
    repaint()
