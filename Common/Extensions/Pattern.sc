/*
FILENAME: Pattern

DESCRIPTION: Pattern Extensions

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
Pbeta(0, 1.0, 0.2, 0.2).plotHisto
Pbeta(0, 1.0, 0.5, 0.5).plotHisto(2000)
*/


+Pattern {
	plotHisto  { | size=1000 |
		^this.asStream.nextN(size).plotHisto;
	}
}

