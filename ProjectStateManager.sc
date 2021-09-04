/*
FILENAME: ProjectSaveAndLoadView

DESCRIPTION: ProjectSaveAndLoadView save and load SC objects as archive files

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
ProjectSaveAndLoadView(bounds:400@50).front()
*/


ProjectSaveAndLoadView : View {
	var <eventLoadProject, <eventSaveProject, <buttonLoad, <buttonSave, <mainLayout, <>projectData;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	initialize {
		this.intializeView();
	    this.initializeEvents();
	}

	intializeView {
		mainLayout = HLayout();
		this.layout = mainLayout;

		buttonLoad = Button()
		.states_([["load project", Color.black, Color.red.alpha_(0.8)]])
		.action_({ this.load(); })
		.font_(Font("Menlo",14));
		this.layout.add(buttonLoad);

		buttonSave = Button()
		.states_([["save project", Color.black, Color.red.alpha_(0.8)]])
		.action_({ this.save(); })
		.font_(Font("Menlo",14));
		this.layout.add(buttonSave);
	}

	initializeEvents {
		eventLoadProject = ();
		eventSaveProject = ();
	}

	invokeEvent { |event|
		event.changed(this);
	}

	save {
		Dialog.savePanel({ |path|
			this.invokeEvent(this.eventSaveProject);
			this.projectData.writeArchive(path);
			"project saved".postln;
		},{ "cancelled".postln; });
	}

	load {
		Dialog.openPanel({ |filepath|
			this.projectData = Object.readArchive(filepath.value);
			this.invokeEvent(this.eventLoadProject);
			"project is loaded".postln;
		},{ "cancelled".postln; });
	}
}
