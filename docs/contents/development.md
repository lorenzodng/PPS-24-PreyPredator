# Processo di sviluppo
Il progetto è stato sviluppato in modalità individuale seguendo un approccio a doppio livello. A livello macro, le attività sono state organizzate secondo un processo di tipo *a cascata*, con una sequenza lineare di fasi: prima l'analisi e la definizione dei requisiti, poi la progettazione, seguita dall’implementazione e infine dal testing. Tuttavia, all’interno della fase di implementazione è stato adottato un approccio più flessibile, combinando modalità *iterative*, incentrate sullo sviluppo ciclico di tutte le funzionalità, e modalità *incrementali*, focalizzate su gruppi ristretti di funzionalità da completare e consolidare prima di passare alle successive.

In coerenza con questa impostazione, per ottimizzare i tempi di sviluppo, si è scelto di non applicare rigidamente la filosofia del *Test Driven Development (TDD)*, pur realizzando *test unitari* e *test di integrazione* per la quasi totalità delle funzionalità implementate, al fine di verificare la correttezza del comportamento e delle interazioni attese tra i componenti del programma, favorendo così la manutenibilità del codice.

La pianificazione e il monitoraggio dell’avanzamento sono stati gestiti in forma autonoma attraverso la definizione e l’utilizzo di un *Product Backlog*, strutturato in forma tabellare. I requisiti funzionali sono stati scomposti in task granulari, organizzati per priorità e distribuiti su tre *sprint* di sviluppo, ciascuno della durata stimata di circa 20 ore. Ogni sprint è stato pianificato in modo da contenere un insieme coerente di funzionalità incrementali, con riesami individuali al termine di ciascun ciclo, in modo da mantenere una visione chiara dell’evoluzione del progetto, facilitando il controllo dell’avanzamento e la gestione delle dipendenze tra le attività.

Per quanto riguarda gli strumenti a supporto dello sviluppo, sono stati utilizzati:

- `Git` per il versionamento del codice sorgente;
- `GitHub` come repository remoto per l’archiviazione del codice e il tracciamento centralizzato delle versioni.

L’intero processo di sviluppo ha seguito i principi di una buona pratica ingegneristica del software, con particolare attenzione alla leggibilità del codice, alla coesione dei moduli e alla facilità di estensione e manutenzione del sistema.
