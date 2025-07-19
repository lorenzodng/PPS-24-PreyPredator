package view

import controller.EcosystemController
import view.panels.{ControlsPanel, StatusPanel, WorldPanel}
import scala.swing.*
import java.awt.Dimension

/**
 * The main window for the Ecosystem Simulation.
 * <p>
 * This class sets up the main layout of the application, integrating the [[ControlsPanel]], [[StatusPanel]], and [[WorldPanel]].
 * It also connects with the controller and delegates UI updates through a SimulationViewManager.
 * </p>
 */
class SimulationView(controller: EcosystemController) extends MainFrame:

  /**
   * The title of the main application window.
   */
  title = "PreyPredator Simulation"

  /**
   * The preferred initial size of the window before maximizing.
   */
  preferredSize = new Dimension(1300, 800)

  /**
   * Panel containing controls (spinners and buttons) for simulation configuration and interaction.
   */
  val controls = new ControlsPanel()

  /**
   * Panel displaying real-time statistics about wolves, sheep, and grass.
   */
  val status = new StatusPanel()

  /**
   * Panel responsible for rendering the simulation world and handling resize events.
   */
  val world = new WorldPanel((width, height) => handleResize(width, height))

  /**
   * Manager responsible for handling interactions between the controller and the UI components.
   */
  val manager = new SimulationViewManager(controller, controls, status, world)

  /**
   * Assembles the GUI layout using a BorderPanel.
   */
  contents = new BorderPanel:
    layout(controls.panel) = BorderPanel.Position.North
    layout(world) = BorderPanel.Position.Center
    layout(status.panel) = BorderPanel.Position.South

  /**
   * Opens and displays the main simulation window.
   */
  override def open(): Unit =
    peer.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH)
    peer.setLocationRelativeTo(null)
    super.open()

  /**
   * Handles window resize events by notifying the [[SimulationViewManager]] to resize the simulation world.
   *
   * @param width  the new width of the world panel.
   * @param height the new height of the world panel.
   */
  private def handleResize(width: Int, height: Int): Unit =
    manager.resizeWorld(width, height)