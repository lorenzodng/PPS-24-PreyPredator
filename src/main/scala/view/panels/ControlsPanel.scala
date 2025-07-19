package view.panels

import java.awt.{Color, Dimension}
import javax.swing.{JSpinner, SpinnerNumberModel}
import scala.swing.*

class ControlsPanel:
  
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
   * Spinner for configuring the initial number of wolves in the simulation.
   */
  val wolvesSpinner: JSpinner = createSpinner(initialWolves, minWolves, maxWolves, stepWolves)

  /**
   * Spinner for configuring the initial number of sheep in the simulation.
   */
  val sheepSpinner: JSpinner = createSpinner(initialSheep, minSheep, maxSheep, stepSheep)

  /**
   * Spinner for configuring the initial amount of grass patches in the simulation.
   */
  val grassSpinner: JSpinner = createSpinner(initialGrass, minGrass, maxGrass, stepGrass)

  /**
   * Spinner for configuring the interval at which grass grows (in ticks).
   */
  val grassIntervalSpinner: JSpinner = createSpinner(initialGrassInterval, minGrassInterval, maxGrassInterval, stepGrassInterval)

  /**
   * Spinner for configuring the number of grass entities generated each growth cycle.
   */
  val grassGeneratedSpinner: JSpinner = createSpinner(initialGrassGenerated, minGrassGenerated, maxGrassGenerated, stepGrassGenerated)

  /**
   * Button to start the simulation. Enabled by default.
   */
  val startButton: Button = new Button("Start") {
    enabled = true
  }

  /**
   * Button to stop the simulation. Disabled by default.
   */
  val stopButton: Button = new Button("Stop") {
    enabled = false
  }

  /**
   * Button to reset the simulation to its initial state. Disabled by default.
   */
  val resetButton: Button = new Button("Reset") {
    enabled = false
  }

  /**
   * Panel containing spinner controls to configure simulation parameters.
   */
  private val spinnersRow = createSpinnersRow()

  /**
   * Panel containing the simulation control buttons.
   */
  private val buttonsRow = createButtonsRow()

  /**
   * Main panel that combines the spinners and buttons, organized vertically for inclusion in the main simulation view.
   */
  val panel: BoxPanel = createControlsPanel()

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
   * Creates a horizontal separator component.
   */
  private def makeSeparator(): Component =
    new Component:
      override lazy val peer = new javax.swing.JSeparator() {
        override def paintComponent(g: java.awt.Graphics): Unit =
          g.setColor(Color.BLACK)
          g.fillRect(0, 0, getWidth, 2)
      }
      preferredSize = new Dimension(1400, 1)
  
  
  