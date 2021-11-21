/*
FILENAME: TextFieldFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

TextFieldFactory {

	*createInstance { |caller, class|
		var newInstance = TextField();
		class = class.asString();

		if (class.contains("text-patternboxparamview"), {
			newInstance.maxWidth = 90;
			newInstance.minWidth = 90;
		});

		^newInstance;
	}
}
