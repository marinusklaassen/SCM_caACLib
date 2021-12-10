/*
FILENAME: SliderFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

SliderFactory {

	*createInstance { |caller, class, deviationID|
		var newInstance = Slider();
		class = class.asString();
		case
		{ class == "slider-horizontal"; }
		{
			newInstance.orientation = \horizontal;
		};
		^newInstance;
	}
}
