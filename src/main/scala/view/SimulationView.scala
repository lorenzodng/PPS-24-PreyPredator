package view

import controller.EcosystemController
import view.utils.SimulationViewRender
import zio.Runtime
import zio.Unsafe.unsafe

import java.awt.event.{ComponentAdapter, ComponentEvent}
import java.awt.{Color, Dimension, Graphics2D}
import javax.swing.{JSpinner, SpinnerNumberModel}
import scala.swing.*
import scala.swing.event.*

class SimulationView(ecosystemController: EcosystemController) extends MainFrame:

  //Parameters

  /**
   * Initial number of wolves in the simulation.
   */
  private val initialWolves = 10

  /**
   * Minimum allowed number of wolves.
   */
  private val minWolves = 0

  /**
   * Maximum allowed number of wolves.
   */
  private val maxWolves = 500

  /**
   * Increment step for wolves count adjustments.
   */
  private val stepWolves = 1

  /**
   * Initial number of sheep in the simulation.
   */
  private val initialSheep = 100

  /**
   * Minimum allowed number of sheep.
   */
  private val minSheep = 0

  /**
   * Maximum allowed number of sheep.
   */
  private val maxSheep = 500

  /**
   * Increment step for sheep count adjustments.
   */
  private val stepSheep = 1

  /**
   * Initial number of grass patches in the simulation.
   */
  private val initialGrass = 500

  /**
   * Minimum allowed number of grass patches.
   */
  private val minGrass = 0

  /**
   * Maximum allowed number of grass patches.
   */
  private val maxGrass = 1000

  /**
   * Increment step for grass count adjustments.
   */
  private val stepGrass = 1

  /**
   * Initial interval (in milliseconds) for grass regrowth.
   */
  private val initialGrassInterval = 2000

  /**
   * Minimum allowed grass regrowth interval.
   */
  private val minGrassInterval = 500

  /**
   * Maximum allowed grass regrowth interval.
   */
  private val maxGrassInterval = 5000

  /**
   * Increment step for grass regrowth interval adjustments.
   */
  private val stepGrassInterval = 500

  /**
   * Initial amount of grass generated at each interval.
   */
  private val initialGrassGenerated = 150

  /**
   * Minimum amount of grass generated at each interval.
   */
  private val minGrassGenerated = 20

  /**
   * Maximum amount of grass generated at each interval.
   */
  private val maxGrassGenerated = 300

  /**
   * Increment step for grass generation adjustments.
   */
  private val stepGrassGenerated = 10

  /**
   * The runtime environment for executing ZIO effects.
   */
  private val runtime = Runtime.default

  /**
   * Holds the current state of the ecosystem world, if any.
   */
  private var currentWorld: Option[model.World] = None

  // Components

  /**
   * Spinner for configuring the initial number of wolves in the simulation.
   */
  private val wolvesSpinner = createSpinner(initialWolves, minWolves, maxWolves, stepWolves)

  /**
   * Spinner for configuring the initial number of sheep in the simulation.
   */
  private val sheepSpinner = createSpinner(initialSheep, minSheep, maxSheep, stepSheep)

  /**
   * Spinner for configuring the initial amount of grass patches in the simulation.
   */
  private val grassSpinner = createSpinner(initialGrass, minGrass, maxGrass, stepGrass)

  /**
   * Spinner for configuring the interval at which grass grows (in ticks).
   */
  private val grassIntervalSpinner = createSpinner(initialGrassInterval, minGrassInterval, maxGrassInterval, stepGrassInterval)

  /**
   * Spinner for configuring the number of grass entities generated each growth cycle.
   */
  private val grassGeneratedSpinner = createSpinner(initialGrassGenerated, minGrassGenerated, maxGrassGenerated, stepGrassGenerated)

  /**
   * Button to start the simulation. Enabled by default.
   */
  private val startButton = new Button("Start") {
    enabled = true
  }

  /**
   * Button to stop the simulation. Disabled by default.
   */
  private val stopButton = new Button("Stop") {
    enabled = false
  }

  /**
   * Button to reset the simulation to its initial state. Disabled by default.
   */
  private val resetButton = new Button("Reset") {
    enabled = false
  }

  /**
   * Label showing the current count of wolves in the simulation.
   */
  private val wolfCountLabel = new Label("Wolves: 0")

  /**
   * Label showing the current count of sheep in the simulation.
   */
  private val sheepCountLabel = new Label("Sheep: 0")

  /**
   * Label showing the current count of grass patches in the simulation.
   */
  private val grassCountLabel = new Label("Grass: 0")

  /**
   * Panel responsible for rendering the simulation world.
   * It visually displays entities such as wolves, sheep, and grass.
   */
  private val worldPanel = createWorldPanel()

  /**
   * Row panel containing all configuration spinners for entity counts and grass growth settings.
   */
  private val spinnersRow = createSpinnersRow()

  /**
   * Row panel containing buttons for simulation control (start, stop, reset).
   */
  private val buttonsRow = createButtonsRow()

  /**
   * Container panel that organizes the control components such as spinners and buttons.
   */
  private val controlsPanel = createControlsPanel()

  /**
   * Panel that displays the current count of wolves, sheep, and grass in the simulation.
   */
  private val statusPanel = createStatusPanel()

  // Initialization

  /**
   * Initializes the main UI layout, setting title, size, and panel arrangement.
   */
  initUI()

  /**
   * Sets up event listeners for the UI components, handling user interactions.
   */
  setupListeners()

  // UI Builders

  /**
   * Creates a configured spinner component with specified initial value, bounds, and step.
   *
   * @param initial the initial value
   * @param min     the minimum allowed value
   * @param max     the maximum allowed value
   * @param step    the increment step
   * @return a configured JSpinner
   */
  private def createSpinner(initial: Int, min: Int, max: Int, step: Int): JSpinner =
    val spinner = new JSpinner(new SpinnerNumberModel(initial, min, max, step))
    val editor = spinner.getEditor.asInstanceOf[javax.swing.JSpinner.DefaultEditor]
    editor.getTextField.setColumns(5)
    spinner.setPreferredSize(new Dimension(80, 20))
    spinner

  /**
   * Builds the row panel containing all the entity and grass configuration spinners.
   *
   * @return a FlowPanel with spinner components
   */
  private def createSpinnersRow(): FlowPanel =
    new FlowPanel(FlowPanel.Alignment.Left)(
      new Label("Wolves: "), Component.wrap(wolvesSpinner), Swing.HStrut(10),
      new Label("Sheep: "), Component.wrap(sheepSpinner), Swing.HStrut(10),
      new Label("Grass: "), Component.wrap(grassSpinner), Swing.HStrut(10),
      new Label("Grass Interval (ms): "), Component.wrap(grassIntervalSpinner), Swing.HStrut(10),
      new Label("Grass Generated: "), Component.wrap(grassGeneratedSpinner)
    ) { background = new Color(230, 230, 230) }

  /**
   * Builds the row panel containing the simulation control buttons (start, stop, reset).
   *
   * @return a FlowPanel with control buttons
   */
  private def createButtonsRow(): FlowPanel =
    new FlowPanel(FlowPanel.Alignment.Center)(
      startButton, Swing.HStrut(10), stopButton, Swing.HStrut(10), resetButton
    ) { background = new Color(230, 230, 230) }

  /**
   * Builds the control panel that organizes spinners and buttons vertically.
   *
   * @return a BoxPanel containing the controls
   */
  private def createControlsPanel(): BoxPanel =
    new BoxPanel(Orientation.Vertical):
      background = new Color(230, 230, 230)
      contents ++= Seq(spinnersRow, Swing.VStrut(4), makeSeparator(), Swing.VStrut(4), buttonsRow)

  /**
   * Builds the status panel showing current entity counts in the simulation.
   *
   * @return a FlowPanel displaying labels for wolves, sheep, and grass
   */
  private def createStatusPanel(): FlowPanel =
    val fontSize = 13f
    wolfCountLabel.font = wolfCountLabel.font.deriveFont(fontSize)
    sheepCountLabel.font = sheepCountLabel.font.deriveFont(fontSize)
    grassCountLabel.font = grassCountLabel.font.deriveFont(fontSize)
    new FlowPanel(FlowPanel.Alignment.Center)(wolfCountLabel, Swing.HStrut(20), sheepCountLabel, Swing.HStrut(20), grassCountLabel)

  /**
   * Builds the panel that renders the world grid and listens for resize events to adjust the world size.
   *
   * @return a Panel responsible for drawing the simulation state
   */
  private def createWorldPanel(): Panel =
    new Panel:
      background = Color.WHITE
      override def paintComponent(g: Graphics2D): Unit =
        super.paintComponent(g)
        currentWorld.foreach(SimulationViewRender.drawWorld(g, _))
      peer.addComponentListener(new ComponentAdapter():
        override def componentResized(e: ComponentEvent): Unit =
          updateWorldSize(size.width, size.height)
      )

  /**
   * Creates a horizontal separator component.
   */
  private def makeSeparator(): Component =
    new Component:
      override lazy val peer: javax.swing.JComponent = new javax.swing.JSeparator() {
        override def paintComponent(g: java.awt.Graphics): Unit =
          g.setColor(Color.BLACK)
          g.fillRect(0, 0, getWidth, 2)
      }
      preferredSize = new Dimension(1400, 1)

  /**
   * Initializes the main user interface of the simulation window.
   * Sets the window title, preferred size, and arranges the main panels (controls, world view, status).
   */
  private def initUI(): Unit =
    title = "Ecosystem Simulation"
    preferredSize = new Dimension(1300, 800)
    contents = new BorderPanel:
      layout(controlsPanel) = BorderPanel.Position.North
      layout(worldPanel) = BorderPanel.Position.Center
      layout(statusPanel) = BorderPanel.Position.South

  /**
   * Sets up event listeners for simulation control buttons (start, stop, reset).
   * Defines reactions to button clicks and delegates to corresponding handlers.
   */
  private def setupListeners(): Unit =
    listenTo(startButton, stopButton, resetButton)
    reactions += {
      case ButtonClicked(`startButton`) => handleStart()
      case ButtonClicked(`stopButton`)  => handleStop()
      case ButtonClicked(`resetButton`) => handleReset()
    }

  // Logic Handlers

  /**
   * Handles the logic for starting the simulation.
   * Reads configuration values from the UI spinners, validates inputs, and triggers the ecosystem controller to start the simulation.
   * Also updates UI component states accordingly.
   */
  private def handleStart(): Unit =
    val nWolves = wolvesSpinner.getValue.asInstanceOf[Int]
    val nSheep = sheepSpinner.getValue.asInstanceOf[Int]
    val nGrass = grassSpinner.getValue.asInstanceOf[Int]
    val nGrassInterval = grassIntervalSpinner.getValue.asInstanceOf[Int]
    val nGrassGenerated = grassGeneratedSpinner.getValue.asInstanceOf[Int]
    val width = worldPanel.size.width
    val height = worldPanel.size.height
    if (nWolves + nSheep) == 0 then
      javax.swing.JOptionPane.showMessageDialog(peer, "Select at least one wolf or sheep", "Configuration error", javax.swing.JOptionPane.WARNING_MESSAGE)
    else unsafe:
      implicit u =>
        runtime.unsafe.run(ecosystemController.startSimulation(nWolves, nSheep, nGrass, nGrassInterval, nGrassGenerated, width, height))
        startButton.enabled = false
        stopButton.enabled = true
        resetButton.enabled = false
        wolvesSpinner.setEnabled(false)
        sheepSpinner.setEnabled(false)
        grassSpinner.setEnabled(false)
        grassIntervalSpinner.setEnabled(false)
        grassGeneratedSpinner.setEnabled(false)

  /**
   * Handles the logic for stopping the simulation.
   * Calls the ecosystem controller to stop the simulation and updates UI button states.
   */
  private def handleStop(): Unit =
    unsafe:
      implicit u =>
        runtime.unsafe.run(ecosystemController.stopSimulation())
    startButton.enabled = true
    stopButton.enabled = false
    resetButton.enabled = true

  /**
   * Handles the logic for resetting the simulation to its initial state.
   * Calls the ecosystem controller to reset and updates the view and UI components.
   */
  private def handleReset(): Unit =
    unsafe:
      implicit u =>
        runtime.unsafe.run(ecosystemController.resetSimulation())
        updateView()
    startButton.enabled = true
    wolvesSpinner.setEnabled(true)
    sheepSpinner.setEnabled(true)
    grassSpinner.setEnabled(true)
    grassIntervalSpinner.setEnabled(true)
    grassGeneratedSpinner.setEnabled(true)
    resetButton.enabled = false

  /**
   * Updates the internal model size when the window is resized.
   */
  private def updateWorldSize(width: Int, height: Int): Unit =
    unsafe:
      implicit u =>
        runtime.unsafe.run(ecosystemController.resizeWorld(width, height))
        updateView()

  /**
   * Updates the button and spinner states for when a simulation is active.
   */
  def updateButtons(): Unit =
    startButton.enabled = false
    stopButton.enabled = false
    resetButton.enabled = true
    wolvesSpinner.setEnabled(false)
    sheepSpinner.setEnabled(false)
    grassSpinner.setEnabled(false)
    grassIntervalSpinner.setEnabled(false)
    grassGeneratedSpinner.setEnabled(false)

  /**
   * Updates the visual world and statistics shown on screen.
   */
  def updateView(): Unit =
    unsafe:
      implicit u =>
        val w = runtime.unsafe.run(ecosystemController.ecosystemManager.getWorld).getOrThrowFiberFailure()
        scala.swing.Swing.onEDT:
          currentWorld = Some(w)
          wolfCountLabel.text = s"Wolves: ${w.wolves.size}"
          sheepCountLabel.text = s"Sheep: ${w.sheep.size}"
          grassCountLabel.text = s"Grass: ${w.grass.size}"
          worldPanel.repaint()

  /**
   * Opens the main window maximized and centered on screen.
   */
  override def open(): Unit =
    peer.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH)
    peer.setLocationRelativeTo(null)
    super.open()
