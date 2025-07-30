# Retrospettiva
In questa sezione si analizza il processo di sviluppo adottato nel progetto, per poi offrire riflessioni conclusive e suggerire possibili sviluppi futuri.

## Descrizione degli sprint
Lo sviluppo del progetto ha seguito un approccio iterativo-incrementale articolato in tre sprint, ciascuno orientato all’introduzione progressiva delle funzionalità principali e al consolidamento dell’architettura del sistema.

#### Sprint 1
In questa prima fase, l’attenzione è stata rivolta alla modellazione della logica di base dell’ecosistema: sono stati definiti i parametri iniziali delle entità e implementati i movimenti casuali, insieme alla logica di collisione e riproduzione.  
Parallelamente, è iniziata la costruzione dell’interfaccia grafica, con particolare attenzione alla visualizzazione delle entità e all’integrazione iniziale dei pulsanti di controllo della simulazione.

#### Sprint 2
Durante il secondo sprint si è lavorato principalmente al completamento e al perfezionamento delle funzionalità avviate in precedenza, in particolare sul movimento guidato delle entità e sull’integrazione della logica per l’aggiornamento dinamico dello stato dell’ecosistema.  
Sono stati inoltre collegati i pulsanti di controllo dell'esecuzione alla logica del Controller, e introdotte funzionalità di configurazione dinamica mediante spinner.  
In questa fase sono stati anche avviati i primi interventi di refactoring, mirati a migliorare l’organizzazione e la leggibilità del codice.

#### Sprint 3
Nel terzo e ultimo sprint, l’obiettivo principale è stato il completamento dello sviluppo della logica della simulazione, con particolare attenzione alla gestione delle collisioni tra entità della stessa popolazione, alla verifica dei parametri iniziali e all’aggiornamento in tempo reale dell'ecosistema.  
È stato inoltre arricchito il dettaglio dell’interfaccia grafica con l’introduzione di etichette descrittive del conteggio delle entità di ogni popolazione.  
Gran parte del tempo è stata infine dedicata al refactoring approfondito e al miglioramento della documentazione, con l’obiettivo di garantire una maggiore manutenibilità e comprensibilità del progetto.

## Commenti finali e sviluppi futuri
Alla luce dei risultati ottenuti, è possibile valutare come l’adozione di un’architettura modulare, unita all'utilizzo della libreria monadica ZIO, abbia apportato significativi benefici, soprattutto nella gestione atomica e sicura delle informazioni condivise tra i componenti attivi del sistema.  
Tale approccio ha consentito, inoltre, di preservare una netta separazione tra la logica funzionale pura e la gestione dei side effects, favorendo modularità, testabilità e una maggiore robustezza complessiva del sistema.

La struttura modulare e la chiara separazione delle responsabilità rendono il sistema favorevole a futuri sviluppi, permettendo l’aggiunta di ulteriori funzionalità volte ad arricchire il modello di simulazione sviluppato, come l’estensione a nuove tipologie di entità o l’introduzione di comportamenti interattivi più complessi e dinamiche ecologiche avanzate, realizzabili senza stravolgere la logica esistente.  
Analogamente, sono facilitate estensioni anche per l’interfaccia utente, che può essere ampliata o migliorata mediante una rappresentazione grafica più dettagliata delle entità o un’interazione più ricca tramite controlli aggiuntivi.

In conclusione, il progetto ha pienamente raggiunto gli obiettivi iniziali, offrendo una simulazione fedele al modello di Lotka-Volterra con un'interfaccia utente interattiva e reattiva, e rappresentando al contempo un’opportunità per applicare i principi della programmazione funzionale in un contesto concreto. 
