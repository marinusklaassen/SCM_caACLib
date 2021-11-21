/*
FILENAME: ProjectPersistanceViewFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

ProjectPersistanceViewFactory {

	*createInstance { |caller, class, deviationID|
		var newInstance = ProjectPersistanceView();
		class = class.asString();
		if (caller.notNil, {
			newInstance.mainLayout.margins = 0!4;
		});

		^newInstance;
	}
}
