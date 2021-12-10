/*
FILENAME: TextViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

TextViewFactory {

 *createInstance { |caller, class|
		var newInstance = TextView();
		class = class.asString();

		if (class.contains("text-patternbox-environment-script"), {
		    newInstance.minHeight = 150;
			newInstance.maxHeight = 150;
		});

		^newInstance;
	}
}
