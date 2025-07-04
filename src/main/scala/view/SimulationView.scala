package view

import scala.swing.*
import scala.swing.event.*
import java.awt.{Color, Dimension, Graphics2D}
import controller.EcosystemController
import javax.swing.{JSpinner, SpinnerNumberModel}
import zio.Unsafe.unsafe
import zio.Runtime

class SimulationView(ecosystemController: EcosystemController) extends MainFrame:

  title = "Ecosystem Simulation"
  preferredSize = new Dimension(800, 800)
  private val runtime = Runtime.default
  private var currentWorld: Option[model.World] = None

  private def createCompactSpinner(initial: Int, min: Int, max: Int, step: Int): JSpinner =
    val spinner = new JSpinner(new SpinnerNumberModel(initial, min, max, step))
    val editor = spinner.getEditor.asInstanceOf[javax.swing.JSpinner.DefaultEditor]
    editor.getTextField.setColumns(5)
    val fixedSize = new Dimension(75, 20)
    spinner.setMinimumSize(fixedSize)
    spinner.setPreferredSize(fixedSize)
    spinner.setMaximumSize(fixedSize)
    spinner

  private val wolvesSpinner = createCompactSpinner(10, 0, 500, 1)
  private val sheepSpinner = createCompactSpinner(100, 0, 500, 1)
  private val grassSpinner = createCompactSpinner(200, 0, 500, 1)
  private val startButton = new Button("Start") { enabled = true }
  private val stopButton = new Button("Stop") { enabled = false }
  private val resetButton = new Button("Reset") { enabled = false }

  private val worldPanel = new Panel:
    background = Color.WHITE
    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      currentWorld match
        case Some(world) => SimulationViewUtils.drawWorld(g, world)
        case None => ()

  private val controlsPanel = new BoxPanel(Orientation.Horizontal):
    background = new Color(230, 230, 230)
    contents += new Label(" Wolves: ")
    contents += Component.wrap(wolvesSpinner)
    contents += Swing.HStrut(10)
    contents += new Label("Sheep: ")
    contents += Component.wrap(sheepSpinner)
    contents += Swing.HStrut(10)
    contents += new Label("Grass: ")
    contents += Component.wrap(grassSpinner)
    contents += Swing.HGlue
    contents += startButton
    contents += stopButton
    contents += resetButton

  contents = new BorderPanel:
    layout(controlsPanel) = BorderPanel.Position.North
    layout(worldPanel) = BorderPanel.Position.Center

  listenTo(startButton, stopButton, resetButton)

  reactions += {
    case ButtonClicked(b) =>
      if b eq startButton then
        val nWolves = wolvesSpinner.getValue.asInstanceOf[Int]
        val nSheep = sheepSpinner.getValue.asInstanceOf[Int]
        val nGrass = grassSpinner.getValue.asInstanceOf[Int]
        unsafe:
          implicit u =>
            runtime.unsafe.run(ecosystemController.startSimulation(nWolves, nSheep, nGrass))
        startButton.enabled = false
        stopButton.enabled = true
        resetButton.enabled = false
        wolvesSpinner.setEnabled(false)
        sheepSpinner.setEnabled(false)
        grassSpinner.setEnabled(false)

      else if b eq stopButton then
        unsafe:
          implicit u =>
            runtime.unsafe.run(ecosystemController.stopSimulation())
        startButton.enabled = true
        stopButton.enabled = false
        resetButton.enabled = true

      else if b eq resetButton then
        unsafe:
          implicit u =>
            runtime.unsafe.run(ecosystemController.resetSimulation())
            updateView()
        
        wolvesSpinner.setEnabled(true)
        sheepSpinner.setEnabled(true)
        grassSpinner.setEnabled(true)
        resetButton.enabled = false
  }

  def updateView(): Unit =
    unsafe:
      implicit u =>
        val w = runtime.unsafe.run(ecosystemController.ecosystemManager.getWorld).getOrThrowFiberFailure()
        scala.swing.Swing.onEDT:
          currentWorld = Some(w)
          worldPanel.repaint()

