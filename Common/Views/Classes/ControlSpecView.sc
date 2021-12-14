/*
FILENAME: ControlSpecView

DESCRIPTION: Enter a control spec name or new instance by code. This view component takes care of the interpretation.
- The model is updated when a controlspec string is succesfully interpreted.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

ControlSpecView(bounds:400@20).front().action = { |sender| sender.controlSpec.postln; sender.getState().postln; };
*/

ControlSpecView : View {
	var controlSpec, mainLayout, textInput, <>action, labelError, model,dependants,setValueFunction;

	// Constructor
	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	// Extra property accesors
	controlSpec {
		^model[\controlSpec];
	}

	spec {
		^model[\controlSpec];
	}

	specAsString {
		^model[\rawSpecAsString];
	}

	specAsString_ { | specAsString |
		var spec = specAsString.asSymbol.asSpec;
		// Only update a valid conversion of default spec like \freq.asSymbol
		if(spec.notNil, {
			model[\controlSpec] =  spec;
			model[\rawSpecAsString] = format("%%.asSpec", $\\, specAsString);
			model.changed(\controlSpec, model[\controlSpec]);
			model.changed(\rawSpecAsString, model[\rawSpecAsString]);
		});
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
				labelError.clear();
				model[\controlSpec] = interpretSpec;
				model.changed(\controlSpec, interpretSpec);
				model[\rawSpecAsString] = rawSpecAsString;
				model.changed(\rawSpecAsString, rawSpecAsString);
				if (this.action.notNil, { |sender| this.action.value(this) });

			}, {
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

		textInput = TextFieldFactory.createInstance(this, "text-controlspec");
		textInput.string = model[\rawSpecAsString];
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
		labelError = MessageLabelViewFactory.createInstance(this, class: "message-error");
		mainLayout.add(labelError, stretch: 1.0);
	}

	getState {
		var state = Dictionary();
        state[\type] = this.class.asString;
		state[\rawSpecAsString] = model[\rawSpecAsString];
		^state;
	}

	loadState { | state |
		model[\rawSpecAsString] = state[\rawSpecAsString];
		setValueFunction.value(state[\rawSpecAsString]);
	}
}
