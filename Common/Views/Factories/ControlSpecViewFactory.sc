/*
FILENAME: ControlSpecViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

ControlSpecViewFactory {

 *createInstance { |caller, class |
		var newInstance = ControlSpecView();
		class = class.asString();
		^newInstance;
	}
}
