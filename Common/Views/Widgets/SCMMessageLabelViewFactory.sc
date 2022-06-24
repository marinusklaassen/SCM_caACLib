/*
FILENAME: SCMMessageLabelViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

SCMMessageLabelViewFactory {

	    *createInstance { |caller, class, message|

		var newInstance = SCMMessageLabelView();

		class = class.asString();

		// Type settings
		if (class.contains("message-error"), {
			newInstance.stringColor_(Color.red);
		});
		^newInstance;
	}
}
