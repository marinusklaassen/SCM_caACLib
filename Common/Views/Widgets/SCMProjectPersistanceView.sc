/*
FILENAME: SCMProjectPersistanceView

DESCRIPTION: SCMProjectPersistanceView save and load SC objects as archive files

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
SCMProjectPersistanceView(bounds:400@50).front()
*/

SCMProjectPersistanceView : View {
	var <eventLoadProject, <>actionChanged, <>actionClearAll, <>actionNewItem, <>actionCloseAllViews, <>actionMidiEditingStateChanged, <eventSaveProject, fileMenu, utilitiesMenu, buttonShowUtilitiesMenu, buttonShowSessionMenu, sessionMenu, <buttonShowFileMenu, <buttonSave, <buttonSaveAs, <labelProjectfile, <mainLayout, <>data, <>projectfile, <>contextID;

	*new { |contextID, parent, bounds|
		^super.new(parent, bounds).initialize(contextID);
	}

	initialize { |contextID|
		this.contextID = contextID;
		this.intializeView();
		this.initializeEvents();
	}

	addView { |view|
		mainLayout.add(view, 0, 3);
	}

	intializeView {
		mainLayout = GridLayout();
		this.layout = mainLayout;

		buttonShowFileMenu = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "File")
		.action_({ fileMenu.front; });

		mainLayout.add(buttonShowFileMenu, 0, 0);

		buttonShowSessionMenu = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "Session")
		.action_({ sessionMenu.front; });

		mainLayout.add(buttonShowSessionMenu, 0, 1);

		buttonShowUtilitiesMenu = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "Utilities")
		.action_({ utilitiesMenu.front; });

		mainLayout.add(buttonShowUtilitiesMenu, 0, 2);

		fileMenu = Menu(
			MenuAction("Open")
			.action_({ this.load(); }),
			MenuAction("Save")
			.action_({ this.save(); }),
			MenuAction("Save as")
			.action_({ this.saveAs(); })
		);

		sessionMenu = Menu(
			MenuAction.separator.string_("Items"),
			MenuAction("New item")
			.action_({ if (actionNewItem.notNil, { actionNewItem.value(this); }); }),
			MenuAction("Hide all views")
			.action_({  if (actionCloseAllViews.notNil, { actionCloseAllViews.value(this); });}),
			MenuAction("Clear all")
			.action_({  if (actionClearAll.notNil, { actionClearAll.value(this); });}),
			MenuAction.separator.string_("MIDI"),
			MenuAction("Show MIDI editing")
			.action_({  if (actionMidiEditingStateChanged.notNil, { actionMidiEditingStateChanged.value(true); });}),
			MenuAction("Hide MIDI editing")
			.action_({  if (actionMidiEditingStateChanged.notNil, { actionMidiEditingStateChanged.value(false); });}),
		);

		utilitiesMenu = Menu(
			MenuAction.separator.string_("Tools"),
			MenuAction("SynthDef browser")
			.action_({ SynthDescLib.global.browse; }),
			MenuAction("Kill all servers")
			.action_({ Server.killAll; }),
			MenuAction.separator.string_("Post window"),
			MenuAction("Print specs")
			.action_({ Spec.specs keysValuesDo: { |spec| postln(spec); }; }),
			MenuAction("Print available scales")
			.action_({ Scale.names keysValuesDo: { |name| postln(name); }; }),
		);

		labelProjectfile = TextFieldFactory.createInstance(this);
		labelProjectfile.visible_(false);
		labelProjectfile.enabled_(false);
		mainLayout.addSpanning(labelProjectfile, row: 1, columnSpan: 4);

	}

	setProjectfile { |projectfile|
		this.projectfile = projectfile;
		labelProjectfile.string = projectfile;
		if (actionChanged.notNil, { actionChanged.value(this); });
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
			var pathName = PathName(filepath.value);
			this.saveData(pathName.fullPath);
			"project saved".postln;
		},{ "cancelled".postln; });
	}

	save {
		if (File.exists(this.projectfile), {
			this.saveData(this.projectfile);
		},{
			this.saveAs();
		});
	}

	saveData { |path|
		var stateFile = this.stateFile();
	    var state = Dictionary[\projectFilepath -> path];
		if (File.exists(this.projectfile) && (path == this.projectfile), {
			var archive = this.projectfile ++ ".archive";
			this.data.writeArchive(archive);
			postln(this.projectfile + "is archived to " + archive);
		});
    	this.invokeEvent(this.eventSaveProject);
		this.data.writeArchive(path);
		this.setProjectfile(path);
		postln("Projectdata is written to" + path);
		state.writeArchive(stateFile);
		postln(stateFile + "is updated");
		this.setProjectfile(path);
	}

	load {
		Dialog.openPanel({ |filepath|
			var pathName = PathName(filepath.value);
			("Loading" + pathName.fullPath).postln;
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
				this.loadData(data);
				this.setProjectfile(state[\projectFilepath]);
			});
		});
	}
}

