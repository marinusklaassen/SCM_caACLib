/*
FILENAME: PatternBoxLauncherItemView

DESCRIPTION: THe PatternBoxLauncherItemView the project item view which references to the projectview.

AUTHOR: Marinus Klaassen (2012, 2021Q4)

EXAMPLE:
s.boot;
PatternBoxLauncherItemView().front;

s.boot;
m = PatternBoxLauncherItemView().front();
a = m.getState()
m.loadState(a);
m.model[\envirText]
a.keys do: { |key| a[key.postln].postln }
*/

PatternBoxLauncherItemView : View {

	var <>bufferpool, mainLayout, <patternBoxView, togglePlay, dragBothPanel, sliderVolume, buttonShowPatternBox, buttonRemove, buttonMoveUp, buttonMoveDown;
	var onCommandPeriodFunc, <>actionRemove, <>actionInsertPatternBox, <>actionMovePatternBox, <>context;
	var volume = 1, <patternBoxName, playState = 0, lemurClient;
	var prCanReceiveDragHandler, prReceiveDragHandler, prBeginDragAction, midiViewPlayButton;

	*new { |parent, bounds, bufferpool, context|
		^super.new(parent, bounds).initialize(bufferpool, context);
	}

	editMIDI { |editMode|
		midiViewPlayButton.visible = editMode;
	}

	initialize { |bufferpool, context|
		this.bufferpool = bufferpool;
		this.context = context;
		patternBoxView = PatternBoxView(bufferpool: bufferpool, context: context, bounds: Rect(100, 100, 700, 800));
		patternBoxView.actionNameChanged = { |sender| this.onPatternBoxNameChanged(sender); };
		patternBoxView.actionPlayStateChanged = { |sender| this.onPatternBoxPlayStateChanged(sender); };
		patternBoxName = patternBoxView.patternBoxName;
		onCommandPeriodFunc = { this.onCommandPeriod();};
		CmdPeriod.add(onCommandPeriodFunc);
		this.initializeView();
	}

	initializeView {
		mainLayout = GridLayout();
		mainLayout.margins_([5,5,20,5]);
		mainLayout.vSpacing_(5);
		this.toolTip = "Press CMD + drag to move this item to another position.";
		this.layout = mainLayout;
		this.background = Color(0.45490196078431, 0.55686274509804, 0.87843137254902);

		this.setContextMenuActions(
			MenuAction.separator.string_("Item"),
			MenuAction("Insert new item above", {
				if (actionInsertPatternBox.notNil, { actionInsertPatternBox.value(this, "INSERT_BEFORE"); });
			}),
			MenuAction("Insert new item below", {
				if (actionInsertPatternBox.notNil, { actionInsertPatternBox.value(this, "INSERT_AFTER"); });
			}),
			MenuAction("Duplicate this item", {
				if (actionInsertPatternBox.notNil, { actionInsertPatternBox.value(this, "INSERT_AFTER_DUPLICATIE"); });
			}),
			MenuAction("Remove this item", {
				this.dispose();
			}),
			MenuAction.separator.string_("MIDI - Launcher Item"),
			MenuAction("Show MIDI editing")
			.action_({  midiViewPlayButton.visible = true; }),
			MenuAction("Hide MIDI editing")
			.action_({  midiViewPlayButton.visible = false ; }),
		);

		dragBothPanel = DragBoth();
		dragBothPanel.minHeight = 50;
		dragBothPanel.maxWidth = 12.5;
		dragBothPanel.background = Color.black.alpha_(0.5);
		dragBothPanel.toolTip = this.toolTip;
		mainLayout.addSpanning(dragBothPanel, 0, 0, rowSpan: 2);

		togglePlay = ButtonFactory.createInstance(this, class: "toggle-play-patternboxprojectitemview");
		togglePlay.toolTip = "Play/stop this PatternBox.";
		togglePlay.action = {|sender| this.onTogglePlay(sender); };
		mainLayout.addSpanning(togglePlay, 0, 1, rowSpan: 2);

		sliderVolume = SCMSliderView()
		.spec_(\db.asSpec)
		.value_(volume)
		.labelText_(patternBoxView.patternBoxName)
		.action_({ |sender |
			this.onSliderVolumeChanged(sender); })
		.actionTextChanged_({ |sender|
			patternBoxView.setName(sender.string);
			patternBoxName = sender.string;
		});

		sliderVolume.numberBoxView.fixedWidth_(45);

		mainLayout.addSpanning(sliderVolume, 0, 2, rowSpan: 2);
		mainLayout.setColumnStretch(1, 1);

		buttonShowPatternBox = ButtonFactory.createInstance(this, class: "btn-patternboxprojectitemview-showpatternbox");
		buttonShowPatternBox.action = { |sender| this.onButtonShowPatternBox(sender); };
		buttonShowPatternBox.toolTip = "Bring the PatternBox editor to the front.";
		layout.addSpanning(buttonShowPatternBox, 0, 3, rowSpan: 2);

		buttonRemove = ButtonFactory.createInstance(this, class: "btn-delete");
		buttonRemove.action = { |sender| this.onButtonRemove(sender); };
		layout.add(buttonRemove, 0, 4, align: \top);

		midiViewPlayButton = SCMMIDIFuncNoteView();
		midiViewPlayButton.visible = false;
		midiViewPlayButton.actionNoteOff = {
			patternBoxView.stop();
		};
		midiViewPlayButton.actionNoteOn = {
			patternBoxView.play();
		};
		mainLayout.addSpanning(midiViewPlayButton, 2, 1, columnSpan: 3);

		// Start drag & drop beahvior workaround
		prBeginDragAction =  { |view, x, y|
			this; // Current instance is the object to drag.
		};

		prCanReceiveDragHandler = {  |view, x, y|
			View.currentDrag.isKindOf(PatternBoxLauncherItemView);
		};

		prReceiveDragHandler = { |view, x, y|
			if (actionMovePatternBox.notNil, { actionMovePatternBox.value(this, View.currentDrag); });
		};

		this.setDragAndDropBehavior(this);
		this.setDragAndDropBehavior(dragBothPanel);
		this.setDragAndDropBehavior(togglePlay);
		this.setDragAndDropBehavior(buttonShowPatternBox);
		this.setDragAndDropBehavior(sliderVolume);
		// End drag & drop behavior workaround
	}

	setDragAndDropBehavior { |object|
		object.dragLabel = patternBoxView.patternBoxName;
		object.beginDragAction = prBeginDragAction;
		object.canReceiveDragHandler = prCanReceiveDragHandler;
		object.receiveDragHandler = prReceiveDragHandler;
	}

	onPatternBoxNameChanged { |sender|
		this.setName(sender.patternBoxName);
	}

	setName { |name|
		sliderVolume.labelText = name;
		patternBoxName = name;
		this.setDragAndDropBehavior(this);
		this.setDragAndDropBehavior(dragBothPanel);
		this.setDragAndDropBehavior(togglePlay);
		this.setDragAndDropBehavior(buttonShowPatternBox);
		this.setDragAndDropBehavior(sliderVolume);
	}

	onPatternBoxPlayStateChanged { |sender|
		defer({togglePlay.value = sender.playState;});
	}

	onCommandPeriod {
		playState = 0;
		togglePlay.value = 0;
	}

	onButtonShowPatternBox { |sender|
		patternBoxView.front();
	}

	onTogglePlay { |sender|
		if (sender.value == 1, { patternBoxView.play(); }, { patternBoxView.stop(); });
	}

	stop {
	    playState = 0;
		togglePlay.value = 0;
		patternBoxView.stop();
	}

	onSliderVolumeChanged { |sender|
		patternBoxView.volume = dbamp(sender.valueMapped);
		volume = sender.value;
	}

	onButtonRemove { |sender|
		this.dispose();
	}

	closeView {
		patternBoxView.close();
	}

	getState {
		var state = Dictionary();
		state[\type] = "PatternBoxLauncherItemView";
		state[\patternBoxState] = patternBoxView.getState();
		state[\patternBoxName] = patternBoxName;
		state[\volume] = volume;
		state[\midiPlayView] = midiViewPlayButton.getState();
		^state;
	}

	loadState { |state|
		patternBoxView.loadState(state[\patternBoxState]);
		this.setName(state[\patternBoxName]);
		patternBoxView.setName(state[\patternBoxName]);
		sliderVolume.value = state[\volume];
		if (state[\midiPlayView].notNil, {
			midiViewPlayButton.loadState(state[\midiPlayView]);
		});
	}

	dispose {
		this.closeView();
		this.remove(); // removes itselfs from the layout
		CmdPeriod.remove(onCommandPeriodFunc);
		if (actionRemove.notNil, { actionRemove.value(); });
		midiViewPlayButton.dispose();
		patternBoxView.stop();
	}
}
