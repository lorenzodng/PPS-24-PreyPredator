package view

import model.EcosystemManager
import scala.swing.*

class SimulationView(ecosystemManager: EcosystemManager) extends MainFrame:

  title = "Ecosystem Simulation"
  preferredSize = new Dimension(600, 600)

  contents = new Panel:
    override def paintComponent(g: Graphics2D): Unit =
      val world = ecosystemManager.getWorld
      SimulationViewUtils.drawWorld(g, world)

