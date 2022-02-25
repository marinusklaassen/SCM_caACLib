/*
FILENAME: ProjectPersistanceView

DESCRIPTION: ProjectPersistanceView save and load SC objects as archive files

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
ProjectPersistanceView(bounds:400@50).front()
*/

ProjectPersistanceView : View {
	var <eventLoadProject, <eventSaveProject, <buttonLoad, <buttonSave, <buttonSaveAs, labelProjectfile, <mainLayout, <>data, <>projectfile, <>contextID;

	*new { |contextID, parent, bounds|
		^super.new(parent, bounds).initialize(contextID);
	}

	initialize { |contextID|
		this.contextID = contextID;
		this.intializeView();
	    this.initializeEvents();
	}

	intializeView {
		mainLayout = GridLayout();
		this.layout = mainLayout;

		buttonLoad = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "open")
		.action_({ this.load(); });
		mainLayout.add(buttonLoad, 0, 0);

		buttonSave = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "save")
		.action_({ this.save(); });
		mainLayout.add(buttonSave, 0, 1);

		buttonSave = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "save as")
		.action_({ this.saveAs(); });
		mainLayout.add(buttonSave, 0, 2);

		labelProjectfile = TextFieldFactory.createInstance(this);
		labelProjectfile.visible_(false);
		labelProjectfile.enabled_(false);
		mainLayout.addSpanning(labelProjectfile, row: 1, columnSpan: 3);

	}

	setProjectfile { |projectfile|
		this.projectfile = projectfile;
		labelProjectfile.string = projectfile;
		labelProjectfile.visible_(true);
	}

	initializeEvents {
		eventLoadProject = ();
		eventSaveProject = ();
	}

	invokeEvent { |event|
		event.changed(this);
	}

	saveAs {
		Dialog.savePanel({ |filepath|
			var stateFile = this.stateFile();
			var state = Dictionary();
			var pathName = PathName(filepath.value);
			this.invokeEvent(this.eventSaveProject);
			this.data.writeArchive(pathName.fullPath);
			this.setProjectfile(pathName.fullPath);
			state[\projectFilepath] = pathName.fullPath;
			state.writeArchive(stateFile);
			"project saved".postln;
		},{ "cancelled".postln; });
	}

	save {
		if (File.exists(this.projectfile), {
			this.data.writeArchive(this.projectfile + ".archive");
			this.invokeEvent(this.eventSaveProject);
			this.data.writeArchive(this.projectfile);
		},{
			this.saveAs();
		});
	}

	load {
		Dialog.openPanel({ |filepath|
			var pathName = PathName(filepath.value);
			this.loadData(Object.readArchive(pathName.fullPath));
			this.setProjectfile(pathName.fullPath);
		},{ "cancelled".postln; });
	}

	loadData { |data|
        this.data = data;
		this.invokeEvent(this.eventLoadProject);
	}

	stateFile {
		var stateDir = Platform.userAppSupportDir ++ "/ExtensionsWorkdir/SCM_CaACLib/" ++ this.class.asString ++ "/";
		var stateFile = stateDir ++ this.contextID ++ ".state";
		File.mkdir(stateDir); // Create if not exists.
		^stateFile;
	}

	autoLoad {
		var data, stateFile = this.stateFile();
		if (File.exists(stateFile), {
			var state = Object.readArchive(stateFile);
			if (state[\projectFilepath].notNil && File.exists(state[\projectFilepath]), {
				data = Object.readArchive(state[\projectFilepath]);
				data.postln;
				this.loadData(data);
				this.setProjectfile(state[\projectFilepath]);
			});
		});
	}
}
