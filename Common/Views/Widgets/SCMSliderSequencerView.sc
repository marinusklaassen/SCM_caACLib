/*
 * FILENAME: SCMSliderSequencerView
 *
 * DESCRIPTION:
 *         Based on the sclang EZSlider, however it's a child of View and the child views are organised by a layout, so it can scale.
 *
 *         SCMSliderSequencerView(nil, 400@100, \db.asSpec, 0.5, "test").front().action_({ |v| v.postln; }).labelText_("Label test").value_(1.0.rand).value.postln;
 *         SCMSliderSequencerView()
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
a = SCMSliderSequencerView().front
b = a.getState();
a.editMode = true;
a.randomize
a.loadState(b);
a.spec = \freq.asSpec;
 */

SCMSliderSequencerView : SCMViewBase {
	var <spec, <labelText, <value, valueMapped;
	var <>name, <stepCount = 0, <mainLayout, stepsLayout, buttonSteps, <>proxySteps, sliders, numberBoxStepCount, labelStepCount;
	var <mainLayoutView, <sliderView, <labelView, <numberBoxView, unitView, controlSpecView, proxySteps, proxyStepsMapped, sliders;

	*new { | parent, bounds |
		^super.new(parent, bounds).init();
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
		var newSliderStep = Slider()
		.orientation_(\vertical)
		.minHeight_(100)
		.value_()
		.action_({ |sender|
			proxySteps[index] = sender.value;
			proxyStepsMapped[index] = spec.map(sender.value);
		});
		sliders.add(newSliderStep);
		proxySteps.add(initVal);
		proxyStepsMapped.add(spec.map(initVal));
		stepsLayout.add(newSliderStep);
		newSliderStep.value = initVal;
	}

	removeStep {
		sliders.pop().remove; // calling remove on controllers removes itself layout
		proxySteps.pop();
		proxyStepsMapped.pop();
	}

	uiMode { |mode|
		// intentional left empty
	}

	randomize {
		sliders.size do: { |i|
			sliders[i].value = 1.0.rand;
			proxySteps[i] = sliders[i].value;
			proxyStepsMapped[i] = spec.map(proxySteps[i]);
		};
	}


	toLow {
		sliders.size do: { |i|
			sliders[i].value = 0;
			proxySteps[i] = sliders[i].value;
			proxyStepsMapped[i] = spec.map(proxySteps[i]);
		};
	}

	toHigh {
		sliders.size do: { |i|
			sliders[i].value = 1;
			proxySteps[i] = sliders[i].value;
			proxyStepsMapped[i] = spec.map(proxySteps[i]);
		};
	}

	toCenter {
		sliders.size do: { |i|
			sliders[i].value = 0.5;
			proxySteps[i] = sliders[i].value;
			proxyStepsMapped[i] = spec.map(proxySteps[i]);
		};
	}

    spec_ { |argSpec|
		spec = argSpec;
		sliders.size do: { |i|
			proxyStepsMapped[i] = spec.map(proxySteps[i]);
		};
	}

	init {
		proxySteps = List();
		proxyStepsMapped = List();
		sliders = List();

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

		this.spec = ControlSpec();
		this.stepCount = 8;
		this.editMode = false;
	}

	getProxies {
		var result = Dictionary();
		result[this.name.asSymbol] = proxyStepsMapped;
		^result;
	}

	getState {
		var state = Dictionary();
		state[\slidersValues] = sliders collect: { |slider| slider.value; };
		^state;
    }

    loadState { |state|
		if (state.notNil, {
			this.stepCount = state[\slidersValues].size;
	   		state[\slidersValues] do: { |value, i|
				sliders[i].value = value;
				proxySteps[i] = value;
				proxyStepsMapped[i] = spec.map(value);
			};
		});
    }
}
