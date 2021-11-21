/*
FILENAME: ServerControlViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

ServerControlViewFactory {

 *createInstance { |caller, class|
		var newInstance = ServerControlView();
		class = class.asString();
		if (caller.notNil, {
			newInstance.mainLayout.margins = 0!4;
	    });
		^newInstance;
	}
}
