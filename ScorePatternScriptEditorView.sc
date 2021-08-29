/*
 * FILENAME: ScorePatternScriptEditingView -> TODO rename ScoreEditingView
 *
 * DESCRIPTION:
 *     - Input text + window dialog to editing pattern (code-first).
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 *
 *
ScorePatternScriptEditingView(bounds:400@200).front().showPopup().action = { | sender | sender.string.postln; }

TODO ().class. // () is a event. TODO: try to make the register events and delegates more clear in the code.

*/

ScorePatternScriptEditingView : View {
	var model, dependants, setValueFunction, textSeparateEditing, string;
	var mainLayout, textEditing, separateEditingParentView;

	// Constructor
	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	// Extra property accesors
    string_ { | string |
		setValueFunction.value(string);
	}

	string { ^model[\script] }

	// Instance initialization
	initialize {
		this.initializeModelAndEvents();
		this.initializeView();
	}

	initializeModelAndEvents {
		model = (script: string);
		setValueFunction = { |script|
			model[\script] = script;
			model.changed(\script, script);
		};
		dependants = ();
		dependants[\action] = {|theChanger, what, string|
			if(action.notNil) { action.value(this, string) };
		};
		model.addDependant(dependants[\action]);
	}

	initializeView {
		mainLayout = StackLayout();

		this.layout = mainLayout;

		textEditing = TextView();
		textEditing.background = Color.white.alpha_(0.5);
		textEditing.string = model[\script];
		textEditing.keyDownAction = {| ... args| // Zoek uit of de ook via sender, etc. etc. Dus meer named arguments
			textSeparateEditing.string = textEditing.string;
			if (args[1].ascii == 13 && args[2] == 524288) { setValueFunction.value(textEditing.string) };
		};
		textEditing.enterInterpretsSelection = false;
		textEditing.hasVerticalScroller = false;

		mainLayout.add(textEditing);

		dependants[\textEditing] = {|theChanger, what, argString|
			textEditing.string_(argString);
		};

		model.addDependant(dependants[\textEditing]);
	}

	// Methods
	showPopup {
		if (separateEditingParentView.isNil, {
			separateEditingParentView = View(bounds:400@200);
			separateEditingParentView.alwaysOnTop = true;
			separateEditingParentView.name = "Pattern Script Editor";
			separateEditingParentView.onClose = {
				model.removeDependant(dependants[\separateEditingView]);
				separateEditingParentView = nil;
				textSeparateEditing = nil;
			};
			textSeparateEditing= TextView(separateEditingParentView);
			textSeparateEditing.background = Color.white.alpha_(0.5);
			textSeparateEditing.string = model[\script];
			textSeparateEditing.keyDownAction = {| ... args|
				textEditing.string = textSeparateEditing.string;
				// alt enter commits change.
				if (args[1].ascii == 13 && args[2] == 524288) { setValueFunction.value(textSeparateEditing.string) };
			};
			textSeparateEditing.enterInterpretsSelection = false;
			dependants[\separateEditingView] = {|theChanger, what, value|
				textSeparateEditing.string = value;
			};
        });
		separateEditingParentView.front;
	}

	// State behaviors
	loadState { |preset| this.string = preset[\script].asString }

	getState { ^model[\script].clone(); }
}