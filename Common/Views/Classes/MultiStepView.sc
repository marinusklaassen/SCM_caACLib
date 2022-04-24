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
a = MultiStepView().front().editMode_(true)
b = a.getState();
a.editMode = true;
a.randomize
a.loadState(b);
*/

MultiStepView : View {
	var  <spec, <useSpec, <>name, <stepCount = 0, <mainLayout, stepsLayout, buttonSteps, <>proxySteps, numberBoxStepCount, labelSteps, textFieldValueOff, footerView, footerLayout, textFieldValueOn;
	var <valueOff, <valueOn;

	*new { | parent, bounds |
		^super.new(parent, bounds).init;
	}

	editMode_ { |mode|
		footerView.visible = mode;
	}

	valueOff_ { |value|
		// reformat this
		if (value.asSymbol() == value.asInteger().asSymbol(), { value = value.asInteger(); }, { value = value.asSymbol; });
		valueOff = value;
		textFieldValueOff.string = value;
		buttonSteps do: { |button,i | proxySteps[i] =  if (button.value == 0, valueOff, valueOn); };
	}

	valueOn_ { |value|
	    // duplicate code. reformat this
		if (value.asSymbol() == value.asInteger().asSymbol(), { value = value.asInteger(); }, { value = value.asSymbol; });
		valueOn = value;
		textFieldValueOn.string = value;
		buttonSteps do: { |button, i| proxySteps[i] = if (button.value == 0, valueOff, valueOn); };
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
		.action_({ |sender| proxySteps[index] = if (sender.value == 0, valueOff, valueOn); proxySteps;  } );
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
		valueOff = 0;
		valueOn = 1;
		proxySteps = List();
		buttonSteps = List();

		mainLayout = VLayout();
		mainLayout.margins = 0!4;
		this.layout = mainLayout;

		stepsLayout = HLayout();
		stepsLayout.margins = 0!4;
		stepsLayout.spacing = 2;

		mainLayout.add(stepsLayout);

		footerView = View();

		footerLayout = HLayout();
		footerLayout.margins = 0!4;
		footerView.layout = footerLayout;
		mainLayout.add(footerView);

		labelSteps = StaticText();
		labelSteps.string = "Steps:";
		labelSteps.minWidth = 40;
		labelSteps.maxWidth = 40;

		footerLayout.add(labelSteps);

		numberBoxStepCount = NumberBox();
        numberBoxStepCount.decimals = 0;
		numberBoxStepCount.clipLo = 1;
		numberBoxStepCount.clipHi = 128;
		numberBoxStepCount.action = { |sender| this.stepCount_(sender.value); };
		numberBoxStepCount.minWidth = 30;
		numberBoxStepCount.maxWidth = 30;
		footerLayout.add(numberBoxStepCount);

		footerLayout.add(StaticText().string_(" - "));

		textFieldValueOff = TextField();
		textFieldValueOff.string = valueOff;
		textFieldValueOff.action = { | sender | this.valueOff = sender.string; };
		textFieldValueOff.minWidth = 80;
		textFieldValueOff.maxWidth = 80;
		footerLayout.add(textFieldValueOff);

		textFieldValueOn = TextField();
		textFieldValueOn.string = valueOn;
		textFieldValueOn.action = { | sender | this.valueOn = sender.string; };
		textFieldValueOn.minWidth = 80;
		textFieldValueOn.maxWidth = 80;
		footerLayout.add(textFieldValueOn);

		footerLayout.add(nil);

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
		state[\stepValues] = buttonSteps collect: { |buttonStep| buttonStep.value; };
		state[\buttonStateValues] = [valueOff, valueOn];
		^state;
    }
//  [ note, rest, note, rest, rest, note, rest, note ]
    loadState { |state|
		// indien size is minder dan pop en remove het aantal.
		if (state.notNil, {
			this.stepCount = state[\stepValues].size;
			if (state[\buttonStateValues][0].notNil, {
				this.valueOff = state[\buttonStateValues][0];
			});
			if (state[\buttonStateValues][1].notNil, {
				this.valueOn = state[\buttonStateValues][1];
			});
			state[\stepValues] do: { |value, i|
				buttonSteps[i].valueAction = value;
			};
		});
    }
}
