package model.managers

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import model.*
import model.entities.*
import zio.*

class EcosystemManagerTest extends AnyFunSuite with Matchers:

  val width = 100
  val height = 100
  val defaultPosition: Position = Position(10, 10)
  val defaultDx = 1
  val defaultDy = 0
  val zeroEnergy = 0
  val lowEnergy = 40
  val highEnergy = 60
  val runtime: Runtime[Any] = Runtime.default

  def createManager(world: World): EcosystemManager =
    val refWorld = runUnsafe(Ref.make(world))
    new EcosystemManager(refWorld)

  def runUnsafe[A](zio: UIO[A]): A =
    Unsafe.unsafe:
      implicit u => runtime.unsafe.run(zio).getOrThrowFiberFailure()

  test("Wolf moves after step"):
    val wolf = Wolf(EntityId.random, defaultPosition)
    val world = World(width, height, Seq(wolf), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(wolf.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        updatedWolf <- ZIO.succeed(updatedWorld.wolfById(wolf.id).get)
        _ <- ZIO.succeed(updatedWolf.position.x should not be wolf.position.x)
      yield ()

  test("Wolf loses energy after step"):
    val wolf = Wolf(EntityId.random, defaultPosition)
    val world = World(width, height, Seq(wolf), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(wolf.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        updatedWolf <- ZIO.succeed(updatedWorld.wolfById(wolf.id).get)
        _ <- ZIO.succeed(updatedWolf.energy should be < wolf.energy)
      yield ()

  test("Sheep moves after step"):
    val sheep = Sheep(EntityId.random, defaultPosition)
    val world = World(width, height, Seq.empty, Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(sheep.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        updatedSheep <- ZIO.succeed(updatedWorld.sheepById(sheep.id).get)
        _ <- ZIO.succeed(updatedSheep.position.x should not be sheep.position.x)
      yield ()

  test("Sheep loses energy after step"):
    val sheep = Sheep(EntityId.random, defaultPosition)
    val world = World(width, height, Seq.empty, Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(sheep.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        updatedSheep <- ZIO.succeed(updatedWorld.sheepById(sheep.id).get)
        _ <- ZIO.succeed(updatedSheep.energy should be < sheep.energy)
      yield ()

  test("Sheep is eaten"):
    val wolf = Wolf(EntityId.random, defaultPosition)
    val sheep = Sheep(EntityId.random, defaultPosition)
    val world = World(width, height, Seq(wolf), Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(wolf.id, defaultDx, defaultDy)
        _ <- ecosystemManager.moveEntityDirection(sheep.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        directions <- ZIO.succeed(ecosystemManager.getDirections)
        _ <- ZIO.succeed(updatedWorld.sheep.exists(_.id == sheep.id) shouldBe false)
      yield ()

  test("Sheep is not eaten"):
    val newPosition = Position(40, 40)
    val wolf = Wolf(EntityId.random, defaultPosition)
    val sheep = Sheep(EntityId.random, newPosition)
    val world = World(width, height, Seq(wolf), Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(wolf.id, defaultDx, defaultDy)
        _ <- ecosystemManager.moveEntityDirection(sheep.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        directions <- ZIO.succeed(ecosystemManager.getDirections)
        _ <- ZIO.succeed(updatedWorld.sheep.exists(_.id == sheep.id) shouldBe true)
      yield ()

  test("Wolves reproduce"):
    val wolf1 = Wolf(EntityId.random, defaultPosition, energy = highEnergy)
    val wolf2 = Wolf(EntityId.random, defaultPosition, energy = highEnergy)
    val world = World(width, height, Seq(wolf1, wolf2), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(wolf1.id, defaultDx, defaultDy)
        _ <- ecosystemManager.moveEntityDirection(wolf2.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed(updatedWorld.wolves.size shouldBe 3)
      yield ()

  test("Wolves do not reproduce"):
    val wolf1 = Wolf(EntityId.random, defaultPosition, energy = lowEnergy)
    val wolf2 = Wolf(EntityId.random, defaultPosition, energy = lowEnergy)
    val world = World(width, height, Seq(wolf1, wolf2), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(wolf1.id, defaultDx, defaultDy)
        _ <- ecosystemManager.moveEntityDirection(wolf2.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed(updatedWorld.wolves.size shouldBe 2)
      yield ()

  test("Wolves lose energy after reproduction"):
    val wolf1 = Wolf(EntityId.random, defaultPosition, energy = highEnergy)
    val wolf2 = Wolf(EntityId.random, defaultPosition, energy = highEnergy)
    val world = World(width, height, Seq(wolf1, wolf2), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(wolf1.id, defaultDx, defaultDy)
        _ <- ecosystemManager.moveEntityDirection(wolf2.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed:
          val expectedEnergy = 39.8
          val updatedWolf1 = updatedWorld.wolfById(wolf1.id).get
          val updatedWolf2 = updatedWorld.wolfById(wolf2.id).get
          updatedWolf1.energy should be(expectedEnergy)
          updatedWolf2.energy should be(expectedEnergy)
      yield ()

  test("Wolf dies"):
    val wolf = Wolf(EntityId.random, defaultPosition, energy = zeroEnergy)
    val world = World(width, height, Seq(wolf), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(wolf.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        directions <- ZIO.succeed(ecosystemManager.getDirections)
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed(updatedWorld.wolves.exists(_.id == wolf.id) shouldBe false)
      yield ()

  test("Grass is eaten"):
    val sheep = Sheep(EntityId.random, defaultPosition)
    val grass = Grass(EntityId.random, defaultPosition)
    val world = World(width, height, Seq.empty, Seq(sheep), Seq(grass))
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(sheep.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        directions <- ZIO.succeed(ecosystemManager.getDirections)
        _ <- ZIO.succeed(updatedWorld.grass.exists(_.id == grass.id) shouldBe false)
      yield ()

  test("Grass is not eaten"):
    val newPosition = Position(40, 40)
    val sheep = Sheep(EntityId.random, defaultPosition)
    val grass = Grass(EntityId.random, newPosition)
    val world = World(width, height, Seq.empty, Seq(sheep), Seq(grass))
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(sheep.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        directions <- ZIO.succeed(ecosystemManager.getDirections)
        _ <- ZIO.succeed(updatedWorld.grass.exists(_.id == grass.id) shouldBe true)
      yield ()

  test("Sheep reproduce"):
    val sheep1 = Sheep(EntityId.random, defaultPosition, energy = highEnergy)
    val sheep2 = Sheep(EntityId.random, defaultPosition, energy = highEnergy)
    val world = World(width, height, Seq.empty, Seq(sheep1, sheep2), Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(sheep1.id, defaultDx, defaultDy)
        _ <- ecosystemManager.moveEntityDirection(sheep2.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed(updatedWorld.sheep.size shouldBe 3)
      yield ()

  test("Sheep do not reproduce"):
    val sheep1 = Sheep(EntityId.random, defaultPosition, energy = lowEnergy)
    val sheep2 = Sheep(EntityId.random, defaultPosition, energy = lowEnergy)
    val world = World(width, height, Seq.empty, Seq(sheep1, sheep2), Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(sheep1.id, defaultDx, defaultDy)
        _ <- ecosystemManager.moveEntityDirection(sheep2.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed(updatedWorld.sheep.size shouldBe 2)
      yield ()

  test("Sheep lose energy after reproduction"):
    val sheep1 = Sheep(EntityId.random, defaultPosition, energy = highEnergy)
    val sheep2 = Sheep(EntityId.random, defaultPosition, energy = highEnergy)
    val world = World(width, height, Seq.empty, Seq(sheep1, sheep2), Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(sheep1.id, defaultDx, defaultDy)
        _ <- ecosystemManager.moveEntityDirection(sheep2.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed:
          val expectedEnergy = 39.8
          val updatedSheep1 = updatedWorld.sheepById(sheep1.id).get
          val updatedSheep2 = updatedWorld.sheepById(sheep2.id).get
          updatedSheep1.energy should be(expectedEnergy)
          updatedSheep2.energy should be(expectedEnergy)
      yield ()

  test("Sheep dies"):
    val sheep = Sheep(EntityId.random, defaultPosition, energy = zeroEnergy)
    val world = World(width, height, Seq.empty, Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.moveEntityDirection(sheep.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        directions <- ZIO.succeed(ecosystemManager.getDirections)
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed(updatedWorld.sheep.exists(_.id == sheep.id) shouldBe false)
      yield ()

  test("Grass generated"):
    val world = World(width, height, Seq.empty, Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    val grassCount = 100
    val grassInterval = 10
    val sleepTime = 1
    runUnsafe:
      for
        _ <- ecosystemManager.setGrassGenerated(grassCount)
        _ <- ecosystemManager.setGrassInterval(grassInterval, sleepTime)
        _ <- ZIO.foreachDiscard(1 to (grassInterval + 1))(_ => ecosystemManager.simulateTick())
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed(updatedWorld.grass.size should be > 0)
      yield ()

  test("Wolf separation after reproduction"):
    val wolf1 = Wolf(EntityId.random, defaultPosition, highEnergy)
    val wolf2 = Wolf(EntityId.random, defaultPosition, highEnergy)
    val world = World(width, height, Seq(wolf1, wolf2), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.setWorld(world)
        _ <- ecosystemManager.moveEntityDirection(wolf1.id, defaultDx, defaultDy)
        _ <- ecosystemManager.moveEntityDirection(wolf2.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed:
          val updatedWolf = updatedWorld.wolves.filter(w => w.id == wolf1.id || w.id == wolf2.id)
          val w1 = updatedWolf.head
          val w2 = updatedWolf(1)
          val expectedDistance = 20.0
          math.hypot(w1.position.x - w2.position.x, w1.position.y - w2.position.y) should be >= expectedDistance
      yield ()

  test("Sheep separation after reproduction"):
    val sheep1 = Sheep(EntityId.random, defaultPosition, highEnergy)
    val sheep2 = Sheep(EntityId.random, defaultPosition, highEnergy)
    val world = World(width, height, Seq.empty, Seq(sheep1, sheep2), Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.setWorld(world)
        _ <- ecosystemManager.moveEntityDirection(sheep1.id, defaultDx, defaultDy)
        _ <- ecosystemManager.moveEntityDirection(sheep2.id, defaultDx, defaultDy)
        _ <- ecosystemManager.simulateTick()
        updatedWorld <- ecosystemManager.getWorld
        _ <- ZIO.succeed:
          val updatedSheep = updatedWorld.sheep.filter(s => s.id == sheep1.id || s.id == sheep2.id)
          val s1 = updatedSheep.head
          val s2 = updatedSheep(1)
          val expectedDistance = 20.0
          math.hypot(s1.position.x - s2.position.x, s1.position.y - s2.position.y) should be >= expectedDistance
      yield ()

  test("Update wolf directions"):
    val wolf = Wolf(EntityId.random, defaultPosition)
    val world = World(width, height, Seq(wolf), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.setWorld(world)
        _ <- ecosystemManager.moveEntityDirection(wolf.id, defaultDx, defaultDy)
        _ <- ZIO.succeed(ecosystemManager.getDirections.get(wolf.id) should be(Some((defaultDx, defaultDy))))
      yield ()

  test("Update sheep directions"):
    val sheep = Sheep(EntityId.random, defaultPosition)
    val world = World(width, height, Seq.empty, Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        _ <- ecosystemManager.setWorld(world)
        _ <- ecosystemManager.moveEntityDirection(sheep.id, defaultDx, defaultDy)
        _ <- ZIO.succeed(ecosystemManager.getDirections.get(sheep.id) should be(Some((defaultDx, defaultDy))))
      yield ()

  test("All entities die"):
    val world = World(width, height, Seq.empty, Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    runUnsafe:
      for
        extinct <- ecosystemManager.simulateTick()
        _ <- ZIO.succeed(extinct shouldBe true)
      yield ()


