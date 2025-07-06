package model

import model.entities.{EntityId, Grass, Sheep, Wolf}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WorldTest extends AnyFunSuite with Matchers:

  val Width = 100
  val Height = 100
  val DefaultPosition: Position = Position(10, 10)
  val InitialWorld: World = World(Width, Height, Seq.empty, Seq.empty, Seq.empty)

  val wolf: Wolf = Wolf(EntityId.random, Position(1, 1))
  val sheep: Sheep = Sheep(EntityId.random, Position(2, 2))
  val grass: Grass = Grass(EntityId.random, Position(3, 3))
  val world: World = World(Width, Height, Seq(wolf), Seq(sheep), Seq(grass))

  test("Generate wolves"):
    val numWolves = 5
    val newWorld = InitialWorld.generateWolves(numWolves)
    val expectedWolves = 5
    newWorld.wolves.size should be (expectedWolves)

  test("Generate wolves with different ids"):
    val numWolves = 5
    val newWorld = InitialWorld.generateWolves(numWolves)
    val ids = newWorld.wolves.map(_.id)
    ids.distinct.size should be(ids.size)

  test("Generate sheep"):
    val numSheep = 5
    val newWorld = InitialWorld.generateWolves(numSheep)
    val expectedSheep = 5
    newWorld.wolves.size should be(expectedSheep)

  test("Generate sheep with different ids"):
    val numSheep = 5
    val newWorld = InitialWorld.generateWolves(numSheep)
    val ids = newWorld.sheep.map(_.id)
    ids.distinct.size should be(ids.size)

  test("Generate grass"):
    val numGrass = 5
    val newWorld = InitialWorld.generateGrass(numGrass)
    val expectedGrass = 5
    newWorld.grass.size should be(expectedGrass)

  test("Generate grass with different ids"):
    val numGrass = 5
    val newWorld = InitialWorld.generateGrass(numGrass)
    val ids = newWorld.grass.map(_.id)
    ids.distinct.size should be(ids.size)

  test("Find wolf success"):
    world.wolfById(wolf.id) shouldBe Some(wolf)

  test("Find wolf fail"):
    world.wolfById(EntityId.random) shouldBe None

  test("Find sheep success"):
    world.sheepById(sheep.id) shouldBe Some(sheep)

  test("Find sheep fail"):
    world.sheepById(EntityId.random) shouldBe None

  test("Update wolf"):
    val updatedWolf = wolf.copy(position = DefaultPosition)
    val updatedWorld = world.updateWolf(updatedWolf)
    updatedWorld.wolves.find(_.id == wolf.id).get.position shouldBe DefaultPosition

  test("Update sheep"):
    val updatedSheep = sheep.copy(position = DefaultPosition)
    val updatedWorld = world.updateSheep(updatedSheep)
    updatedWorld.sheep.find(_.id == sheep.id).get.position shouldBe DefaultPosition

  test("Remove sheep"):
    val updatedWorld = world.removeSheep(Seq(sheep))
    updatedWorld.sheep should not contain sheep

  test("Remove grass"):
    val updatedWorld = world.removeGrass(Seq(grass))
    updatedWorld.grass should not contain grass

  test("Add grass"):
    val grassSeq = Seq(grass)
    val updatedWorld = world.addGrass(grassSeq)
    updatedWorld.grass should contain (grass)

  test("Delete all entities"):
    val clearedWorld = world.deleteEntities()
    clearedWorld.wolves shouldBe empty
    clearedWorld.sheep shouldBe empty
    clearedWorld.grass shouldBe empty


