package view

import controller.EcosystemController
import view.panels.{ControlsPanel, StatusPanel, WorldPanel}
import zio.Runtime
import zio.Unsafe.unsafe

import scala.swing.MainFrame
import scala.swing.event.ButtonClicked

/**
 * Acts as a bridge between the simulation controller and the Swing-based UI components.
 * <p>
 * It handles user interactions from the control panel, delegates actions to the controller, and updates the view accordingly.
 * </p>
 *
 * @param controller the controller handling simulation logic
 * @param controls   the panel containing input controls and buttons
 * @param status     the panel displaying current simulation statistics
 * @param worldPanel the panel rendering the simulation world
 */
class SimulationViewManager(controller: EcosystemController, controls: ControlsPanel, status: StatusPanel, worldPanel: WorldPanel) extends MainFrame:

  private val runtime = Runtime.default

  setupListeners()

  /**
   * Sets up event listeners for simulation control buttons (Start, Stop, Reset).
   * Defines reactions to button clicks and delegates to corresponding handlers.
   */
  private def setupListeners(): Unit =
    listenTo(controls.startButton, controls.stopButton, controls.resetButton)
    reactions += {
      case ButtonClicked(controls.startButton) => handleStart()
      case ButtonClicked(controls.stopButton)  => handleStop()
      case ButtonClicked(controls.resetButton) => handleReset()
    }

  /**
   * Handles the Start button click event.
   * Reads input values from the controls, validates them, and starts the simulation by delegating to the controller.
   * Updates the UI buttons accordingly.
   */
  private def handleStart(): Unit =
    val nWolves = controls.wolvesSpinner.getValue.asInstanceOf[Int]
    val nSheep = controls.sheepSpinner.getValue.asInstanceOf[Int]
    val nGrass = controls.grassSpinner.getValue.asInstanceOf[Int]
    val nGrassInterval = controls.grassIntervalSpinner.getValue.asInstanceOf[Int]
    val nGrassGenerated = controls.grassGeneratedSpinner.getValue.asInstanceOf[Int]
    val width = worldPanel.size.width
    val height = worldPanel.size.height

    if nWolves + nSheep == 0 then
      javax.swing.JOptionPane.showMessageDialog(null, "Select at least one wolf or sheep", "Configuration error", javax.swing.JOptionPane.WARNING_MESSAGE)
    else unsafe:
      implicit u =>
        runtime.unsafe.run(controller.startSimulation(nWolves, nSheep, nGrass, nGrassInterval, nGrassGenerated, width, height))
        updateButtonsOnStart()

  /**
   * Handles the Stop button click event.
   * Delegates to the controller to stop the simulation.
   * Updates the UI buttons accordingly.
   */
  private def handleStop(): Unit =
    unsafe:
      implicit u =>
        runtime.unsafe.run(controller.stopSimulation())
        updateButtonsOnStop()

  /**
   * Handles the Reset button click event.
   * Delegates to the controller to reset the simulation.
   * Updates the UI buttons and refreshes the view.
   */
  private def handleReset(): Unit =
    unsafe:
      implicit u =>
        runtime.unsafe.run(controller.resetSimulation())
        updateButtonsOnReset()
        updateView()

  /**
   * Updates the control buttons state after starting the simulation.
   */
  private def updateButtonsOnStart(): Unit =
    controls.startButton.enabled = false
    controls.stopButton.enabled = true
    controls.resetButton.enabled = false
    disableSpinners()

  /**
   * Updates the control buttons state after stopping the simulation.
   */
  private def updateButtonsOnStop(): Unit =
    controls.startButton.enabled = true
    controls.stopButton.enabled = false
    controls.resetButton.enabled = true
    disableSpinners()

  /**
   * Updates the control buttons state after resetting the simulation.
   */
  private def updateButtonsOnReset(): Unit =
    controls.startButton.enabled = true
    controls.stopButton.enabled = false
    controls.resetButton.enabled = false
    enableSpinners()

  /**
   * Disables all the spinner controls to prevent input changes during simulation run.
   */
  private def disableSpinners(): Unit =
    controls.wolvesSpinner.setEnabled(false)
    controls.sheepSpinner.setEnabled(false)
    controls.grassSpinner.setEnabled(false)
    controls.grassIntervalSpinner.setEnabled(false)
    controls.grassGeneratedSpinner.setEnabled(false)

  /**
   * Enables all the spinner controls to allow user input.
   */
  private def enableSpinners(): Unit =
    controls.wolvesSpinner.setEnabled(true)
    controls.sheepSpinner.setEnabled(true)
    controls.grassSpinner.setEnabled(true)
    controls.grassIntervalSpinner.setEnabled(true)
    controls.grassGeneratedSpinner.setEnabled(true)

  /**
   * Updates the control buttons and spinner states when the simulation ends due to extinction of wolves and sheep.
   */
  def updateButtons(): Unit =
    controls.startButton.enabled = false
    controls.stopButton.enabled = false
    controls.resetButton.enabled = true
    controls.wolvesSpinner.setEnabled(false)
    controls.sheepSpinner.setEnabled(false)
    controls.grassSpinner.setEnabled(false)
    controls.grassIntervalSpinner.setEnabled(false)
    controls.grassGeneratedSpinner.setEnabled(false)

  /**
   * Handles resizing of the simulation world.
   * Delegates the new dimensions to the controller and refreshes the view.
   *
   * @param width  the new width of the simulation world panel
   * @param height the new height of the simulation world panel
   */
  def onWorldResize(width: Int, height: Int): Unit =
    unsafe:
      implicit u =>
        runtime.unsafe.run(controller.resizeWorld(width, height))
        updateView()

  /**
   * Updates the view components to reflect the current state of the simulation.
   */
  def updateView(): Unit =
    unsafe:
      implicit u =>
        val w = runtime.unsafe.run(controller.ecosystemManager.getWorld).getOrThrowFiberFailure()
        scala.swing.Swing.onEDT:
          status.updateCounts(w.wolves.size, w.sheep.size, w.grass.size)
          worldPanel.updateWorld(w)

