/*
FILENAME: Purno

DESCRIPTION: Pattern implementation of an urn based random selection principle from a list.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
*/

Purno : ListPattern {

	*new { arg list, repeats=1;
		^super.new(list, repeats);
	}

	embedInStream {  arg inval;
		var offsetValue, tempArray, prevItem;
		repeats.value(inval) * list.value(inval).size do: {
			var item, index;
			if (tempArray.size == 0, { tempArray = list.copy;  });
			// While new item is the same as the previous value or nil, select randomly a new element from the list.
			while ({var bool;
				if ((item == prevItem).or(item.isNil), {
					bool = true;
					},{
						bool = false;
						/* Evaluation class.isKindOf(class) doesn't work in this situation. Because instVarHash gives different number for each
						 * different setted class this if expression only work on non numerical objects. */
						if (item.isNumber != true && (prevItem.isNumber != true), { bool = item.instVarHash == prevItem.instVarHash });
				});
				bool;
			})
			{
				// generate a random index
				index = tempArray.size.rand;
				// grab item from tempArray with the new index
				item = tempArray[index];
			};
			prevItem = item;
			tempArray.removeAt(index);
			inval = prevItem.embedInStream(inval);
		};
		^inval;
	}

	storeArgs { ^[ list, repeats ] }
}
