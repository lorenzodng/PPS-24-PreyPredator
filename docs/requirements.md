# Analisi dei requisiti
In questo capitolo sono descritti in dettaglio i requisiti del sistema, suddivisi in: *requisiti di business*, *requisiti funzionali*, *requisiti non funzionali* e *requisiti di implementazione*. 

Ogni requisito è stato scelto in modo da essere verificabile e coerente con gli obiettivi del progetto.

## Requisiti di business
L'obiettivo del sistema è simulare un ecosistema virtuale costituito da entità viventi autonome, che interagiscono in un ambiente condiviso secondo regole biologiche semplificate.

Il sistema è progettato per supportare studi, osservazioni e visualizzazioni didattiche delle dinamiche preda-predatore in ambienti simulati, con il fine di analizzare l’evoluzione naturale delle popolazioni delle diverse specie nel corso del tempo.
- Obiettivi principali:
  - Simulare il comportamento autonomo delle entità viventi.
  - Visualizzare in tempo reale l’evoluzione delle popolazioni e dell’ambiente.
  - Offrire un’interfaccia utente per configurare i parametri iniziali e gestire la simulazione.

- Benefici attesi:
  - Supportare studi e osservazioni didattiche.
  - Analizzare l’evoluzione delle popolazioni in ambienti controllati.
  - Fornire uno strumento interattivo per comprendere le dinamiche preda-predatore.

## Modello di dominio
La simulazione è centralizzata su un *mondo* virtuale, popolato da entità lupi, pecore ed erba, che interagiscono ed evolvono secondo regole di predazione, riproduzione e mortalità.

- Mondo: rappresenta lo stato globale dell’ecosistema, incluse le posizioni spaziali e lo stato delle entità presenti.
- Lupo:
  - **Caratteristiche**: predatore che si muove nel mondo. Consuma pecore per ottenere *energia*, perde energia ad ogni movimento.
  - **Strategia di movimento**: al momento della creazione (tramite i parametri iniziali o per nascita) assume una direzione casuale; se nel mondo è presente almeno una pecora, si orienta automaticamente verso quella più vicina per predarla.
  - **Strategia di riproduzione**: se due lupi collidono e possiedono entrambi un livello di energia sufficiente, generano un nuovo lupo in una posizione adiacente.
  - **Criterio di estinzione**: muore se il suo livello di energia si esaurisce.
- Pecora:
  - **Caratteristiche**: erbivoro che si muove nel mondo. Consuma erba per recuperare energia, perde energia ad ogni movimento.
  - **Strategia di movimento**: al momento della creazione (tramite i parametri iniziali o per nascita) assume una direzione casuale; se nel mondo è presente almeno un'entità di erba, si orienta automaticamente verso quella più vicina per consumarla.
  - **Strategia di riproduzione**: se due pecore collidono e possiedono entrambe un livello di energia sufficiente, generano una nuova pecora in una posizione adiacente.
  - **Criterio di estinzione**: muore se il suo livello di energia si esaurisce o se viene predata.
- Erba: 
  - **Caratteristiche**: risorsa statica del mondo che cresce nel tempo. Può essere consumata dalle pecore.
  - **Posizione**: fissa, non si muove.
  - **Strategia di riproduzione**: generata automaticamente nel mondo a intervalli regolari.
  - **Criterio di eliminazione**: temporaneamente rimossa dal mondo quando consumata, per poi ricrescere successivamente.

## Requisiti funzionali
In questa sezione vengono descritti i requisiti funzionali del sistema, suddivisi in due categorie principali: *requisiti utente*, che definiscono le funzionalità utilizzabili dall’utente finale, e *requisiti di sistema*, che specificano i comportamenti e le responsabilità del sistema per soddisfare tali esigenze.

#### Requisiti utente
Il sistema deve fornire le seguenti funzionalità:
- Parametrizzazione iniziale della simulazione tramite interfaccia grafica:
  - Numero iniziale di lupi;
  - Numero iniziale di pecore;
  - Quantità iniziale di erba;
  - Intervallo temporale di generazione automatica dell'erba;
  - Quantità di erba generata a ogni intervallo.
- Controllo dell’andamento della simulazione:
  - Avviare la simulazione;
  - Fermare la simulazione;
  - Resettare la simulazione.
- Visualizzazione dell’ambiente simulato:
  - Rappresentazione dinamica delle entità e delle loro interazioni.
- Monitoraggio in tempo reale:
  - Visualizzazione della popolazione corrente di lupi;
  - Visualizzazione della popolazione corrente di pecore;
  - Visualizzazione della quantità di erba presente.
  
#### Requisiti di sistema
Il sistema è responsabile di:
- Validare la configurazione iniziale inserita dall’utente prima di avviare la simulazione; in caso di configurazione non valida, impedire l’avvio e mostrare un messaggio di errore;
- Creare e inizializzare l’ambiente di simulazione in base ai parametri forniti dall’utente;
- Gestire il ciclo di vita delle entità (lupi, pecore, erba), inclusi:
  - Movimento autonomo di lupi e pecore, secondo le regole specificate;
  - Gestione delle collisioni tra entità, includendo predazione, consumo e riproduzione;
  - Rimozione delle entità morte per esaurimento di energia, per predazione o per consumo;
  - Aggiornamento e gestione dell’energia delle entità animali, con incrementi e decrementi basati sulle azioni compiute;
  - Rigenerazione ciclica dell’erba, rispettando intervalli temporali e quantità determinati dai parametri di configurazione.
- Aggiornare lo stato globale del mondo simulato ogni 30 ms;
- Gestire la sincronizzazione e l’aggiornamento dell’interfaccia grafica, includendo:
  - Rappresentazione grafica aggiornata delle entità;
  - Visualizzazione in tempo reale degli indicatori numerici delle popolazioni.
- Gestire il comportamento dei comandi di controllo ricevuti dall’utente, avviando, interrompendo o reimpostando conseguentemente la simulazione;
- Terminare automaticamente la simulazione in caso di estinzione totale delle popolazioni di lupi e pecore.

## Requisiti non funzionali
I seguenti requisiti non funzionali descrivono i vincoli di prestazioni, usabilità e robustezza che il sistema deve garantire per assicurare un corretto utilizzo e stabilità della simulazione.
- I parametri iniziali della simulazione devono poter essere configurati tramite controlli numerici prima dell’avvio, senza la necessità di consultare istruzioni esterne.
- I valori configurabili tramite i controlli dell'interfaccia devono rispettare i seguenti intervalli:
  - Entità animali iniziali: 1–500 unità per lupi e pecore (almeno una unità totale tra le due specie);
  - Erba iniziale: 0–1000 unità;
  - Intervallo di crescita dell’erba: 500–5000 ms;
  - Quantità di erba generata a ogni ciclo: 20–300 unità.
- L'interfaccia grafica deve aggiornarsi ogni 30 ms senza sospendere l’esecuzione della simulazione e l’interazione con l’utente.
- La visualizzazione dell’ambiente simulato deve adattarsi automaticamente alle dimensioni della finestra, mantenendo leggibilità e proporzioni corrette.

  Le prove di adattabilità sono state eseguite su risoluzioni comprese tra 1280×720 e 1920×1080 pixel.
    
- La simulazione deve evitare crash indipendentemente dal numero di entità presenti (da 0 fino al massimo supportato).
  
  Le prove di stabilità sono state eseguite su una macchina con le seguenti caratteristiche:
    - CPU: Six-Core, 3.2 GHz;
    - RAM: 8 GB.

- Il sistema deve garantire un’elevata manutenibilità ed estendibilità, consentendo interventi di manutenzione correttiva ed evolutiva senza modificare direttamente i componenti esistenti.

## Requisiti di implementazione
La seguente lista riporta gli strumenti e le tecnologie utilizzati per implementare il progetto, evidenziando le scelte tecnologiche adottate:
- `Scala 3` come linguaggio di programmazione per lo sviluppo del progetto;
- `ZIO` come libreria per la gestione degli effetti e l’esecuzione monadica;
- `Swing` come libreria per la realizzazione dell’interfaccia grafica;
- `ScalaTest` come framework per la scrittura e l’esecuzione dei test, secondo lo stile `AnyFunSuite`;
- `SBT` come strumento per la compilazione, la gestione delle dipendenze e l’automazione delle build.
