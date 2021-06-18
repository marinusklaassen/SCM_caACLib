MBFader {
	var gui, model, depedants, <>spec, <name, <>action, midiResp, midiFlag, <>bMidiInvert;

	*new { |argName, argSpec|
		^super.newCopyArgs.init(argName,argSpec);
	}


	init { |argName, argSpec|
		if (argSpec.isNil) { argSpec = \amp.asSpec };
		if (argName.isNil) { argName = \default };
		spec = argSpec;
		name = argName;
		midiFlag = 0;
		bMidiInvert = false;
		model = (value: 0.0);
		model[\setValueFunction] = { |value|
			model[\value] = value;
			model.changed(\value, value);
		};
		depedants = ();
		depedants[\actionFunc] = {|theChanger, what, val|
			if (action.notNil) { action.value(spec.map(val)) };
		};
		model.addDependant(depedants[\actionFunc]);
	}


	makeGui { |parent, bounds|
		var sliderWidth = bounds.asRect.width - 180;
		bounds = bounds.asRect;
		gui = Dictionary.new;
		gui[\canvas] = CompositeView(parent,bounds);
		gui[\nameView] = StaticText.new(gui[\canvas], Rect(4,0,54,bounds.height));
		gui[\nameView].string_(name);
		gui[\sliderView] = Slider(gui[\canvas], Rect(60,0,sliderWidth-2,bounds.height));
		gui[\sliderView].value_(model[\value]);
		gui[\sliderView].action_({ |sl| model[\setValueFunction].value(sl.value) });
		gui[\boxView] = NumberBox(gui[\canvas], Rect(60+sliderWidth,0,68,bounds.height));
		gui[\boxView].value_(model[\value]);
		gui[\boxView].minDecimals_(1);
		gui[\boxView].action_({ |sl| model[\setValueFunction].value(spec.unmap(sl.value)) });
		gui[\midiView] = Button(gui[\canvas], Rect(130+sliderWidth,0,28,bounds.height))
		.states_([["OFF", Color.red, Color.black],
			["ON", Color.black, Color.red]])
		.action_({ arg butt;
			if (butt.value == 1) { this.midiLearn; } { this.midiUnlearn; };
		})
		.value_(midiFlag);

		gui[\btnMidiInvert] = Button(gui[\canvas], Rect(160+sliderWidth,0,16,16))
		.states_([["ø", Color.red, Color.black],
			["ø", Color.black, Color.red]])
		.action_({ arg butt;

			bMidiInvert = butt.value == 1;
		})
		.value_(\bMidiInvert);

		depedants[\updateView] = {|theChanger, what, val|
			gui[\sliderView].value_(val);
			gui[\boxView].value_(spec.map(val));
		};
		model.addDependant(depedants[\updateView]);
		model[\setValueFunction].value(model[\value]);
	}


	midiStart {

		arg iControlChannel;

		if (midiResp.notNil) { midiResp.remove; };

		midiResp = CCResponder({

			arg src, chan, num, value;

			if (bMidiInvert) { value = 127 - value; };

			{ model[\setValueFunction].value(value / 127); }.defer;

			}
			,chan: iControlChannel
		);

	} /* midiStart */


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
		if (gui.notNil) { gui[\nameView].string_(name); };
	}


	closeGui {
	model.removeDependant(depedants[\updateView]);
        }
}
