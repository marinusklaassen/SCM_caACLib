/*
FILENAME: PopUpMenuFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

PopUpMenuFactory {

 *createInstance { |caller, class|
		var newInstance = PopUpMenu();
		class = class.asString();
		^newInstance;
	}
}
