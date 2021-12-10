/*
FILENAME: ScriptFieldViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

ScriptFieldViewFactory {

 *createInstance { |caller, class|
		var newInstance = ScriptFieldView();
		class = class.asString();
		^newInstance;
	}
}
