package view

import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import scala.swing.*
import scala.swing.event.*
import java.awt.{Color, Dimension, Graphics2D}
import controller.EcosystemController
import javax.swing.{JSpinner, SpinnerNumberModel}
import zio.Unsafe.unsafe
import zio.Runtime

class SimulationView(ecosystemController: EcosystemController) extends MainFrame:

  private val InitialWolves = 10
  private val MinWolves = 0
  private val MaxWolves = 500
  private val StepWolves = 1
  private val InitialSheep = 100
  private val MinSheep = 0
  private val MaxSheep = 500
  private val StepSheep = 1
  private val InitialGrass = 500
  private val MinGrass = 0
  private val MaxGrass = 1000
  private val StepGrass = 1
  private val InitialGrassInterval = 2000
  private val MinGrassInterval = 500
  private val MaxGrassInterval = 5000
  private val StepGrassInterval = 500
  private val InitialGrassGenerated = 100
  private val MinGrassGenerated = 20
  private val MaxGrassGenerated = 300
  private val StepGrassGenerated = 10

  title = "Ecosystem Simulation"
  preferredSize = new Dimension(1300, 800)
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

  private val wolvesSpinner = createCompactSpinner(InitialWolves, MinWolves, MaxWolves, StepWolves)
  private val sheepSpinner = createCompactSpinner(InitialSheep, MinSheep, MaxSheep, StepSheep)
  private val grassSpinner = createCompactSpinner(InitialGrass, MinGrass, MaxGrass, StepGrass)
  private val grassIntervalSpinner = createCompactSpinner(InitialGrassInterval, MinGrassInterval, MaxGrassInterval, StepGrassInterval)
  private val grassGeneratedSpinner = createCompactSpinner(InitialGrassGenerated, MinGrassGenerated, MaxGrassGenerated, StepGrassGenerated)

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

  worldPanel.peer.addComponentListener(new ComponentAdapter() {
    override def componentResized(e: ComponentEvent): Unit =
      val width = worldPanel.size.width
      val height = worldPanel.size.height
      updateWorldSize(width, height)
  })

  private val spinnersRow = new FlowPanel(FlowPanel.Alignment.Left)(
    new Label(" Wolves: "),
    Component.wrap(wolvesSpinner),
    Swing.HStrut(10),
    new Label("Sheep: "),
    Component.wrap(sheepSpinner),
    Swing.HStrut(10),
    new Label("Grass: "),
    Component.wrap(grassSpinner),
    Swing.HStrut(10),
    new Label("Grass Interval (ms): "),
    Component.wrap(grassIntervalSpinner),
    Swing.HStrut(10),
    new Label("Grass Generated: "),
    Component.wrap(grassGeneratedSpinner)
  )
  spinnersRow.background = new Color(230, 230, 230)

  private val buttonsRow = new FlowPanel(FlowPanel.Alignment.Center)(
    startButton,
    Swing.HStrut(10),
    stopButton,
    Swing.HStrut(10),
    resetButton
  )
  buttonsRow.background = new Color(230, 230, 230)

  private val controlsPanel = new BoxPanel(Orientation.Vertical):
    background = new Color(230, 230, 230)
    contents += spinnersRow
    contents += Swing.VStrut(4)
    contents += new Component:
      override lazy val peer: javax.swing.JComponent = new javax.swing.JSeparator():
        override def paintComponent(g: java.awt.Graphics): Unit =
          g.setColor(Color.BLACK)
          g.fillRect(0, 0, getWidth, 2)
      preferredSize = new Dimension(1400, 1)
    contents += Swing.VStrut(4)
    contents += buttonsRow

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
        val nGrassInterval = grassIntervalSpinner.getValue.asInstanceOf[Int]
        val nGrassGenerated = grassGeneratedSpinner.getValue.asInstanceOf[Int]

        val width = worldPanel.size.width
        val height = worldPanel.size.height

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
              runtime.unsafe.run(
                ecosystemController.startSimulation(nWolves, nSheep, nGrass, nGrassInterval, nGrassGenerated, width, height)
              )
              startButton.enabled = false
              stopButton.enabled = true
              resetButton.enabled = false
              wolvesSpinner.setEnabled(false)
              sheepSpinner.setEnabled(false)
              grassSpinner.setEnabled(false)
              grassIntervalSpinner.setEnabled(false)
              grassGeneratedSpinner.setEnabled(false)

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
        grassIntervalSpinner.setEnabled(true)
        grassGeneratedSpinner.setEnabled(true)
        resetButton.enabled = false
  }

  def updateButtons(): Unit =
    startButton.enabled = false
    stopButton.enabled = false
    resetButton.enabled = true
    wolvesSpinner.setEnabled(false)
    sheepSpinner.setEnabled(false)
    grassSpinner.setEnabled(false)
    grassIntervalSpinner.setEnabled(false)
    grassGeneratedSpinner.setEnabled(false)

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

  private def updateWorldSize(width: Int, height: Int): Unit =
    unsafe:
      implicit u =>
        runtime.unsafe.run(
          ecosystemController.resizeWorld(width, height)
        )
        updateView()

  override def open(): Unit =
    peer.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH)
    peer.setLocationRelativeTo(null)
    super.open()

