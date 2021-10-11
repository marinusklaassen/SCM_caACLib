/*
FILENAME: SyntBoxView

DESCRIPTION: SynthBoxView is a dedicated reponsive Synth control UI based on SynthDef metadata to assign controls to synth control parameters.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:

s.boot; SynthBoxView(\PmGrain1, bounds: 700@400).front
*/

SynthBoxView : View {
	var <mainLayout, <layoutControls, <controlViews, <synthDefName, synthDesc, <>controlPanel, presetView, tempNames, tempToggleSynth;

	*new { |argSynthDefName, parent, bounds|
		^super.new(parent, bounds).initialize(argSynthDefName);
	}

	initialize { |argSynthDefName|
		var labelWidthHint = 0;
		synthDefName = argSynthDefName;
		synthDesc = SynthDesc.readDef(synthDefName);
		this.background_(Color.grey);
        this.deleteOnClose = false;
		mainLayout = VLayout();
		this.layout = mainLayout;
		this.name = synthDefName;
		presetView = PresetView(contextId: (\SynthBoxView ++ argSynthDefName));
		presetView.actionLoadPreset = { |state| this.loadState(state); };
		presetView.actionFetchPreset = { this.getState(); };

		mainLayout.add(presetView);

		controlPanel = SynthBoxControlPanelView(synthDefName);
		controlPanel.mainLayout.margins = 0!4;
		controlPanel.randomAction = {
			controlViews do: { |element|
				if (element.isKindOf(SynthBoxSliderView)) { element.value_(1.0.rand) }
			}
		};

		controlPanel.playTrigger = { this.oneShotPlay(); };
		controlPanel.playToggle = { |value| this.togglePlay(value) };

		mainLayout.add(controlPanel);

	    controlViews = Dictionary();
		tempNames = synthDesc.controlNames;

		synthDesc.metadata[\hideUI] do: { |key| tempNames.removeAt(tempNames.indexOfEqual(key)) };
		// Collects controls

		layoutControls = VLayout();
		layoutControls.spacing = 0;
		layoutControls.margins = 0!4;
		mainLayout.add(layoutControls);

		tempNames do: { | key |
			var newView, spec, specOverride;
			specOverride = synthDesc.metadata[\specs][key];
			// Check for an controlspec override. Else try a default.
			if (specOverride.notNil, {
				spec = specOverride.asSpec; // When the ControlSpec is defined as an array.
			}, {
				spec = key.asSpec;
			});
			if (spec.isKindOf(ControlSpec), {
				newView = SynthBoxSliderView(key, spec);
				newView.action = { |sender|   if(tempToggleSynth.notNil, { tempToggleSynth.set(sender.name.asSymbol, sender.mappedValue); }); };
			}, {
				newView = SynthBoxNumberView(key);
				 newView.action = { |sender|  if(tempToggleSynth.notNil, { tempToggleSynth.set(sender.name.asSymbol, sender.value); }); };

			});
			newView.mainLayout.margins = 0!4;
			if (newView.labelName.sizeHint.width > labelWidthHint, {
				labelWidthHint = newView.labelName.sizeHint.width;
			});
			controlViews[key] = newView;
			layoutControls.add(newView);
			layoutControls.add(CompositeView().minHeight_(2).background_(Color.rand));
		};
		controlViews do: { |controlView| controlView.labelName.minWidth = labelWidthHint; };
	    mainLayout.add(nil, stretch:1, align: \bottom);
	}

	getParamValuesArray {
		var paramValuesArray = List(), paramValue;
		controlViews do: { |controlView|
			if(controlView.isKindOf(SynthBoxSliderView)) {
				paramValue = controlView.mappedValue;
				} {
				paramValue = controlView.value;
			};
    		paramValuesArray.add(controlView.name);
			paramValuesArray.add(paramValue);
			};
		^paramValuesArray.asArray;
	}

	oneShotPlay {
		fork {
			var tempSynth, indexOf, paramsValues;
			if (tempSynth.notNil) { tempSynth.release; tempSynth = nil };
			paramsValues = this.getParamValuesArray();
			tempSynth = Synth(synthDefName, paramsValues);
			Server.default.sync;
			indexOf = paramsValues.indexOf(\atk);
			if (indexOf.notNil) {
				fork { (paramsValues[indexOf + 1]).wait; tempSynth.set(\gate, 0); tempSynth = nil }
			} {
				tempSynth.set(\gate, 0); tempSynth = nil
			};
		}
	}

	togglePlay { |toggle|
		var paramsValues = this.getParamValuesArray;

		if (toggle > 0) {

			if (tempToggleSynth.notNil) { tempToggleSynth.release; tempToggleSynth = nil };
			tempToggleSynth = Synth(synthDefName, paramsValues);



		} {
			tempToggleSynth.set(\gate, 0); tempToggleSynth = nil;
		};
	}

	getState  {
		var state = Dictionary();
		state[\controllerStates] = Dictionary();
		controlViews collect: { |element|
			state[\controllerStates][element.name] = element.getState();
		};
		^state;
	}

	loadState { |state|
		state[\controllerStates].keys do: { |name|
			controlViews[name].loadState(state[\controllerStates][name]);
		};
	}
}