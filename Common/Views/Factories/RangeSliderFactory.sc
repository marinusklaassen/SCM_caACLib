/*
FILENAME: RangeSliderFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

RangeSliderFactory {

	*createInstance { |caller, class|
		var newInstance = RangeSlider();
		class = class.asString();

		case
		{ class == "slider-horizontal"; }
		{
			newInstance.orientation = \horizontal;
		};

		^newInstance;
	}
}
