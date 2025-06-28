package view

import model.EcosystemManager
import scala.swing.*

class EcosystemView(ecosystemManager: EcosystemManager) extends MainFrame:

  title = "Ecosystem View"
  preferredSize = new Dimension(600, 600)

  contents = new Panel:
    override def paintComponent(g: Graphics2D): Unit =
      val world = ecosystemManager.getWorld
      SimulationViewUtils.drawWorld(g, world)

