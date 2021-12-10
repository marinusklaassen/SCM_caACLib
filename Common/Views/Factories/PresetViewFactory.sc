/*
FILENAME: TextViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

PresetViewFactory {

 *createInstance { |caller, class|
		var newInstance = PresetView(caller.class.asString);
		class = class.asString();
		^newInstance;
	}
}
