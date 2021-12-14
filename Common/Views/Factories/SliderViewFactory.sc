/*
FILENAME: SliderViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

SliderViewFactory {

	*createInstance { |caller, class, controlSpec, initVal, labelText|
		var newInstance = SliderView();
		class = class.asString();
		newInstance.numberBoxView
		.background_(Color.white.alpha_(0.5))
		.maxWidth_(60).minWidth_(60);
		^newInstance;
	}
}
