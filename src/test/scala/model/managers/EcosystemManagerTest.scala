package model.managers

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import model._
import model.entities._
import zio._

class EcosystemManagerTest extends AnyFunSuite with Matchers:

  val Width = 100
  val Height = 100
  val DefaultPosition: Position = Position(10, 10)
  val DefaultDx = 1
  val DefaultDy = 0
  val ZeroEnergy = 0
  val LowEnergy = 40
  val HighEnergy = 60
  
  val runtime: Runtime[Any] = Runtime.default
  def createManager(world: World): EcosystemManager =
    val refWorld = Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run(Ref.make(world)).getOrThrowFiberFailure()
    new EcosystemManager(refWorld)

  test("Wolf updated after tick"):
    val wolf = Wolf(EntityId.random, DefaultPosition)
    val world = World(Width, Height, Seq(wolf), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(wolf.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            updatedWolf <- ZIO.succeed(updatedWorld.wolfById(wolf.id).get)
            _ <- ZIO.succeed(updatedWolf.position.x should not be wolf.position.x)
            _ <- ZIO.succeed(updatedWolf.energy should be < wolf.energy)
          yield ()
        }.getOrThrowFiberFailure()

  test("Sheep updated after tick"):
    val sheep = Sheep(EntityId.random, DefaultPosition)
    val world = World(Width, Height, Seq.empty, Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    val dx = 1
    val dy = 0
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(sheep.id, dx, dy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            updatedSheep <- ZIO.succeed(updatedWorld.sheepById(sheep.id).get)
            _ <- ZIO.succeed(updatedSheep.position.x should not be sheep.position.x)
            _ <- ZIO.succeed(updatedSheep.energy should be < sheep.energy)
          yield ()
        }.getOrThrowFiberFailure()

  test("Sheep is eaten"):
    val wolf = Wolf(EntityId.random, DefaultPosition)
    val sheep = Sheep(EntityId.random, DefaultPosition)
    val world = World(Width, Height, Seq(wolf), Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(wolf.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.moveEntityDirection(sheep.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            directions <- ZIO.succeed(ecosystemManager.getDirections)
            _ <- ZIO.succeed(directions.contains(sheep.id) shouldBe false)
            _ <- ZIO.succeed(updatedWorld.sheep.exists(_.id == sheep.id) shouldBe false)
          yield ()
        }.getOrThrowFiberFailure()

  test("Sheep is not eaten"):
    val newPosition = Position(40, 40)
    val wolf = Wolf(EntityId.random, DefaultPosition)
    val sheep = Sheep(EntityId.random, newPosition)
    val world = World(Width, Height, Seq(wolf), Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(wolf.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.moveEntityDirection(sheep.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            directions <- ZIO.succeed(ecosystemManager.getDirections)
            _ <- ZIO.succeed(directions.contains(sheep.id) shouldBe true)
            _ <- ZIO.succeed(updatedWorld.sheep.exists(_.id == sheep.id) shouldBe true)
          yield ()
        }.getOrThrowFiberFailure()

  test("Wolves reproduce"):
    val wolf1 = Wolf(EntityId.random, DefaultPosition, energy = HighEnergy)
    val wolf2 = Wolf(EntityId.random, DefaultPosition, energy = HighEnergy)
    val world = World(Width, Height, Seq(wolf1, wolf2), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(wolf1.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.moveEntityDirection(wolf2.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            directions <- ZIO.succeed(ecosystemManager.getDirections)
            _ <- ZIO.succeed:
              val expectedSize = 3
              updatedWorld.wolves.size should be (expectedSize)
            _ <- ZIO.succeed:
              val existingIds = Set(wolf1.id, wolf2.id)
              val newWolves = updatedWorld.wolves.filterNot(w => existingIds.contains(w.id))
              val expectedSize = 1
              newWolves.size should be (expectedSize)
            _ <- ZIO.succeed:
              val existingIds = Set(wolf1.id, wolf2.id)
              val newWolves = updatedWorld.wolves.filterNot(w => existingIds.contains(w.id))
              directions.contains(newWolves.head.id) shouldBe true
          yield ()
        }.getOrThrowFiberFailure()

  test("Wolves do not reproduce"):
    val wolf1 = Wolf(EntityId.random, DefaultPosition, energy = LowEnergy)
    val wolf2 = Wolf(EntityId.random, DefaultPosition, energy = LowEnergy)
    val world = World(Width, Height, Seq(wolf1, wolf2), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(wolf1.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.moveEntityDirection(wolf2.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            _ <- ZIO.succeed(updatedWorld.wolves.size shouldBe 2)
          yield ()
        }.getOrThrowFiberFailure()

  test("Wolves lose energy after reproduction"):
    val wolf1 = Wolf(EntityId.random, DefaultPosition, energy = HighEnergy)
    val wolf2 = Wolf(EntityId.random, DefaultPosition, energy = HighEnergy)
    val world = World(Width, Height, Seq(wolf1, wolf2), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(wolf1.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.moveEntityDirection(wolf2.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            _ <- ZIO.succeed:
              val expectedEnergy = 39.8
              val updatedWolf1 = updatedWorld.wolfById(wolf1.id).get
              val updatedWolf2 = updatedWorld.wolfById(wolf2.id).get
              updatedWolf1.energy should be(expectedEnergy)
              updatedWolf2.energy should be(expectedEnergy)
          yield ()
        }.getOrThrowFiberFailure()

  test("Wolf dies"):
    val wolf = Wolf(EntityId.random, DefaultPosition, energy = ZeroEnergy)
    val world = World(Width, Height, Seq(wolf), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(wolf.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            directions <- ZIO.succeed(ecosystemManager.getDirections)
            updatedWorld <- ecosystemManager.getWorld
            _ <- ZIO.succeed(directions.contains(wolf.id) shouldBe false)
            _ <- ZIO.succeed(updatedWorld.wolves.exists(_.id == wolf.id) shouldBe false)
          yield ()
        }.getOrThrowFiberFailure()

  test("Grass is eaten"):
    val sheep = Sheep(EntityId.random, DefaultPosition)
    val grass = Grass(EntityId.random, DefaultPosition)
    val world = World(Width, Height, Seq.empty, Seq(sheep), Seq(grass))
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(sheep.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            directions <- ZIO.succeed(ecosystemManager.getDirections)
            _ <- ZIO.succeed(updatedWorld.grass.exists(_.id == grass.id) shouldBe false)
          yield ()
        }.getOrThrowFiberFailure()

  test("Grass is not eaten"):
    val newPosition = Position(40, 40)
    val sheep = Sheep(EntityId.random, DefaultPosition)
    val grass = Grass(EntityId.random, newPosition)
    val world = World(Width, Height, Seq.empty, Seq(sheep), Seq(grass))
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(sheep.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            directions <- ZIO.succeed(ecosystemManager.getDirections)
            _ <- ZIO.succeed(updatedWorld.grass.exists(_.id == grass.id) shouldBe true)
          yield ()
        }.getOrThrowFiberFailure()

  test("Sheep reproduce"):
    val sheep1 = Sheep(EntityId.random, DefaultPosition, energy = HighEnergy)
    val sheep2 = Sheep(EntityId.random, DefaultPosition, energy = HighEnergy)
    val world = World(Width, Height, Seq.empty, Seq(sheep1, sheep2), Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(sheep1.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.moveEntityDirection(sheep2.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            directions <- ZIO.succeed(ecosystemManager.getDirections)
            _ <- ZIO.succeed:
              val expectedSize = 3
              updatedWorld.sheep.size should be(expectedSize)
            _ <- ZIO.succeed:
              val existingIds = Set(sheep1.id, sheep2.id)
              val newSheep = updatedWorld.sheep.filterNot(w => existingIds.contains(w.id))
              val expectedSize = 1
              newSheep.size should be(expectedSize)
            _ <- ZIO.succeed:
              val existingIds = Set(sheep1.id, sheep2.id)
              val newSheep = updatedWorld.sheep.filterNot(w => existingIds.contains(w.id))
              directions.contains(newSheep.head.id) shouldBe true
          yield ()
        }.getOrThrowFiberFailure()

  test("Sheep do not reproduce"):
    val sheep1 = Sheep(EntityId.random, DefaultPosition, energy = LowEnergy)
    val sheep2 = Sheep(EntityId.random, DefaultPosition, energy = LowEnergy)
    val world = World(Width, Height, Seq.empty, Seq(sheep1, sheep2), Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(sheep1.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.moveEntityDirection(sheep2.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            _ <- ZIO.succeed(updatedWorld.sheep.size shouldBe 2)
          yield ()
        }.getOrThrowFiberFailure()

  test("Sheep lose energy after reproduction"):
    val sheep1 = Sheep(EntityId.random, DefaultPosition, energy = HighEnergy)
    val sheep2 = Sheep(EntityId.random, DefaultPosition, energy = HighEnergy)
    val world = World(Width, Height, Seq.empty, Seq(sheep1, sheep2), Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(sheep1.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.moveEntityDirection(sheep2.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            _ <- ZIO.succeed:
              val expectedEnergy = 39.8
              val updatedSheep1 = updatedWorld.sheepById(sheep1.id).get
              val updatedSheep2 = updatedWorld.sheepById(sheep2.id).get
              updatedSheep1.energy should be(expectedEnergy)
              updatedSheep2.energy should be(expectedEnergy)
          yield ()
        }.getOrThrowFiberFailure()

  test("Sheep dies"):
    val sheep = Sheep(EntityId.random, DefaultPosition, energy = ZeroEnergy)
    val world = World(Width, Height, Seq.empty, Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.moveEntityDirection(sheep.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            directions <- ZIO.succeed(ecosystemManager.getDirections)
            updatedWorld <- ecosystemManager.getWorld
            _ <- ZIO.succeed(directions.contains(sheep.id) shouldBe false)
            _ <- ZIO.succeed(updatedWorld.sheep.exists(_.id == sheep.id) shouldBe false)
          yield ()
        }.getOrThrowFiberFailure()

  test("Grass generated"):
    val world = World(Width, Height, Seq.empty, Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    val grassCount = 100
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ZIO.foreachDiscard(1 to grassCount)(_ => ecosystemManager.tick())
            updatedWorld <- ecosystemManager.getWorld
            _ <- ZIO.succeed(updatedWorld.grass.size should be > 0)
          yield ()
        }.getOrThrowFiberFailure()

  test("Wolf separation after reproduction"):
    val wolf1 = Wolf(EntityId.random, DefaultPosition, HighEnergy)
    val wolf2 = Wolf(EntityId.random, DefaultPosition, HighEnergy)
    val world = World(Width, Height, Seq(wolf1, wolf2), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.setWorld(world)
            _ <- ecosystemManager.moveEntityDirection(wolf1.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.moveEntityDirection(wolf2.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            _ <- ZIO.succeed:
              val updatedWolf = updatedWorld.wolves.filter(w => w.id == wolf1.id || w.id == wolf2.id)
              val w1 = updatedWolf.head
              val w2 = updatedWolf(1)
              w1.position should not be w2.position
            _ <- ZIO.succeed:
              val updatedWolf = updatedWorld.wolves.filter(w => w.id == wolf1.id || w.id == wolf2.id)
              val w1 = updatedWolf.head
              val w2 = updatedWolf(1)
              val expectedDistance = 20.0
              math.hypot(w1.position.x - w2.position.x, w1.position.y - w2.position.y) should be >= expectedDistance
          yield ()
        }.getOrThrowFiberFailure()

  test("Sheep separation after reproduction"):
    val sheep1 = Sheep(EntityId.random, DefaultPosition, HighEnergy)
    val sheep2 = Sheep(EntityId.random, DefaultPosition, HighEnergy)
    val world = World(Width, Height, Seq.empty, Seq(sheep1, sheep2), Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.setWorld(world)
            _ <- ecosystemManager.moveEntityDirection(sheep1.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.moveEntityDirection(sheep2.id, DefaultDx, DefaultDy)
            _ <- ecosystemManager.tick()
            updatedWorld <- ecosystemManager.getWorld
            _ <- ZIO.succeed:
              val updatedSheep = updatedWorld.sheep.filter(s => s.id == sheep1.id || s.id == sheep2.id)
              val s1 = updatedSheep.head
              val s2 = updatedSheep(1)
              s1.position should not be s2.position
            _ <- ZIO.succeed:
              val updatedSheep = updatedWorld.sheep.filter(s => s.id == sheep1.id || s.id == sheep2.id)
              val s1 = updatedSheep.head
              val s2 = updatedSheep(1)
              val expectedDistance = 20.0
              math.hypot(s1.position.x - s2.position.x, s1.position.y - s2.position.y) should be >= expectedDistance
          yield ()
        }.getOrThrowFiberFailure()

  test("Update wolf directions"):
    val wolf = Wolf(EntityId.random, DefaultPosition)
    val world = World(Width, Height, Seq(wolf), Seq.empty, Seq.empty)
    val ecosystemManager = createManager(world) 
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.setWorld(world)
            _ <- ecosystemManager.moveEntityDirection(wolf.id, DefaultDx, DefaultDy)
            _ <- ZIO.succeed(ecosystemManager.getDirections.get(wolf.id) should be(Some((1.0, 0.0))))
          yield ()
        }

  test("Update sheep directions"):
    val sheep = Sheep(EntityId.random, DefaultPosition)
    val world = World(Width, Height, Seq.empty, Seq(sheep), Seq.empty)
    val ecosystemManager = createManager(world)
    Unsafe.unsafe:
      implicit u =>
        runtime.unsafe.run {
          for
            _ <- ecosystemManager.setWorld(world)
            _ <- ecosystemManager.moveEntityDirection(sheep.id, DefaultDx, DefaultDy)
            _ <- ZIO.succeed(ecosystemManager.getDirections.get(sheep.id) should be(Some((1.0, 0.0))))
          yield ()
        }



