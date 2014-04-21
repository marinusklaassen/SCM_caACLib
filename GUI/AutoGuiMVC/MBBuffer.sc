

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
	    <samplerMode, <midi, <invert, <sampler, labelName;

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

		label = StaticText.new(frame,Rect(4, 2, 44, height - 4));
		label.string_(labelName);

		bank = PopUpMenu(frame, Rect(50, 2, 70, height - 4));

		soundfiles = PopUpMenu(frame, Rect(130, 2, 70, height - 4));

		incBank = RoundButton(frame, Rect(210, 2, height - 4 , height - 4)).states_([[ "-" ]]);
		incBank.background = Color.yellow;

		decBank = RoundButton(frame, Rect(240, 2, height - 4 , height - 4) ).states_([[ "+" ]]);
		decBank.background = Color.yellow;

		led = LED(frame, Rect(268, 2, height - 4, height - 4));
		led.value = 0;

		sampler = Button(frame, Rect(295,2,40,height - 4))
		.font_(Font("Monaco", italic: true, size: 9))
		.states_([["SM OFF", Color.red, Color.black],
			["SM ON", Color.black, Color.red]]);

		midi = Button(frame, Rect(340,2,40,height - 4))
			.font_(Font("Monaco", size: 9))
		.states_([["CC OFF", Color.red, Color.black],
			["CC ON", Color.black, Color.red]]);

		invert = Button(frame, Rect(382,2,18,18))
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
	var <view;

	*new { |argName, argSpec|
		^super.newCopyArgs.init(argName);
	}

	init { |argName = "buffer"|
		name = argName;
		/* make a reference to the class BufferPool to retrieve the buffers and control specs */
		// bufferData = BufferPoolData();
		/* add dependant to BufferPoolData here function here */
		// bufferData.model.addDependant({});
		learnFlag = 0;
		invertFlag = 0;
	}

	gui { |argParent, argBounds|
		view = SBufControlView(name);
		view.gui(argParent, argBounds);
	}

	closeGui {
		view.closeGui;
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
		invertFlag = argValue;
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