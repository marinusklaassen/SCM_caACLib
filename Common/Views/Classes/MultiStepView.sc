/*
 * FILENAME: MultiStepView
 *
 * DESCRIPTION:
 *         Based on the sclang EZSlider, however it's a child of View and the child views are organised by a layout, so it can scale.
 *
 *         MultiStepView(nil, 400@100, \db.asSpec, 0.5, "test").front().action_({ |v| v.postln; }).labelText_("Label test").value_(1.0.rand).value.postln;
 *         MultiStepView()
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 *
 * EXAMPLE:
a = MultiStepView().front
b = a.getState();
a.editMode = true;
a.randomize
a.loadState(b);
*/


MultiStepView : View {
	var <>name, <stepCount = 0, <mainLayout, stepsLayout, buttonSteps, <>proxySteps, numberBoxStepCount, labelStepCount;

	*new { | parent, bounds |
		^super.new(parent, bounds).init;
	}

	editMode_ { |mode|
		numberBoxStepCount.visible = mode;
		labelStepCount.visible = mode;
	}

	stepCount_ { |argStepCount|
		if(argStepCount > stepCount, {
			(argStepCount - stepCount) do: { this.addStep(); };
		}, if (argStepCount < stepCount, {
			(stepCount - argStepCount) do: { this.removeStep(); };
		}));
		numberBoxStepCount.value = argStepCount;
		stepCount = argStepCount;
	}

	addStep { |initVal = 0|
		var index = proxySteps.size;
		var newButtonStep = Button()
		.states_([["_", nil, Color.black.alpha_(0.5)], ["_", nil, Color.red.alpha_(0.5)]])
		  .minWidth_(30)
		.action_({ |sender| proxySteps[index] = sender.value; });
		buttonSteps.add(newButtonStep);
		proxySteps.add(initVal);
		stepsLayout.add(newButtonStep);
		newButtonStep.value = initVal;
	}

	removeStep {
		buttonSteps.pop().remove; // calling remove on controllers removes itself layout
		proxySteps.pop();
	}

	uiMode { |mode|
		// intentional left empty
	}

	spec_ { |argSpec|
		// intentional left empty
	}

	randomize {
		buttonSteps do: { |buttonStep| buttonStep.valueAction = 2.rand; };
	}

	init {
		proxySteps = List();
		buttonSteps = List();

		mainLayout = GridLayout();
		mainLayout.margins = 0!4;
		this.layout = mainLayout;

		stepsLayout = HLayout();
		stepsLayout.margins = 0!4;
		stepsLayout.spacing = 2;

		mainLayout.addSpanning(stepsLayout, 0, 0, columnSpan: 2);

		labelStepCount = StaticText();
		labelStepCount.string = "Step amount:";
		labelStepCount.maxWidth = 100;

		mainLayout.add(labelStepCount, 1, 0);

		numberBoxStepCount = NumberBox();
        numberBoxStepCount.decimals = 0;
		numberBoxStepCount.clipLo = 1;
		numberBoxStepCount.clipHi = 128;
		numberBoxStepCount.action = { |sender| this.stepCount_(sender.value); };
		numberBoxStepCount.maxWidth = 50;
		mainLayout.add(numberBoxStepCount, 1, 1, align: \left);

		mainLayout.add(nil, 1, 2);

		this.stepCount = 8;
		this.editMode = false;
	}

	getProxies {
		var result = Dictionary();
		result[this.name.asSymbol] = proxySteps;
		^result;
	}

	getState {
		var state = Dictionary();
		state[\stepValues] = proxySteps collect: { |buttonStep| buttonStep.value; };
		^state;
    }

    loadState { |state|
		// indien size is minder dan pop en remove het aantal.
		if (state.notNil, {
			this.stepCount = state[\stepValues].size;
			state[\stepValues] do: { |value, i| buttonSteps[i].value = value; };
		});
    }
}
