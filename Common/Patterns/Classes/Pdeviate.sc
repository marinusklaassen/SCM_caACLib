/*
FILENAME: Pdeviate

DESCRIPTION: avg and deviate input stream mapping to lo and hi values.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
*/

Pdeviate : Pattern {
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
			lo = b * 0.5 - a;
			hi = b * 0.5 + a;

			inval = [lo,hi].yield;
		};
		^inval
	}
	storeArgs { ^[avg,dev] }
}