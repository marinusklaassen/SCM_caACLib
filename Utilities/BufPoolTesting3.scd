/*
Belangrijk!!

Creeer een load file and save file knop zodat een project opgeslagen kan worden
en weer kan worden opgeroepen in een later stadium.

Bij elke actie schrijf naar een default locatie op schijf
Implementeer de BufRecord objecxt
methodes die aanwezen moeten zijn:
- na opname sla buffer data als geluids bestand in de default locatie op schijf.
- methodes/parameters die nodig zijn:
= record
= doneAction
= makeGui
= recordSettings
= channels
= duration
= metadata

Verder wanneer een bank verandert is, update de onderliggende objecten die de buffers
gebruiken in hun processen. Dit om realtime sampling and processing mogelijk te maken.

Maak een bank slider/selector zodat de EmbedGui objecten en the Score & Controller interface ook
gebruik kan maken van deze buffers.
*/


// Debug swap files in bankview! Iets gaat fout maar wat?
// Misschien werkt de hernaming niet goed?
// De makeGui wordt niet gevonden?? Kijk dit na!

a = BufferPool();
a.makeGui;
