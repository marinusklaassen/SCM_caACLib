/*
FILENAME: ProjectPersistanceViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

ProjectPersistanceViewFactory {

	*createInstance { |caller, class, contextID|
		var newInstance = ProjectPersistanceView(contextID);
		class = class.asString();
		if (caller.notNil, {
			newInstance.mainLayout.margins = 0!4;
		});
		^newInstance;
	}
}
