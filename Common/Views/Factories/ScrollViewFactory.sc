/*
FILENAME: ScrollViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

ScrollViewFactory {

 *createInstance { |caller, class|
		var newInstance = ScrollView();
		newInstance.canvas = View();
		class = class.asString();
		^newInstance;
	}
}
