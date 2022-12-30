/*
FILENAME: SynthBoxSliderView

DESCRIPTION: Slider, spec en midi learn in one class.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
SynthBoxSliderView(bounds:400@50).front();
*/

SynthBoxSliderView : View {
	var model, dependants, <>spec, <name, midiResp, midiFlag, <>bMidiInvert, <valueMapped, <canRandomize;
    var <mainLayout, <labelName, <sliderView, <numberBoxValue, <toggleMidiLearn, <toggleMidiInvert, buttonMuteRandomization;

	*new { |argName, argSpec, parent, bounds|
		^super.new(parent, bounds).initialize(argName,argSpec);
	}

	initialize { |argName, argSpec|
		if (argSpec.isNil) { argSpec = ControlSpec(); };
		if (argName.isNil) { argName = \default };
		spec = argSpec;
		name = argName;
		midiFlag = 0;
		bMidiInvert = false;
		canRandomize = true;
		model = (value: 0.0);
		model[\setValueFunction] = { |value|
			model[\value] = value;
			valueMapped = spec.map(value);
			model.changed(\value, value);

		};
		dependants = ();
		dependants[\actionFunc] = {|theChanger, what, val|
			if (action.notNil) { action.value(this) };
		};
		model.addDependant(dependants[\actionFunc]);
		this.initializeView();
		this.value = 0;
	}

	initializeView {

		mainLayout = HLayout();
		this.layout = mainLayout;

		sliderView = SliderFactory.createInstance(this, class: "slider-horizontal");
		sliderView.action = { |sender| model[\setValueFunction].value(sender.value) };
		mainLayout.add(sliderView, stretch: 1);

		numberBoxValue = NumberBoxFactory.createInstance(this, class: "numberbox-synthbox-mapped-value");
		numberBoxValue.action = { |sender| model[\setValueFunction].value(spec.unmap(sender.value)) };
		mainLayout.add(numberBoxValue);

		buttonMuteRandomization = ButtonFactory.createInstance(this, class: "btn-mute-randomizer");
		buttonMuteRandomization.action = { |sender|
			if (sender.value == 1) { canRandomize=false } { canRandomize=true; }
		};
		mainLayout.add(buttonMuteRandomization);

		toggleMidiLearn = ButtonFactory.createInstance(this, class: "btn-toggle-midiswitch", buttonString1: "ON", buttonString2: "OFF");
		toggleMidiLearn.action = { |sender|
			if (sender.value == 1) { this.midiLearn; } { this.midiUnlearn; };
		};
		mainLayout.add(toggleMidiLearn);

		toggleMidiInvert  = ButtonFactory.createInstance(this, class: "btn-toggle-midiinvert", buttonString1: "ø", buttonString2: "ø");
		toggleMidiInvert.action = { arg sender; bMidiInvert = sender.value == 1; };
		mainLayout.add(toggleMidiInvert, align: \top);

		dependants[\updateView] = {|theChanger, what, val|∂
			sliderView.value_(val);
			numberBoxValue.value_(spec.map(val));
		};
		model.addDependant(dependants[\updateView]);
	}

	midiStart { |iControlChannel|
		if (midiResp.notNil) { midiResp.remove; };
		midiResp = CCResponder({
			arg src, chan, num, value;
			if (bMidiInvert) { value = 127 - value; };
			{ model[\setValueFunction].value(value / 127); }.defer;
			}, chan: iControlChannel);
	}

	midiLearn {
		if (midiResp.notNil) { midiResp.remove; };
		midiResp = CCResponder({ |src,chan,num,value|
			if (bMidiInvert) { value = 127 - value; };
				{ model[\setValueFunction].value(value / 127); }.defer;
		});
		midiResp.learn; // wait for the first controller
		midiFlag = 1;
	}

	midiUnlearn {
		midiResp.remove; midiResp = nil; midiFlag = 0;
	}

	valueMapped_ {|argValue|
		var unmappedValue = spec.unmap(argValue);
		model[\setValueFunction].value(unmappedValue);
	}

	value_ {|argValue|
		model[\setValueFunction].value(argValue)
	}

	value {
		^model[\value]
	}

	name_ {|argName|
		name = argName;
	}

	randomize {
		if (this.canRandomize, { this.value_(1.0.rand) });
	}

	getState  {
		var state = Dictionary();
		state[\name] = this.name;
		state[\value] = this.value;
		^state;
	}

	loadState { |state|
		this.name = state[\name];
		this.value = state[\value];
	}
}
