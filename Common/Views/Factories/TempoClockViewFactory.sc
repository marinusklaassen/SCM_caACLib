/*
FILENAME: TempoClockViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

TempoClockViewFactory {

 *createInstance { |caller, class|
		var newInstance = TempoClockView();
		class = class.asString();
		^newInstance;
	}
}
