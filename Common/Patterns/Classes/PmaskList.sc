/*
FILENAME: PmaskList

DESCRIPTION: Mask the list indices range by a external shape.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

TODO: The index pattern is a fixed beta pattern generator. This needs to be made configurable.

EXAMPLE:
a = PmaskList((0,2..12),Pwhite(0,1.0),Pwhite(0,1.0),1,1).asStream;
a.nextT;
*/

PmaskList : ListPattern {
	var <>lo, <>hi, <>a, <>b, <>length;

	*new { arg list,lo = 0, hi = 1, a = 1, b = 1, length = inf;
		^super.newCopyArgs(list,lo,hi,a,b,length)
	}

	embedInStream { arg inval;
		var item;
		var loStr = lo.asStream,loVal;
		var hiStr = hi.asStream,hiVal;
		var aStr = a.asStream,aVal;
		var bStr = b.asStream,bVal;
		var n = list.size - 1,index;

		length.value(inval) do: {
			loVal = loStr.next(inval);
			hiVal = hiStr.next(inval);
			aVal = aStr.next;
			bVal = bStr.next;

			aVal.postln;

			index = Pbeta(loVal.next,hiVal.next,aVal.next,bVal.next).asStream.next(inval);

			index = round(index * n,1);
			item  = list[index].postln;
			inval = item.embedInStream(inval);
		};
		^inval;
	}

	storeArgs { ^[list,lo,hi,a,b,length] }
}
