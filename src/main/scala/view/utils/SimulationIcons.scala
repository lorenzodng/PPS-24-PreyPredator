package view.utils

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Utility object that loads and stores the icons used for rendering entities (wolves, sheep, grass) in the simulation view.
 */
object SimulationIcons:

  /** Icon representing a wolf */
  val wolfIcon: BufferedImage = ImageIO.read(getClass.getResourceAsStream("/icons/wolf.png"))

  /** Icon representing a sheep */
  val sheepIcon: BufferedImage = ImageIO.read(getClass.getResourceAsStream("/icons/sheep.png"))
  
  /** Icon representing a grass patch */
  val grassIcon: BufferedImage = ImageIO.read(getClass.getResourceAsStream("/icons/grass.png"))

  

  
