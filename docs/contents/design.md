# Design architetturale
Per garantire una corretta separazione delle responsabilità fra componenti, il progetto adotta il pattern architetturale *Model-View-Controller (MVC)*, consentendo la realizzazione di un'architettura modulare, scalabile e facilmente estendibile.
In particolare, la scelta del pattern MVC assicura i seguenti vantaggi:
- La logica di dominio è completamente separata dalla logica di presentazione, rendendo possibile riutilizzare il modello in contesti differenti.
- Il *Controller* agisce come unico punto di orchestrazione, semplificando il coordinamento del flusso di simulazione.
- La modularità facilita il testing e gli eventuali interventi di manutenzione.
- Le singole componenti possono essere facilmente estese con nuove funzionalità senza modificare le altre.

Tale scelta architetturale consente di mantenere il controllo sulla concorrenza, evitando la complessità di sincronizzazione di più entità attive in parallelo, garantendo, al tempo stesso, reattività e fluidità.

<p align="center">
  <img src="../imgs/mainArchitecture.png" alt="Architettura generale" width="55%">
  <br>
  <em>Figura 1 – Architettura generale</em>
</p>

Come illustrato in **Figura 1**, le componenti principali del sistema sono disaccoppiate e progettate secondo i principi del paradigma MVC, favorendo una chiara separazione delle responsabilità e una maggiore riusabilità del codice.  
Ciascuna componente assume le seguenti responsabilità:
- Il *Model* rappresenta il cuore logico dell'ecosistema simulato, descrivendo lo stato del mondo e il comportamento delle entità che lo popolano.  
  Inoltre, è responsabile della gestione dello stato interno del sistema e dell’evoluzione temporale dell’ambiente simulato. In particolare:
  - Definisce le entità (lupi, pecore, erba) e ne modella il comportamento.
  - Incapsula le regole di evoluzione dell’ecosistema.
  - Gestisce il ciclo di aggiornamento dello stato, mantenendo la coerenza delle interazioni tra le entità.
  - Espone un’interfaccia astratta per permettere al controller di manipolare e osservare lo stato del mondo simulato.
- Il Controller agisce come intermediario tra il Model e la *View*, coordinando la logica di esecuzione del ciclo simulativo dell’ecosistema. In particolare:
  - Avvia la simulazione con la creazione e inizializzazione dello stato del mondo.
  - Gestisce il ciclo di simulazione in modo asincrono e ne coordina l’evoluzione temporale.
  - Gestisce l’interruzione controllata della simulazione.
  - Gestisce il reset dell’ambiente simulato.
  - Coordina gli aggiornamenti dell’interfaccia grafica.
  - Gestisce il ridimensionamento dinamico dell’ambiente simulato.
- La View si occupa della rappresentazione grafica dell’ecosistema e della gestione dell’interazione con l’utente, e fornisce un’interfaccia per configurare, osservare e controllare la simulazione. In particolare:
  - Consente la configurazione dei parametri della simulazione tramite controlli dedicati.
  - Fornisce pulsanti per avviare, fermare e resettare la simulazione.
  - Consente di visualizzare graficamente lo stato corrente del mondo simulato, mostrando dinamicamente l’evoluzione delle popolazioni di entità.
  - Espone etichette informative sul numero corrente di entità presenti nel sistema, per ogni popolazione.

## Design di dettaglio
L'applicazione è sviluppata in Scala e sfrutta la libreria ZIO per la gestione funzionale degli effetti e del flusso asincrono. Il componente responsabile dell'esecuzione della simulazione è rappresentato da una `fibra`, incaricata di gestire il ciclo principale dell’ecosistema attraverso un loop continuo, scandito da intervalli regolari di tempo, in cui aggiorna lo stato globale del sistema.  
La scelta di utilizzare una fibra come componente attivo dedicato all'esecuzione della simulazione è motivata dai seguenti vantaggi:

- Separazione chiara delle responsabilità:  
  Il thread main si occupa esclusivamente dell’inizializzazione e dell’avvio della simulazione, delegando l’esecuzione continua e la gestione del ciclo di vita alla fibra dedicata, favorendo una maggiore modularità e semplificando la manutenzione del codice.
- Esecuzione parallela di più simulazioni:  
  La fibra, essendo una struttura leggera e indipendente, può essere creata singolarmente per ciascuna simulazione. Questo permette di eseguire più simulazioni in parallelo senza dover condividere il *thread main*, il quale rimarrebbe invece un punto unico di esecuzione e responsabilità. Grazie a ciò, la scalabilità e l’isolamento delle simulazioni risultano migliorati, senza penalizzare le prestazioni o la reattività dell’interfaccia utente.
- Gestione efficiente del reset:  
  L’interruzione della fibra consente di arrestare immediatamente il ciclo di simulazione, evitando l’esecuzione continua di operazioni di polling non necessarie.   Tale meccanismo riduce il carico computazionale complessivo, garantendo un processo di reset più rapido e pulito, con benefici sulla reattività dell’applicazione.  
Inoltre, attraverso l’interruzione delle fibre non più necessarie, il runtime ZIO può liberare e riallocare i thread coinvolti, migliorando l’efficienza nella gestione delle risorse e supportando un numero maggiore di simulazioni concorrenti senza sovraccaricare le prestazioni.

Di seguito sono analizzate, più in dettaglio, le scelte di progettazione considerate per implementare i componenti architetturali del sistema:
- Model:  
  Lo stato globale dell’ecosistema è descritto all'interno del Model, ed è rappresentato da un oggetto immutabile che descrive lo stato corrente dell’ambiente simulato e delle sue entità.

  Per gestire in sicurezza l’accesso concorrente a questo stato condiviso, viene utilizzato un riferimento `Ref`fornito dalla libreria ZIO, in modo da consentire aggiornamenti atomici, thread-safe e composabili in uno stile funzionale. Le operazioni sul riferimento sono modellate come effetti monadici, che rendono esplicite le dipendenze e facilitano la composizione sicura delle modifiche allo stato del mondo.
- Controller:  
  Gli aggiornamenti dello stato, gestiti da parte del Controller, avvengono mediante accessi atomici e sequenziali al riferimento Ref, assicurando un accesso isolato e coerente al modello, evitando così condizioni di competizione (*race conditions*) e mantenendo la consistenza tra i diversi step di simulazione.
  
  La comunicazione con la View è affidata a callback implementate dalla View stessa e registrate nel Controller, che vengono invocate ad ogni step per aggiornare l’interfaccia e in risposta a eventi specifici, rappresentati dall'aggiornamento dello stato della simulazione e dall'estinzione delle popolazioni delle entità animali.
  
  Questo approccio favorisce una maggiore separazione delle responsabilità, evitando dipendenze bidirezionali tra Controller e View.
- View:  
  I componenti di interfaccia sono aggiornati esclusivamente sull’`Event Dispatch Thread (EDT)`, allo scopo di garantire stabilità e reattività nei cambiamenti dell'interfaccia. Gli aggiornamenti dello stato grafico e le interazioni dell’utente vengono quindi separati logicamente e concettualmente dal ciclo di simulazione, mantenendo un’architettura robusta e priva di problematiche di inconsistenza.

#### Pattern di progettazione
Nel progetto sono stati adottati diversi pattern di progettazione per strutturare l’architettura della simulazione in modo modulare, riusabile e sicuro, semplificando la gestione delle entità e delle loro interazioni:
- **Strategy**: implementato attraverso l’utilizzo di `trait` per definire insiemi di dati e operazioni intercambiabili senza modificare direttamente le classi che li utilizzano.  
- **Observer**: rappresentato dall’uso di callback registrate nella View e invocate dal Controller ad ogni step di simulazione e in presenza di eventi di estinzione delle popolazioni di entità animali. 
- **Command**: le operazioni compiute durante l'esecuzione della simulazione sono modellate come effetti ZIO rappresentati come azioni autonome e componibili, separando la definizione dell’operazione dalla sua effettiva esecuzione.   
- **Singleton**: realizzato tramite l'impiego di `objects` e `companion objects`, che forniscono metodi statici permettendo di mantenere un solo punto di accesso alle operazioni.

<p align="center">
  <img src="../imgs/detailedArchitecture.png" alt="Architettura dettagliata" width="80%">
  <br>
  <em>Figura 2 – Architettura dettagliata</em>
</p>

#### Organizzazione del codice
Il progetto è organizzato in package che riflettono la suddivisione logica delle responsabilità secondo il pattern architetturale MVC.  
Di seguito è riportata una panoramica dei principali package e delle classi in essi contenute:
- `model`: rappresenta la logica del dominio e la struttura del mondo simulato.
  Include:
  - `model.entities`: contiene le entità dell’ecosistema, i loro comportamenti e attributi.
  - `model.managers`: contiene i componenti responsabili della gestione del ciclo di vita, del movimento e delle interazioni tra le entità.
- `controller`: contiene i componenti che gestiscono la logica di controllo dell’applicazione e orchestrano l’esecuzione della simulazione, fungendo da collegamento tra model e `view`:
- view: contiene i componenti che gestiscono la rappresentazione grafica della simulazione e l’interazione con l’utente.
  Include:
   - `view.panels`: contiene i componenti per la rappresentazione grafica e l’interazione con l’utente, quali i pannelli di controllo, visualizzazione dello stato e indicatori numerici delle popolazioni.
   - `view.utils`: raccoglie utilità grafiche per il disegno, il rendering e la gestione delle icone dell’interfaccia.
