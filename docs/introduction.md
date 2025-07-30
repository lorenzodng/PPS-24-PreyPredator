
# Introduzione
Il progetto riguarda la realizzazione di una simulazione interattiva del modello Preda-Predatore di Lotka-Volterra, che descrive l’evoluzione nel tempo di due popolazioni interagenti in un ecosistema condiviso: prede e predatori. In questo modello, le prede fungono da fonte di cibo per i predatori, i quali influenzano a loro volta la crescita e la sopravvivenza delle prede attraverso eventi di predazione. Parallelamente, la sopravvivenza e la crescita dei predatori dipendono dalla disponibilità delle prede come risorsa alimentare, creando così un sistema dinamico interdipendente caratterizzato da oscillazioni nel numero di individui di entrambe le specie. 

Nel progetto presentato, tale schema è implementato attraverso tre tipologie di entità: *lupi*, *pecore* ed *erba*, in cui le pecore, che si nutrono dell’erba, svolgono il ruolo di prede, mentre i lupi rappresentano i predatori. Questa struttura a tre livelli consente di osservare interazioni più articolate rispetto al modello originale, mantenendo però le caratteristiche dinamiche fondamentali previste da Lotka-Volterra.  
La simulazione adotta un approccio multi-agente in cui gli individui si muovono in un ambiente dinamico condiviso e interagiscono tramite regole di predazione, riproduzione e mortalità, generando comportamenti collettivi tipici di un ecosistema preda-predatore.

L’applicazione è dotata di un’interfaccia grafica che consente all’utente di configurare il numero di entità iniziali, l’intervallo di generazione dell’erba e la quantità di erba generata ad ogni ciclo. L’utente può inoltre interagire tramite tre pulsanti principali: *Start*, per avviare la simulazione; *Stop*, per sospenderla temporaneamente; *Reset*, per riportare l'ecosistema allo stato iniziale.

Durante l’esecuzione, il simulatore fornisce un tracciamento continuo dell’andamento delle popolazioni nel corso del tempo, consentendo di osservare e analizzare l’evoluzione dell’ecosistema in base ai parametri iniziali scelti e all’interazione dinamica tra le entità.

La seguente relazione descrive in dettaglio l’intero ciclo di sviluppo del progetto: dal processo adottato, all’analisi dei requisiti, fino alla progettazione, implementazione e attività di testing, concludendosi con una retrospettiva sull’andamento generale del lavoro svolto.
