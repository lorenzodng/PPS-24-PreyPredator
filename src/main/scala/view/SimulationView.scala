package view

import scala.swing.*
import scala.swing.event.*
import java.awt.{Color, Dimension, Graphics2D}
import controller.{EcosystemController, Flag}
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
    val fixedSize = new Dimension(80, 20)
    spinner.setMinimumSize(fixedSize)
    spinner.setPreferredSize(fixedSize)
    spinner.setMaximumSize(fixedSize)
    spinner

  private val wolvesSpinner = createCompactSpinner(10, 0, 500, 1)
  private val sheepSpinner = createCompactSpinner(100, 0, 500, 1)
  private val grassSpinner = createCompactSpinner(500, 0, 1000, 1)
  private val startButton = new Button("Start") { enabled = true }
  private val stopButton = new Button("Stop") { enabled = false }
  private val resetButton = new Button("Reset") { enabled = false }
  private val wolfCountLabel = new Label("Wolves: 0")
  private val sheepCountLabel = new Label("Sheep: 0")
  private val grassCountLabel = new Label("Grass: 0")
  wolfCountLabel.font = wolfCountLabel.font.deriveFont(13f)
  sheepCountLabel.font = sheepCountLabel.font.deriveFont(13f)
  grassCountLabel.font = grassCountLabel.font.deriveFont(13f)

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

  private val statusPanel = new FlowPanel(FlowPanel.Alignment.Center)(
    wolfCountLabel,
    Swing.HStrut(20),
    sheepCountLabel,
    Swing.HStrut(20),
    grassCountLabel
  )

  contents = new BorderPanel:
    layout(controlsPanel) = BorderPanel.Position.North
    layout(worldPanel) = BorderPanel.Position.Center
    layout(statusPanel) = BorderPanel.Position.South

  listenTo(startButton, stopButton, resetButton)

  reactions += {
    case ButtonClicked(b) =>
      if b eq startButton then
        val nWolves = wolvesSpinner.getValue.asInstanceOf[Int]
        val nSheep = sheepSpinner.getValue.asInstanceOf[Int]
        val nGrass = grassSpinner.getValue.asInstanceOf[Int]
        if (nWolves + nSheep) == 0 then
          javax.swing.JOptionPane.showMessageDialog(
            peer,
            "Select at least one wolf or sheep",
            "Configuration error",
            javax.swing.JOptionPane.WARNING_MESSAGE
          )
        else
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

        startButton.enabled = true
        wolvesSpinner.setEnabled(true)
        sheepSpinner.setEnabled(true)
        grassSpinner.setEnabled(true)
        resetButton.enabled = false
  }

  def updateButtons(): Unit =
    startButton.enabled = false
    stopButton.enabled = false
    resetButton.enabled = true
    wolvesSpinner.setEnabled(false)
    sheepSpinner.setEnabled(false)
    grassSpinner.setEnabled(false)

  def updateView(): Unit =
    unsafe:
      implicit u =>
        val w = runtime.unsafe.run(ecosystemController.ecosystemManager.getWorld).getOrThrowFiberFailure()
        scala.swing.Swing.onEDT:
          currentWorld = Some(w)
          val wolfCount = w.wolves.size
          val sheepCount = w.sheep.size
          val grassCount = w.grass.size
          wolfCountLabel.text = s"Wolves: $wolfCount"
          sheepCountLabel.text = s"Sheep: $sheepCount"
          grassCountLabel.text = s"Grass: $grassCount"
          worldPanel.repaint()
