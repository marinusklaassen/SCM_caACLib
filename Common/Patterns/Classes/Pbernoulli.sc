/*
FILENAME: Pbernoulli

DESCRIPTION: Stacked coin tossed with random weight.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
a = Pbernoulli(5,0.1,inf).asStream;
b = a.nextN(200);
b.plot;
b.histo(100,0,1.0).plot;
*/

Pbernoulli : Pattern {
	var <>n, <>weight, <>length;
	*new { arg n=3, weight=0.5, length=inf;
		^super.newCopyArgs(n, weight, length)
	}
	storeArgs { ^[n, weight,length] }
	embedInStream { arg inval;
		var 	nStr = n.asStream,
		weightStr = weight.asStream,
		nVal,weightVal,return;
		length.value(inval).do({
			nVal = nStr.next(inval);
			weightVal = weightStr.next(inval);
			if(nVal.isNil or: { weightVal.isNil }) { ^inval };
			return = Mix.fill(nVal, { if (weightVal.coin) { 1 } { 0 } }) / nVal;

			inval = return.yield;
		});
		^inval;
	}
}
