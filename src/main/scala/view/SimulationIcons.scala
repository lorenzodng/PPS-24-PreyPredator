package view

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object SimulationIcons:
  val grassIcon: BufferedImage = ImageIO.read(getClass.getResourceAsStream("/icons/grass.png"))
  val sheepIcon: BufferedImage = ImageIO.read(getClass.getResourceAsStream("/icons/sheep.png"))
  val wolfIcon: BufferedImage = ImageIO.read(getClass.getResourceAsStream("/icons/wolf.png"))

