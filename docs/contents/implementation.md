# Implementazione 
L’implementazione del sistema sfrutta in modo avanzato alcune delle principali funzionalità offerte da Scala 3, con particolare attenzione alla composizione monadica del codice, all’immutabilità dei dati e all’utilizzo di costrutti volti ad offrire maggiore astrazione e flessibilità.  
In questa sezione vengono illustrati gli aspetti più rilevanti a livello di codice, alcuni dei quali già anticipati nel paragrafo precedente.

## Composizione monadica e gestione degli effetti
Il controller della simulazione utilizza la libreria ZIO per modellare gli effetti in modo funzionale e compositivo.
Gli effetti vengono combinati tramite monadi ZIO, fondamentali per avviare la simulazione e gestire il ciclo di aggiornamento in modo sequenziale, evitando side effects e potenziali race conditions:
```scala 
def startSimulation(nWolves: Int, nSheep: Int, nGrass: Int, nGrassInterval: Int, nGrassGenerated: Int, widthSimulation: Int, heightSimulation: Int): UIO[Unit] =
    for
      _ <- ecosystemManager.setGrassInterval(nGrassInterval, sleepTime)
      _ <- ecosystemManager.setGrassGenerated(nGrassGenerated)
      _ <- ZIO.succeed(stopFlag.reset())
      f <- loop(widthSimulation, heightSimulation, nWolves, nSheep, nGrass).forkDaemon
      _ <- ZIO.succeed:
        fiber = Some(f)
    yield ()
```
Il comportamento principale è descritto nel metodo *loop*, che rappresenta il ciclo simulativo ricorsivo e compone effetti puri per ogni fase di aggiornamento:
```scala 
private def loop(width: Int, height: Int, nWolves: Int, nSheep: Int, nGrass: Int): UIO[Unit] =
    for
      start <- Clock.nanoTime
      updatedWorld <- prepareWorld(width, height, nWolves, nSheep, nGrass)
      _ <- simulateStep(updatedWorld)
      _ <- waitNextFrame(start)
      _ <- if stopFlag.isSet then ZIO.unit
      else loop(width, height, nWolves, nSheep, nGrass)
    yield ()
```

## Immutabilità dello stato
Lo stato globale della simulazione è rappresentato dalla classe *World*, completamente immutabile. Ogni trasformazione sullo stato produce una nuova istanza:

```scala 
case class World(width: Int, height: Int, wolves: Seq[Wolf], sheep: Seq[Sheep], grass: Seq[Grass]):

  def generateWolves(nWolves: Int): World =
    val newWolves = (1 to nWolves).map: _ =>
      val randomId = EntityId.random
      val randomPosition = Position(x = math.random() * width, y = math.random() * height)
      Wolf(randomId, randomPosition)
    copy(wolves = wolves ++ newWolves)
```

Analogamente, esistono metodi immutabili per generare pecore ed erba, così come per aggiornare o rimuovere entità.

Ogni operazione di modifica dello stato quindi produce una nuova istanza della classe, garantendo coerenza dello stato e assenza di effetti collaterali (*side effects*).

## Opaque types per la gestione degli identificatori
Per rappresentare in modo sicuro gli identificatori univoci delle entità, evitando conversioni implicite o accessi diretti, viene utilizzato il costrutto `opaque type`:

```scala 
object EntityId:

  opaque type Type = String

  def random: Type = UUID.randomUUID().toString
```
Questo approccio permette di nascondere l’implementazione interna basata su `UUID`, esponendo esclusivamente le operazioni necessarie.  
In questo modo, si crea un tipo astratto e distintivo, che rafforza la sicurezza a livello di tipo e previene utilizzi errati o accidentali degli identificatori rappresentativi delle entità all'interno del sistema.

## Astrazione tramite trait
Nel progetto, i trait sono stati utilizzati come strumenti fondamentali per definire interfacce comuni e comportamenti condivisi tra le entità del sistema:
```scala 
trait Entity:
  val id: EntityId.Type
  val position: Position
  val energy: Double
  val mass: Int
  val speed: Double
```
Questo trait definisce le proprietà essenziali condivise da tutte le entità, assicurando coerenza e facilitando l’estensione tramite classi concrete.  

L’adozione dei trait favorisce inoltre la programmazione orientata ai tipi e l’uso del *polimorfismo inclusivo*, permettendo di manipolare diverse entità utilizzando, in modo uniforme, strutture dati immutabili del modello.

## Companion object per operazioni ausiliarie
I companion object sono stati utilizzati per associare a trait e classi metodi di utilità statici, mantenendo il codice modulare e separando adeguatamente le responsabilità:

```scala 
object Grass:

  def generateRandomGrass(grassCount: Int, worldWidth: Double, worldHeight: Double): Seq[Grass] =
    (1 to grassCount).map: _ =>
      Grass(id = EntityId.random, position = Position(x = Math.random() * worldWidth, y = Math.random() * worldHeight))
```
Analogamente, altri companion object, quali *Entity* e *Position*, forniscono metodi per il calcolo di operazioni geometriche.

In questo modo il modello rimane semplice e immutabile, delegando a tali oggetti le operazioni funzionali ausiliarie e i metodi che non appartengono direttamente allo stato delle entità.

## Extension methods per estensione delle entità
Per estendere il comportamento delle entità senza modificare direttamente il trait Entity, è stato definito un `extension method` all’interno del relativo companion object:

```scala 
object Entity:

  extension (e: Entity)
    def radius(): Double = math.sqrt(e.mass / math.Pi)
```
Questo approccio permette di definire funzioni che si comportano come metodi associati alle istanze di Entity, mantenendo il trait focalizzato sulle proprietà essenziali.  

## Gestione degli eventi con callback e runtime

Oltre che attraverso elementi dell’interfaccia grafica, la comunicazione tra Controller e View avviene tramite due funzioni callback, responsabili dell'aggiornamento visivo dell'ecosistema in corrispondenza di eventi specifici di esecuzione:
- `updateViewCallback`: viene invocata al termine di ogni step simulativo per aggiornare graficamente la vista.
- `extinctionCallback`: viene invocata quando tutte le entità animali (lupi e pecore) si estinguono, per aggiornare la vista e lo stato dei pulsanti.

```scala 
ecosystemController.setUpdateViewCallback(() =>
simulationView.viewManager.updateView())

ecosystemController.setExtinctionCallback(() =>
simulationView.viewManager.updateView()
simulationView.viewManager.updateButtons())
```
L'esecuzione della simulazione e l’inizializzazione dello stato interno avvengono tramite due primitive di ZIO, necessarie per valutare concretamente gli effetti monadici che rappresentano la logica funzionale del sistema:
- `Unsafe.unsafe`: permette di accedere all’API di esecuzione imperativa di ZIO.
- `Runtime.default`: fornisce un runtime predefinito per valutare gli effetti ZIO.

```scala 
unsafe:
    implicit u =>
      val runtime = Runtime.default

      val worldRef = runtime.unsafe.run(Ref.make(emptyWorld)).getOrThrowFiberFailure()
      val ecosystemManager = new EcosystemManager(worldRef)
      val stopFlag = Flag
      val ecosystemController = new EcosystemController(ecosystemManager, stopFlag)
      val simulationView = new SimulationView(ecosystemController)

      ecosystemController.setUpdateViewCallback(() =>
        simulationView.viewManager.updateView())

      ecosystemController.setExtinctionCallback(() =>
        simulationView.viewManager.updateView()
        simulationView.viewManager.updateButtons())

      simulationView.open()
```
In modo analogo, anche durante l’aggiornamento della vista e la gestione degli eventi di ridimensionamento dell'ambiente simulato, il recupero dello stato della simulazione avviene tramite l'uso di Unsafe.unsafe e Runtime.default, garantendo una sincronizzazione precisa tra modello e l'interfaccia grafica.
