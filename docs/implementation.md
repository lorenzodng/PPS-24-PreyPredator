# Implementazione 
L’implementazione del sistema sfrutta in modo avanzato alcune delle principali funzionalità offerte da Scala 3, con particolare attenzione alla composizione monadica del codice, all’immutabilità dei dati e all’utilizzo di costrutti volti ad offrire maggiore astrazione e flessibilità.  
In questa sezione vengono illustrati gli aspetti più rilevanti a livello di codice, alcuni dei quali già anticipati nel paragrafo precedente.

## Composizione monadica e gestione degli effetti
Il controller della simulazione utilizza la libreria \texttt{ZIO} per modellare gli effetti in modo funzionale e compositivo.
Gli effetti vengono combinati tramite monadi ZIO, fondamentali per avviare la simulazione e gestire il ciclo di aggiornamento in modo sequenziale, evitando side effects e potenziali race conditions:
\begin{center}
\begin{minipage}{0.94 \textwidth}
\begin{lstlisting}[language=scala, basicstyle=\footnotesize\ttfamily]
def startSimulation(nWolves: Int, nSheep: Int, nGrass: Int, nGrassInterval: Int, nGrassGenerated: Int, widthSimulation: Int, heightSimulation: Int): UIO[Unit] =
    for
      _ <- ecosystemManager.setGrassInterval(nGrassInterval, sleepTime)
      _ <- ecosystemManager.setGrassGenerated(nGrassGenerated)
      _ <- ZIO.succeed(stopFlag.reset())
      f <- loop(widthSimulation, heightSimulation, nWolves, nSheep, nGrass).forkDaemon
      _ <- ZIO.succeed:
        fiber = Some(f)
    yield ()
\end{lstlisting}
\end{minipage}
\end{center}
Il comportamento principale è descritto nel metodo \textit{loop}, che rappresenta il ciclo simulativo ricorsivo e compone effetti puri per ogni fase di aggiornamento:
\newpage
\begin{center}
\begin{minipage}{0.94 \textwidth}
\begin{lstlisting}[language=scala, basicstyle=\footnotesize\ttfamily]
private def loop(width: Int, height: Int, nWolves: Int, nSheep: Int, nGrass: Int): UIO[Unit] =
    for
      start <- Clock.nanoTime
      updatedWorld <- prepareWorld(width, height, nWolves, nSheep, nGrass)
      _ <- simulateStep(updatedWorld)
      _ <- waitNextFrame(start)
      _ <- if stopFlag.isSet then ZIO.unit
      else loop(width, height, nWolves, nSheep, nGrass)
    yield ()
\end{lstlisting}
\end{minipage}
\end{center}
\subsection{Immutabilità dello stato}
Lo stato globale della simulazione è rappresentato dalla classe \textit{World}, completamente immutabile. Ogni trasformazione sullo stato produce una nuova istanza:

\begin{center}
\begin{minipage}{0.94 \textwidth}
\begin{lstlisting}[language=scala, basicstyle=\footnotesize\ttfamily]
case class World(width: Int, height: Int, wolves: Seq[Wolf], sheep: Seq[Sheep], grass: Seq[Grass]):

  def generateWolves(nWolves: Int): World =
    val newWolves = (1 to nWolves).map: _ =>
      val randomId = EntityId.random
      val randomPosition = Position(x = math.random() * width, y = math.random() * height)
      Wolf(randomId, randomPosition)
    copy(wolves = wolves ++ newWolves)
\end{lstlisting}
\end{minipage}
\end{center}
Analogamente, esistono metodi immutabili per generare pecore ed erba, così come per aggiornare o rimuovere entità.
\\\\
Ogni operazione di modifica dello stato quindi produce una nuova istanza della classe, garantendo coerenza dello stato e assenza di effetti collaterali (\textit{side effects}).

\subsection{Opaque types per la gestione degli identificatori}
Per rappresentare in modo sicuro gli identificatori univoci delle entità, evitando conversioni implicite o accessi diretti, viene utilizzato il costrutto \texttt{opaque type}:

\begin{center}
\begin{minipage}{0.94 \textwidth}
\begin{lstlisting}[language=scala, basicstyle=\footnotesize\ttfamily]
object EntityId:

  opaque type Type = String

  def random: Type = UUID.randomUUID().toString
\end{lstlisting}
\end{minipage}
\end{center}
Questo approccio permette di nascondere l’implementazione interna basata su \texttt{UUID}, esponendo esclusivamente le operazioni necessarie. \\In questo modo, si crea un tipo astratto e distintivo, che rafforza la sicurezza a livello di tipo e previene utilizzi errati o accidentali degli identificatori rappresentativi delle entità all'interno del sistema.

\subsection{Astrazione tramite trait}
Nel progetto, i \texttt{trait} sono stati utilizzati come strumenti fondamentali per definire interfacce comuni e comportamenti condivisi tra le entità del sistema:
\begin{center}
\begin{minipage}{0.94 \textwidth}
\begin{lstlisting}[language=scala, basicstyle=\footnotesize\ttfamily]
trait Entity:
  val id: EntityId.Type
  val position: Position
  val energy: Double
  val mass: Int
  val speed: Double
\end{lstlisting}
\end{minipage}
\end{center}
Questo trait definisce le proprietà essenziali condivise da tutte le entità, assicurando coerenza e facilitando l’estensione tramite classi concrete.  
\\\\
L’adozione dei trait favorisce inoltre la programmazione orientata ai tipi e l’uso del \textit{polimorfismo inclusivo}, permettendo di manipolare diverse entità utilizzando, in modo uniforme, strutture dati immutabili del modello.

\subsection{Companion object per operazioni ausiliarie}
I \texttt{companion object} sono stati utilizzati per associare a trait e classi metodi di utilità statici, mantenendo il codice modulare e separando adeguatamente le responsabilità:

\begin{center}
\begin{minipage}{0.94 \textwidth}
\begin{lstlisting}[language=scala, basicstyle=\footnotesize\ttfamily]
object Grass:

  def generateRandomGrass(grassCount: Int, worldWidth: Double, worldHeight: Double): Seq[Grass] =
    (1 to grassCount).map: _ =>
      Grass(id = EntityId.random, position = Position(x = Math.random() * worldWidth, y = Math.random() * worldHeight))
\end{lstlisting}
\end{minipage}
\end{center}
Analogamente, altri companion object, quali \textit{Entity} e \textit{Position}, forniscono metodi per il calcolo di operazioni geometriche. \\\\
In questo modo il modello rimane semplice e immutabile, delegando a tali oggetti le operazioni funzionali ausiliarie e i metodi che non appartengono direttamente allo stato delle entità.

\subsection{Extension methods per estensione delle entità}
Per estendere il comportamento delle entità senza modificare direttamente il trait \textit{Entity}, è stato definito un \texttt{extension method} all’interno del relativo companion object:

\begin{center}
\begin{minipage}{0.94 \textwidth}
\begin{lstlisting}[language=scala, basicstyle=\footnotesize\ttfamily]
object Entity:

  extension (e: Entity)
    def radius(): Double = math.sqrt(e.mass / math.Pi)
\end{lstlisting}
\end{minipage}
\end{center}
Questo approccio permette di definire funzioni che si comportano come metodi associati alle istanze di Entity, mantenendo il trait focalizzato sulle proprietà essenziali.  

\subsection{Gestione degli eventi con callback e runtime}

Oltre che attraverso elementi dell’interfaccia grafica, la comunicazione tra Controller e View avviene tramite due funzioni callback, responsabili dell'aggiornam-\ ento visivo dell'ecosistema in corrispondenza di eventi specifici di esecuzione:

\begin{itemize}
\item \texttt{updateViewCallback}: viene invocata al termine di ogni step simulativo per aggiornare graficamente la vista.
\item \texttt{extinctionCallback}: viene invocata quando tutte le entità animali (lupi e pecore) si estinguono, per aggiornare la vista e lo stato dei pulsanti.
\end{itemize}
In particolare, l'avvio della simulazione e l'interazione con il Controller avvengono nel metodo \texttt{runSimulation}, punto d'ingresso dell'applicazione, in cui la View registra le funzioni di callback nel Controller:
\begin{center}
\begin{minipage}{0.94 \textwidth}
\begin{lstlisting}[language=scala, basicstyle=\footnotesize\ttfamily]
ecosystemController.setUpdateViewCallback(() =>
simulationView.viewManager.updateView())

ecosystemController.setExtinctionCallback(() =>
simulationView.viewManager.updateView()
simulationView.viewManager.updateButtons())
\end{lstlisting}
\end{minipage}
\end{center}
L'esecuzione della simulazione e l’inizializzazione dello stato interno avvengono tramite due primitive di ZIO, necessarie per valutare concretamente gli effetti monadici che rappresentano la logica funzionale del sistema:

\begin{itemize}
\item \texttt{Unsafe.unsafe}: permette di accedere all’API di esecuzione imperativa di ZIO.
\item \texttt{Runtime.default}: fornisce un runtime predefinito per valutare gli effetti ZIO.
\end{itemize}

\begin{center}
\begin{minipage}{0.94 \textwidth}
\begin{lstlisting}[language=scala, basicstyle=\footnotesize\ttfamily]
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
\end{lstlisting}
\end{minipage}
\end{center}
In modo analogo, anche durante l’aggiornamento della vista e la gestione degli eventi di ridimensionamento dell'ambiente simulato, il recupero dello stato della simulazione avviene tramite l'uso di Unsafe.unsafe e Runtime.default, garantendo una sincronizzazione precisa tra modello e l'interfaccia grafica.
