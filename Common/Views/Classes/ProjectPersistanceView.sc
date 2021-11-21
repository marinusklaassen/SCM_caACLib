/*
FILENAME: ProjectPersistanceView

DESCRIPTION: ProjectPersistanceView save and load SC objects as archive files

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
ProjectPersistanceView(bounds:400@50).front()
*/

ProjectPersistanceView : View {
	var <eventLoadProject, <eventSaveProject, <buttonLoad, <buttonSave, labelProjectName, <mainLayout, <>data, <projectFilename;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	initialize {
		this.intializeView();
	    this.initializeEvents();
	}

	intializeView {
		mainLayout = GridLayout();
		this.layout = mainLayout;

	    labelProjectName = TextFieldFactory.createInstance(this);
		labelProjectName.visible_(false);
		mainLayout.addSpanning(labelProjectName, columnSpan: 2);

		buttonLoad = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "open project")
		.action_({ this.load(); });
		mainLayout.add(buttonLoad, 1, 0);

		buttonSave = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "save project")
		.action_({ this.save(); });

		mainLayout.add(buttonSave, 1, 1);
	}

	setProjectFilename { |projectFilename|
		labelProjectName.string = projectFilename;
		labelProjectName.visible_(true);
	}

	initializeEvents {
		eventLoadProject = ();
		eventSaveProject = ();
	}

	invokeEvent { |event|
		event.changed(this);
	}

	save {
		Dialog.savePanel({ |filepath|
			var pathName = PathName(filepath.value);
			this.invokeEvent(this.eventSaveProject);
			this.data.writeArchive(pathName.fullPath);
			this.setProjectFilename(pathName.fileNameWithoutExtension);
			"project saved".postln;
		},{ "cancelled".postln; });
	}

	load {
		Dialog.openPanel({ |filepath|
			var pathName = PathName(filepath.value);
			this.data = Object.readArchive(pathName.fullPath);
			this.invokeEvent(this.eventLoadProject);
			this.setProjectFilename(pathName.fileNameWithoutExtension);
		},{ "cancelled".postln; });
	}
}
