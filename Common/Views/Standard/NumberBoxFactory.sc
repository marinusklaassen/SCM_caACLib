/*
FILENAME: NumberBoxFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

NumberBoxFactory {

	 *createInstance { |caller, class|

		var newInstance = NumberBox();
		class = class.asString();

		newInstance.background_(Color.white.alpha_(0));

		case
		{ class == "numberbox-synthbox-mapped-value" }
		{
			newInstance.background_(Color.clear.alpha_(0.1));
			newInstance.minDecimals = 1;
			newInstance.maxWidth = 65;
		}
		{ class == "numberbox-synthbox-wholenumber" }
		{
			newInstance.background_(Color.clear.alpha_(0.1));
		}
		{ class == "numberbox-patternbox-layers" }
		{
			newInstance.background_(Color.black.alpha_(0.1));
			newInstance.align = \center;
			newInstance.clipLo = 1;
			newInstance.clipHi = 20;
			newInstance.maxWidth = 23;
		};

		^newInstance;
	}
}
