/*
* FILENAME: ScoreControlSpecView
*
* DESCRIPTION:
*         - Enter a control spec name or new instance by code. This view component takes care of the interpretation.
- The model is updated when a controlspec string is succesfully interpreted.
*
* AUTHOR: Marinus Klaassen (2012, 2021Q3)
*
ScoreControlSpecView(bounds:400@20).front().action = { |sender| sender.controlSpec.postln; sender.getState().postln; };
*/

ScoreControlSpecView : View {
	var controlSpec, mainLayout, textInput, labelError, model,dependants,setValueFunction;

	// Constructor
	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	// Extra property accesors
	controlSpec {
		^model[\controlSpec];
	}

	// Instance initialization
	initialize {
		this.initializeModelAndEvents();
		this.initializeView();
	}

	initializeModelAndEvents {
		var initSpec = nil.asSpec;
		model = (
			controlSpec: initSpec,
			rawSpecAsString: initSpec.asCompileString()
		);

		setValueFunction = {| rawSpecAsString|
			var interpretSpec; // TODO separate method. InterpretStringAsControlSpec
			// The user can evaluate a valid ControlSpec() or \name.asSpec.
			try {
				interpretSpec = rawSpecAsString.interpretPrint;
			}
			{ interpretSpec = nil; };
			if (interpretSpec.class == ControlSpec, {
				if (labelError.visible == true, {
					labelError.visible = false;
					labelError.string = "";
				});
				model[\controlSpec] = interpretSpec;
				model.changed(\controlSpec, interpretSpec);
				model[\rawSpecAsString] = rawSpecAsString;
				model.changed(\rawSpecAsString, rawSpecAsString);

			}, {
				labelError.visible = true;
				labelError.string = "Invalid input. Must be a valid ControlSpec.";
			});
		};
		dependants = ();

		dependants[\action] = {|theChanger, what|
			if(what == \controlSpec, {
				if (action.notNil) { action.value(this) };
			});
		};

		model.addDependant(dependants[\action]);
	}

	initializeView {
		mainLayout = VLayout();
		mainLayout.margins = 0!4;
		mainLayout.spacing = 0;
		this.layout = mainLayout;

		textInput = TextField();
		textInput.string = model[\rawSpecAsString];
		textInput.background = Color.yellow.alpha_(0.7);
		textInput.action = { | sender |
			setValueFunction.value(sender.string);
		};
		mainLayout.add(textInput);

		dependants[\textInput] = {|theChanger, what, val|
			if(what == \rawSpecAsString, {
				textInput.string = val;
			});
		};

		model.addDependant(dependants[\textInput]);

		labelError = StaticText();
		labelError.stringColor = Color.blue;
		labelError.visible = false;

		mainLayout.add(labelError, stretch: 1.0);
	}

    setSpecByString { | specAsString |
		var spec = specAsString.asSymbol.asSpec;
		// Only update a valid conversion of default spec like \freq.asSymbol
		if(spec.notNil, {
			model[\controlSpec] =  spec;
			model[\rawSpecAsString] = format("%%.asSpec", $\\, specAsString);
			model.changed(\controlSpec, model[\controlSpec]);
			model.changed(\rawSpecAsString, model[\rawSpecAsString]);
		});
	}

	getState {
		var state = Dictionary();
        state[\type] = "ControlSpecView";
		state[\rawSpecAsString] = model[\rawSpecAsString];
		^state;
	}

	loadState { | state |
		model[\rawSpecAsString] = state[\rawSpecAsString];
		setValueFunction.value(state[\rawSpecAsString]);
	}
}
