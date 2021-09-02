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



*/


ScorePatternScriptEditingView : View {
	var model, dependants, setValueFunction, textSeparateEditing, string;
	var <mainLayout, <textEditing, windowPopup, labelError;

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
		dependants = ();

		setValueFunction = { |script|
			model[\script] = script;
			model.changed(\script, script);
		};

		model.addDependant({|theChanger, what|
			if(action.notNil) { action.value(this) };
		});
	}

	initializeView {
		mainLayout = VLayout();
		mainLayout.margins = 0!4;

		this.layout = mainLayout;

		textEditing = TextView();
		textEditing.string = model[\script];
		textEditing.keyUpAction = {| ... args| // Zoek uit of de ook via sender, etc. etc. Dus meer named arguments
			this.prSetTextFieldHeightByText(textEditing.string);
			if (textSeparateEditing.notNil, { textSeparateEditing.string = textEditing.string; });

			if (args[1].ascii == 13 && args[2] == 524288) { setValueFunction.value(textEditing.string) };
		};
		textEditing.enterInterpretsSelection = false;
		textEditing.hasVerticalScroller = false;

		mainLayout.add(textEditing);

		labelError = StaticText();
		labelError.stringColor = Color.blue;
		labelError.visible = false;

		mainLayout.add(labelError, stretch: 1.0);

		this.prSetTextFieldHeightByText("");

		dependants[\textEditing] = {|theChanger, what, argString|
			textEditing.string_(argString);
		};

		model.addDependant(dependants[\textEditing]);
	}

	// Methods
	showPopup {
		if (windowPopup.isNil, {
			windowPopup = View(bounds:400@200);
			windowPopup.alwaysOnTop = true;
			windowPopup.name = "Pattern Script Editor";
			windowPopup.onClose = {
				model.removeDependant(dependants[\separateEditingView]);
				windowPopup = nil;
				textSeparateEditing = nil;
			};
			windowPopup.layout = VLayout();
			windowPopup.layout.margins = 0!4;
			textSeparateEditing = TextView();
			textSeparateEditing.string = textEditing.string;
			textSeparateEditing.keyUpAction = {| ... args|
				textEditing.string = textSeparateEditing.string;
			    this.prSetTextFieldHeightByText(textEditing.string);
				// alt enter commits change.
				if (args[1].ascii == 13 && args[2] == 524288) { setValueFunction.value(textSeparateEditing.string) };
			};
			textSeparateEditing.enterInterpretsSelection = false;
			dependants[\separateEditingView] = {|theChanger, what, value|
				textSeparateEditing.string = value;
			};
			windowPopup.layout.add(textSeparateEditing);
			// TODO error label ook in de popup toevoegen.
        });
		windowPopup.front;
	}

	setErrorText { |errorText|
		labelError.visible = true;
		labelError.string = errorText;
	}

	clearErrorText {
	    labelError.visible = false;
		labelError.string = "";
	}

	prSetTextFieldHeightByText { |text|
         var lineCount = text.findAll("\n").size + 1;
         textEditing.minHeight = Font.defaultSerifFace.size * 1.45 * lineCount;
         textEditing.maxHeight = Font.defaultSerifFace.size * 1.45 * lineCount;
	}

	// State behaviors
	loadState { |preset| this.string = preset[\script].asString }

	getState { ^model[\script].clone(); }
}