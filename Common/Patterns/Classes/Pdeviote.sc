/*
FILENAME: Pdeviote

DESCRIPTION: A variant of Pdeviate.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
*/

Pdeviote : Pattern {
	var <>avg, <>dev;
	*new { arg avg = 0, dev = 0;
		^super.newCopyArgs(avg, dev)
	}
	embedInStream { arg inval;
		var lo,hi,a,b,
		avgStr = avg.asStream,
		devStr = dev.asStream;

		inf do: {
			a = avgStr.next;
			b = devStr.next;
			lo = a - (b * a * 0.5);
			hi = b * a * 0.5 + a;

			inval = [lo,hi].yield;
		};
		^inval
	}
	storeArgs { ^[avg,dev] }
}
