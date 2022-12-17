/*
FILENAME: SCMIDIInNoteRangeView

DESCRIPTION: Select a MIDIIn source

AUTHOR: Marinus Klaassen (2012, 2021Q4)

EXAMPLE:
m = SCMIDIInNoteRangeView(noteOnAction: { |...args| args.postln; }, noteOffAction: {|...args| args.postln; }).front;
a = m.getState();
m.loadState(a);
m.dispose
*/

SCMIDIInNoteRangeView : SCMViewBase {
	var <proxy, midiInSelectorView, mainLayout, numberBoxMIDIChannel, numberBoxClipLo, numberBoxClipHi, <midiIn, <clipLo=0, <clipHi=127, <midiNoteFuncOn, chan=nil, <midiNoteFuncOff, <>name, <>noteOnAction, <>noteOffAction, noteOnActionIntermediate, noteOffActionIntermediate;

	*new { |parent, bounds, noteOnAction, noteOffAction|
		^super.new(parent, bounds).initialize(noteOnAction, noteOffAction).initializeView();
	}

	initialize { |noteOnAction, noteOffAction|
		this.needsControlSpec = false;
		proxy = PatternProxy();
		proxy.source = 0;
		this.noteOnAction = noteOnAction;
		this.noteOffAction = noteOffAction;
		noteOnActionIntermediate = {  |val, num, chan, src|
			if ((num >= clipLo) && (num <= clipHi), {
				this.noteOnAction.value(val, num, chan, src);
			});
		};
		noteOffActionIntermediate = {  |val, num, chan, src|
			if ((num >= clipLo) && (num <= clipHi), {
				this.noteOffAction.value(val, num, chan, src);
			});
		};
	}

	getProxies {
		var result = Dictionary();
		result[this.name.asSymbol] = proxy;
		^result;
	}

	initializeView {
		mainLayout = HLayout();
		mainLayout.margins = 0!4;
		this.layout = mainLayout;

		midiInSelectorView = SCMMIDIInSelectorView();
		midiInSelectorView.action = { |sender|
			midiIn = sender.midiIn;
			this.update();
		};

		mainLayout.add(midiInSelectorView);

		numberBoxMIDIChannel = NumberBox();
		numberBoxMIDIChannel.value = -1;
		numberBoxMIDIChannel.background = Color.black.alpha_(0);
		numberBoxMIDIChannel.clipLo = -1;
		numberBoxMIDIChannel.clipHi =  127;
		numberBoxMIDIChannel.decimals = 0;
		numberBoxMIDIChannel.minWidth = 50;
		numberBoxMIDIChannel.step = 1;
		numberBoxMIDIChannel.toolTip = "Select a MIDI channel, -1 is all channels";

		numberBoxMIDIChannel.action = { |sender|
			chan = if (sender.value > 0, { sender.value.asInteger(); }, {});
			this.update();
		};
		mainLayout.add(numberBoxMIDIChannel);

	    numberBoxClipLo = NumberBox();
		numberBoxClipLo.background = Color.black.alpha_(0);
	    numberBoxClipLo.value = clipLo;
		numberBoxClipLo.clipLo = -1;
		numberBoxClipLo.clipHi =  127;
		numberBoxClipLo.decimals = 0;
		numberBoxClipLo.minWidth = 50;
		numberBoxClipLo.toolTip = "Clip lo MIDI note input";

		numberBoxClipLo.step = 1;
		numberBoxClipLo.action = { |sender| clipLo = sender.value.asInteger(); };

	    mainLayout.add(numberBoxClipLo);

	    numberBoxClipHi = NumberBox();
		numberBoxClipHi.background = Color.black.alpha_(0);
		numberBoxClipHi.value = clipHi;
		numberBoxClipHi.clipLo = -1;
		numberBoxClipHi.clipHi =  127;
		numberBoxClipHi.decimals = 0;
		numberBoxClipHi.minWidth = 50;
		numberBoxClipLo.toolTip = "Clip hi MIDI note input";
		numberBoxClipHi.step = 1;
		numberBoxClipHi.action = { |sender| clipHi = sender.value.asInteger(); };

		mainLayout.add(numberBoxClipHi);
	}

	update {
		if (midiNoteFuncOn.notNil, { midiNoteFuncOn.free; });
		if (midiNoteFuncOff.notNil, { midiNoteFuncOff.free; });
		midiNoteFuncOn = MIDIFunc.noteOn(noteOnActionIntermediate, srcID: midiIn.uid, chan: chan);
		midiNoteFuncOff = MIDIFunc.noteOff(noteOffActionIntermediate, srcID: midiIn.uid, chan: chan);
	}

	dispose {
		midiInSelectorView.dispose();
		if (midiNoteFuncOn.notNil, { midiNoteFuncOn.free; });
		if (midiNoteFuncOff.notNil, { midiNoteFuncOff.free; });
	}

	getState {
		var state = Dictionary();
		state[\midiInSelectorView] = midiInSelectorView.getState();
		state[\numberBoxMIDIChannel] = numberBoxMIDIChannel.value;
		state[\numberBoxClipLo] = numberBoxClipLo.value;
		state[\numberBoxClipHi] = numberBoxClipHi.value;
		^state;
	}

	loadState { |state|
		midiInSelectorView.loadState(state[\midiInSelectorView]);
		numberBoxMIDIChannel.valueAction = state[\numberBoxMIDIChannel];
		numberBoxClipLo.valueAction = state[\numberBoxClipLo];
		numberBoxClipHi.valueAction = state[\numberBoxClipHi];
	}
}
