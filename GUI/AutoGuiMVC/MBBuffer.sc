

BufferGCM {  /* static class BufferGui Control Model */
	classvar <>model;

	*init {
		model = Event();
	}

	*changed { | ...args |
		var what = args.removeAt(0);
		if (model.notNil) { model.changed(what, args); };
	}
}


SBufControlView {
	var <frame, <label, <bank, <soundfiles, <incBank, <decBank, <led,
	    <samplerMode, <midi, <invert, <sampler, <labelName;

	*new { |argLabelName|
		^super.newCopyArgs.init(argLabelName);
	}

	init { |argLabelName|
		labelName = argLabelName;
	}

	gui { |argParent, argBounds|
		var bounds     = argBounds.asRect,
	            width      = bounds.width,
		    height     = bounds.height;

		frame = CompositeView(argParent, bounds);
		frame.background = Color.grey;

		label = StaticText.new(frame,Rect(4, 0, 54, height));
		label.string_(labelName);

		bank = PopUpMenu(frame, Rect(60, 0, 70, height));
		bank.items = ["b1", "b2", "b3"];

		soundfiles = PopUpMenu(frame, Rect(60, 0, 70, height));
		soundfiles.items = ["s1", "s2", "s3"];

		incBank = RoundButton(frame, Rect(130, 0, height , height)).states_([[ "-".postln ]]);
		incBank.background = Color.yellow;

		decBank = RoundButton(frame, Rect(160, 0, height , height) ).states_([[ "+".postln ]]);
		decBank.background = Color.yellow;

		led = LED(frame, Rect(190, 0, height, height));
		led.value = 1;

		sampler = Button(frame, Rect(220,0,height,height))
		.states_([["SM OFF", Color.red, Color.black],
			["SM ON", Color.black, Color.red]])
		.action_({ arg button;
		 "sampler mode toggle".postln;
		})
		.value_(0);

		midi = Button(frame, Rect(250,0,height,height))
		.states_([["OFF", Color.red, Color.black],
			["ON", Color.black, Color.red]]);

		invert = Button(frame, Rect(280,height - 18,18,18))
		.states_([["ø", Color.red, Color.black],
			["ø", Color.black, Color.red]]);
	}

	closeGui {
		frame.remove; label.remove; bank.remove; soundfiles.remove;
		incBank.remove; decBank.remove; led.remove; samplerMode.remove;
		midi.remove; invert.remove; sampler.remove;

	}
}


SBufControl {
	var <>spec, <name, <>action, midiResp, learnFlag, invertFlag;
	var bufferData;
	var view;

	*new { |argName, argSpec|
		^super.newCopyArgs.init(argName);
	}

	init { |argName|
		name = argName;
		/* make a reference to the class BufferPool to retrieve the buffers and control specs */
		// bufferData = BufferPoolData();
		/* add dependant to BufferPoolData here function here */
		// bufferData.model.addDependant({});
		learnFlag = 0;
		invertFlag = 0;
	}

	gui { |argParent, argBounds|
		view = SBufControlView("buffer");
		view.gui(argParent, argBounds);
	}

	midiLearn {
	/*	if (midiResp.isNil) {
			midiResp = CCResponder({ |src,chan,num,value|
				// { model[\setValueFunction].value(value / 127); }.defer;
			});
		};
		midiResp.learn; // wait for the first controller
		learnFlag = 1;*/
	}

	midiUnlearn {
	/*	midiResp.remove; midiResp = nil;
		learnFlag = 0;*/
	}

	invert { |argValue|
		invertFlag = argValue.postln;
	}

	/*
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

	closeGui { model.removeDependant(depedants[\updateView]); }*/
}