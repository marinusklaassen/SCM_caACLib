/*
FILENAME: MessageLabelViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

MessageLabelViewFactory {

	    *createInstance { |caller, class, deviationID, message|

		var newInstance = MessageLabelView();

		class = class.asString();

		// Type settings
		if (class.contains("message-error"), {
			newInstance.stringColor_(Color.red);
		});
		^newInstance;
	}
}
