/*
 * FILENAME: SCMKnobSequencerView
 *
 * DESCRIPTION:
 *         Based on the sclang EZSlider, however it's a child of View and the child views are organised by a layout, so it can scale.
 *
 *         SCMKnobSequencerView(nil, 400@100, \db.asSpec, 0.5, "test").front().action_({ |v| v.postln; }).labelText_("Label test").value_(1.0.rand).value.postln;
 *         SCMKnobSequencerView()
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
a = SCMKnobSequencerView().front.editMode_(true).spec_(\freq.asSpec)
b = a.getState();
a.editMode = true;
a.randomize
a.proxySteps;
a.proxyStepsMapped;
a.loadState(b);
a.spec = \freq.asSpec;
Server.killAll

*/

SCMKnobSequencerView : SCMViewBase {
	var <spec, <labelText, <value, valueMapped;
	var <>name, <stepCount = 0, <mainLayout, prevClickedSelector, stepsLayout, stepLayouts, <selectors, buttonSteps, <proxySteps, sliders, numberBoxStepCount, popupMode;
	var <mainLayoutView, prevSlider, <sliderView, <labelView, <sequencerMode, <numberBoxView, unitView, controlSpecView ,<proxyStepsMapped, sliders, currentSelectedIndex;

	*new { | parent, bounds |
		^super.new(parent, bounds).init();
	}

	editMode_ { |mode|
		numberBoxStepCount.visible = mode;
		popupMode.visible = mode;
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

	setMode { |mode|
		if (sequencerMode != mode) {

			if (mode == "manual", {
				// Proxy list
				while({ proxySteps.size > 1 }, { proxySteps.pop; });
				while({ proxyStepsMapped.size > 1 }, { proxyStepsMapped.pop; });
				selectors do: { |selector| selector.visible_(true); };
				// Point to first stap
				this.setStep(0, sliders.first.value);
				this.selectStep(selectors.first, sliders.first, 0);
			}, {
				if(mode == "sequencer", {
					selectors do: { |selector| selector.visible_(false); };
					sliders do: { |slider, i|
						slider.background = Color.black.alpha_(0.2);
						if (proxySteps[i].isNil, { proxySteps.add(slider.value); }, { proxySteps[i] = slider.value; });
						if (proxyStepsMapped[i].isNil, { proxyStepsMapped.add(spec.map(slider.value);); }, { proxyStepsMapped[i] = spec.map(slider.value); });
					};
				});
			});
			sequencerMode = mode;
		};
	}

	selectStep { |selector, slider, index|
		var color = Color.black.alpha_(0.8);
		if (prevClickedSelector.notNil, {
			prevClickedSelector.states_([[nil, Color.white.alpha_(0.5), Color.white.alpha_(0.5)]]);
			prevSlider.background = Color.black.alpha_(0.2);
		});
		currentSelectedIndex = index;
		selector.states = [["", color, color]];
		slider.background = Color.blue.alpha_(0.7);
		prevClickedSelector = selector;
		prevSlider = slider;
		this.setStep(0, slider.value);
	}

	setStep { |index, value|
		proxySteps[index] = value;
		proxyStepsMapped[index] = spec.map(value);
	}

	addStep { |initVal = 0|
		var index = proxySteps.size;
		var selector = Button()
		.states_([[nil, Color.white.alpha_(0.5), Color.white.alpha_(0.5)]])
		.action_({ |sender| this.selectStep(sender, sliders[index], index); })
		.maxHeight_(15)
		.minHeight_(15)
		.visible_(sequencerMode == "manual");

		var knop = Knob()
		.value_(initVal)
		.background_(Color.black.alpha_(0.2))
		.action_({ |sender|
			if (sequencerMode == "sequencer", {
				this.setStep(index, sender.value);
			},{
				if (sequencerMode == "manual" && index == currentSelectedIndex, {
					this.setStep(0, sender.value);
				});
			});
		});
		var stepLayout = VLayout();
		stepLayout.add(knop);
		stepLayout.add(selector);
		stepLayouts.add(stepLayout);

		if (sequencerMode == "sequencer", {
			proxySteps.add(0);
			proxyStepsMapped.add(spec.map(0));
		});

		sliders.add(knop);
		selectors.add(selector);
		stepsLayout.add(stepLayout);
	}

	removeStep {
		sliders.pop().remove; // calling remove on controllers removes itself layout
		selectors.pop().remove;
		if (sequencerMode == "sequencer", {
			proxySteps.pop();
			proxyStepsMapped.pop();
		});
	}

	uiMode { |mode|
 	// intentional left empty
	}

	randomize {
		sliders.size do: { |i|
			sliders[i].value = 1.0.rand;

			if (sequencerMode == "sequencer", {
				proxySteps[i] = sliders[i].value;
				proxyStepsMapped[i] = spec.map(sliders[i].value);
			},{
				if (sequencerMode == "manual" && i == currentSelectedIndex, {
					proxySteps[0] = sliders[i].value;
					proxyStepsMapped[0] = spec.map(sliders[i].value);
				});
			});
		};
	}

	setMainSequencerPosition { |position|
		if ((sequencerMode == "manual") && (position < sliders.size), {
			this.selectStep(selectors[position], sliders[position], position);
		});
	}

	setMainSequencerMode { |mode|
		this.setMode(mode);
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
		selectors = List();
		stepLayouts = List();
		mainLayout = GridLayout();
		mainLayout.margins = 0!4;
		this.layout = mainLayout;

		stepsLayout = HLayout();
		stepsLayout.margins = 0!4;
		stepsLayout.spacing = 2;

		mainLayout.addSpanning(stepsLayout, 0, 0, columnSpan: 2);

		popupMode = PopUpMenu();
		popupMode.toolTip = "Set the sequence mode to sequencer or manual selector";
		popupMode.items = ["sequencer", "manual"];
		popupMode.maxWidth = 100;
		popupMode.action = { |sender|
			this.setMode(sender.item);
		};

		mainLayout.add(popupMode, 1, 0);

		numberBoxStepCount = NumberBox();
        numberBoxStepCount.decimals = 0;
		numberBoxStepCount.toolTip = "Sets the amount of steps";
		numberBoxStepCount.clipLo = 1;
		numberBoxStepCount.clipHi = 128;
		numberBoxStepCount.action = { |sender| this.stepCount_(sender.value); };
		numberBoxStepCount.maxWidth = 50;
		mainLayout.add(numberBoxStepCount, 1, 1, align: \left);

		mainLayout.add(nil, 1, 2);
		sequencerMode = "sequencer";
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
		state[\mode] = popupMode.value;
		^state;
    }

    loadState { |state|
		if (state.notNil, {
			this.stepCount = state[\slidersValues].size;
			if (state[\mode].notNil, {
				popupMode.valueAction = state[\mode];
			});
			state[\slidersValues] do: { |value, i|
				sliders[i].valueAction = value;
			};
		});
    }
}
