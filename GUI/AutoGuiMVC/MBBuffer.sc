
BufferGCM {  /* static class BufferGui Control Model */
	classvar <>model;

	*init {
		model = Event();
	}

	*changed { | ...args |
		if (model.notNil) { model.changed(\bufferPool, args); };
	}
}


MBBuffer {
	var <>spec, <name, <>action, midiResp, learnFlag, invertFlag;
	var bufferData;
	var view, midiInvertView, midiToggleView, incBankView, decBankView, bankView, samplerModeView, ledView, labelView, dropView;

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

	makeGui { |parent, argBounds|
		var bounds = argBounds.asRect,
		width = bounds.width,
		height = bounds.height;

		view = CompositeView(parent, bounds);
		view.background = Color.grey;

		labelView = StaticText.new(view,Rect(4, 0, 54, height));
		labelView.string_(name);

		bankView = PopUpMenu(view, Rect(60, 0, 70, height));
		bankView.items = ["a", "b", "c"];

		incBankView = RoundButton(view, Rect(130, 0, height , height)).states_([[ "-".postln ]]);
		incBankView.background = Color.yellow;

		decBankView = RoundButton(view, Rect(160, 0, height , height) ).states_([[ "+".postln ]]);
		decBankView.background = Color.yellow;

		ledView = LED(view, Rect(190, 0, height, height));
		ledView.value = 1;

		dropView = DragSink(view, Rect(220, 0, 80, height));

		samplerModeView = Button(view, Rect(220,0,height,height))
		.states_([["SM OFF", Color.red, Color.black],
			["SM ON", Color.black, Color.red]])
		.action_({ arg button;
		 "sampler mode toggle".postln;
		})
		.value_(0);

		midiToggleView = Button(view, Rect(250,0,height,height))
		.states_([["OFF", Color.red, Color.black],
			["ON", Color.black, Color.red]])
		.action_({ arg button;
			if (button.value == 1) { this.midiLearn; } { this.midiUnlearn; };
		})
		.value_(learnFlag);

		midiInvertView = Button(view, Rect(280,height - 18,18,18))
		.states_([["ø", Color.red, Color.black],
			["ø", Color.black, Color.red]])
		.action_({ arg button;
			this.invert(button.value);
		})
		.value_(invertFlag);



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