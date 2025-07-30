## Testing
Per la verifica del corretto funzionamento del software è stato utilizzato il framework ScalaTest, che consente di scrivere test unitari e di integrazione in modo espressivo e leggibile.
Il codice adotta un approccio monadico per la gestione degli effetti, modellati tramite la libreria ZIO ed eseguiti in modo sincrono grazie all’impiego di un runtime dedicato e resi eseguibili attraverso l’uso esplicito di unsafe. Questo meccanismo consente di forzare l’esecuzione delle computazioni ZIO all'interno del contesto di test, assicurando che le asserzioni vengano realmente valutate e non rimangano semplici descrizioni non eseguite.

Il progetto adotta una metodologia di testing prevalentemente unitaria, con test mirati sulle singole entità, sui manager e sul controller, volti a verificare sia la correttezza delle funzionalità individuali, sia l’interazione tra i diversi componenti.

In base a questa strategia, il codice risulta coperto in modo esaustivo nei seguenti ambiti:
- **Test sul controller**: verifica del corretto funzionamento dei metodi di avvio, interruzione e reset della simulazione, con particolare attenzione alla gestione dello stato complessivo.
- **Test sulle entità**: verifica della corretta generazione delle entità, delle proprietà e dei comportamenti associati.
- **Test sui manager**: controllo della logica di simulazione, delle interazioni tra le entità e degli aggiornamenti dello stato globale dell'ecosistema.
- **Test sul mondo**: verifica delle operazioni di generazione, aggiornamento e rimozione delle entità all’interno dell'ecosistema.
Come si evince, la copertura dei test riguarda principalmente il funzionamento base e i comportamenti critici delle entità e dei manager, sebbene rimangano aree per ulteriori approfondimenti, come l’interazione fra più entità in simultanea e test orientati alla performance.

Grazie a questo approccio strutturato ai test automatizzati è possibile verificare rapidamente la correttezza delle modifiche apportate al codice del programma, prevedendo con maggiore affidabilità l’impatto sul funzionamento complessivo del sistema.
