package view.panels

import scala.swing.*

/**
 * Panel that displays the current counts of wolves, sheep, and grass in the simulation.
 *
 * It provides a simple UI component with labels that update dynamically to reflect the current state of the ecosystem.
 */
class StatusPanel:

  /**
   * Label displaying the current number of wolves.
   */
  private val wolfCountLabel = new Label("Wolves: 0")

  /**
   * Label displaying the current number of sheep.
   */
  private val sheepCountLabel = new Label("Sheep: 0")

  /**
   * Label displaying the current number of grass patches.
   */
  private val grassCountLabel = new Label("Grass: 0")

  /**
   * The main panel containing the count labels.
   */
  val panel: FlowPanel = createStatusPanel()

  /**
   * Creates the status panel containing the labels for wolves, sheep, and grass counts.
   *
   * @return a FlowPanel containing the count labels
   */
  private def createStatusPanel(): FlowPanel =
    val fontSize = 13f
    wolfCountLabel.font = wolfCountLabel.font.deriveFont(fontSize)
    sheepCountLabel.font = sheepCountLabel.font.deriveFont(fontSize)
    grassCountLabel.font = grassCountLabel.font.deriveFont(fontSize)
    new FlowPanel(FlowPanel.Alignment.Center)(
      wolfCountLabel, Swing.HStrut(20), sheepCountLabel, Swing.HStrut(20), grassCountLabel
    )
  
  /**
   * Updates the displayed counts of wolves, sheep, and grass.
   *
   * @param wolves the current number of wolves in the simulation
   * @param sheep  the current number of sheep in the simulation
   * @param grass  the current number of grass patches in the simulation
   */
  def updateCounts(wolves: Int, sheep: Int, grass: Int): Unit =
    wolfCountLabel.text = s"Wolves: $wolves"
    sheepCountLabel.text = s"Sheep: $sheep"
    grassCountLabel.text = s"Grass: $grass"
