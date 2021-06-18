ScoreParamControlSpecWidget : ScoreWidgetBase {
	var <spec;

	init {
		model = (
			controlSpec: ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, ""),
			gui: nil
		);
		setValueFunction = {| argSpec|
			var controlSpec,code;
			if (argSpec.isKindOf(ControlSpec))
			{ controlSpec = argSpec }
			{ controlSpec = argSpec.asSpec; };

			code = controlSpec.asCompileString;
			"paramSpec setValueFunction".postln;
			controlSpec.class.postln;
			controlSpec.postln;
			code.class.postln;
			code.postln;



			model[\controlSpec] = controlSpec;
			model.changed(\controlSpec, controlSpec);
			model[\gui] = code;
			model.changed(\gui, code);
		};
		dependants = ();
		dependants[\action] = {|theChanger, what, argSpec|
			if(what == \controlSpec, {
				spec = argSpec;
				if (action.notNil) { action.value(argSpec) };
			});
		};
		model.addDependant(dependants[\action]);
	}

	spec_ { |argSpec| setValueFunction.value(argSpec) }

	makeGui { |argParent, argBounds|
		parent = argParent;
		bounds = argBounds.asRect;

		gui = ();

		canvas = CompositeView(parent, bounds)
		.background_(Color.yellow.alpha_(0.9));

		gui[\specText] = TextField(canvas,canvas.bounds.extent)
		.background_(Color.rand.alpha_(0))
		.string_(model[\gui])
		.action_({ |getSpec|
			var returnInterprettedCode = getSpec.value.interpret;
			if (returnInterprettedCode.notNil)
			{ "New ControlSpec asssigned".postln; setValueFunction.value(returnInterprettedCode) }
			{ "Spec is not assigned because of a writing mistake!".postln };
		});

		dependants[\gui] = {|theChanger, what, val|
			if(what == \gui, {
				gui[\specText].string_(val);
			});
		};
		model.addDependant(dependants[\gui]);

	}

	closeGui { model.removeDependant(dependants[\gui]) }

	loadState { |preset|
		setValueFunction.value(preset)
	}
}
