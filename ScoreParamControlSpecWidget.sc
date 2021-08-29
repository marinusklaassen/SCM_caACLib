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
			var controlSpec;
            // The user can evaluate a valid ControlSpec() or \name.asSpec.
			controlSpec = rawSpecAsString.interpretPrint;

            if (controlSpec.class == ControlSpec, {
				labelError.string = "";
	            labelError.maxHeight = 0;
				model[\controlSpec] = controlSpec;
				model.changed(\controlSpec, controlSpec);
				model[\rawSpecAsString] = rawSpecAsString;
				model.changed(\rawSpecAsString, rawSpecAsString);

            }, {
				labelError.string = "Invalid input. Must be a valid ControlSpec..";
				labelError.maxHeight = 50;
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
		this.layout = mainLayout;

		textInput = TextField();
		textInput.string = model[\rawSpecAsString];
		textInput.background = Color.yellow.alpha_(0.7);
		textInput.action = { | sender |
			setValueFunction.value(sender.string);
		};

		dependants[\textInput] = {|theChanger, what, val|
			if(what == \rawSpecAsString, {
				textInput.string = val;
			});
		};

		model.addDependant(dependants[\textInput]);

		labelError = StaticText();
		labelError.stringColor = Color.red;
		labelError.maxHeight = 0;
		mainLayout.add(textInput);
		mainLayout.add(labelError);
		mainLayout.add(nil, stretch: 1);
	}

	getState { ^model[\rawSpecAsString]; }

	loadState { | specAsString |
		// Update model
		setValueFunction.value(specAsString)
	}
}
