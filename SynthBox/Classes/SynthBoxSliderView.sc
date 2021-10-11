/*
FILENAME: SynthBoxSliderView

DESCRIPTION: Slider, spec en midi learn in one class.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
SynthBoxSliderView(bounds:400@50).front();
*/

SynthBoxSliderView : View {
	var model, dependants, <>spec, <name, midiResp, midiFlag, <>bMidiInvert;
    var <mainLayout, <labelName, <sliderView, <numberBoxValue, <toggleMidiLearn, <toggleMidiInvert, <mappedValue;

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
		model = (value: 0.0);
		model[\setValueFunction] = { |value|
			model[\value] = value;
			mappedValue = spec.map(value);
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

		labelName = StaticText();
		labelName.string = name;
		mainLayout.add(labelName);

		sliderView = Slider();
		sliderView.orientation = \horizontal;
		sliderView.action = { |sender| model[\setValueFunction].value(sender.value) };
		mainLayout.add(sliderView, stretch: 1);

		numberBoxValue = NumberBox();
		numberBoxValue.minDecimals = 1;
		numberBoxValue.maxWidth = 80;
		numberBoxValue.action = { |sender| model[\setValueFunction].value(spec.unmap(sender.value)) };
	    mainLayout.add(numberBoxValue);

		toggleMidiLearn = Button();
		toggleMidiLearn.maxWidth = 30;
		toggleMidiLearn.states = [["OFF", Color.red, Color.black], ["ON", Color.black, Color.red]];
		toggleMidiLearn.action = { |sender|
			if (sender.value == 1) { this.midiLearn; } { this.midiUnlearn; };
		};
		mainLayout.add(toggleMidiLearn);

		toggleMidiInvert  = Button();
		toggleMidiInvert.fixedSize = 16;
		toggleMidiInvert.states = [["ø", Color.red, Color.black], ["ø", Color.black, Color.red]];
		toggleMidiInvert.action = { arg sender; bMidiInvert = sender.value == 1; };

		mainLayout.add(toggleMidiInvert, align: \top);

		dependants[\updateView] = {|theChanger, what, val|
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

	value_ {|argValue|
		model[\setValueFunction].value(argValue)
	}

	value {
		^model[\value]
	}

	name_ {|argName|
		name = argName;
		labelName.string_(name);
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
