/*
FILENAME: PatternBoxProjectItemView

DESCRIPTION: THe PatternBoxProjectItemView the project item view which references to the projectview.

AUTHOR: Marinus Klaassen (2012, 2021Q4)

EXAMPLE:
s.boot;
PatternBoxProjectItemView().front;

s.boot;
m = PatternBoxProjectItemView().front();
a = m.getState()
m.loadState(a);
m.model[\envirText]
a.keys do: { |key| a[key.postln].postln }
*/


PatternBoxProjectItemView : View {

	var mainLayout, patternBoxView, togglePlay, dragBothPanel, sliderVolume, buttonShowPatternBox, buttonRemove, buttonMoveUp, buttonMoveDown;
	var onCommandPeriodFunc, <>actionRemove, <>actionInsertPatternBox, <>actionMovePatternBox;
	var volume = 1, <patternBoxName, playState = 0, lemurClient;
	var prCanReceiveDragHandler, prReceiveDragHandler, prBeginDragAction;

	*new { |parent, bounds, lemurClient|
		^super.new(parent, bounds).initialize(lemurClient);
	}

	initialize { |lemurClient|
		lemurClient = lemurClient;
		patternBoxView = PatternBoxView(lemurClient: patternBoxView, bounds: Rect(100, 100, 600, 800));
		patternBoxView.actionNameChanged = { |sender| this.onPatternBoxNameChanged(sender); };
		patternBoxView.actionPlayStateChanged = { |sender| this.onPatternBoxPlayStateChanged(sender); };
		patternBoxName = patternBoxView.patternBoxName;
		onCommandPeriodFunc = { this.onCommandPeriod();};
		CmdPeriod.add(onCommandPeriodFunc);
		this.initializeView();
	}

	initializeView {
		mainLayout = GridLayout();
		mainLayout.margins_([5,2,20,2]);
		mainLayout.vSpacing_(0);
		this.toolTip = "Press CMD + drag to move this item to another position.";
		this.layout = mainLayout;
		this.background = Color.black.alpha_(0.2);

		this.setContextMenuActions(
			MenuAction("Insert row before", {
				if (actionInsertPatternBox.notNil, { actionInsertPatternBox.value(this, "INSERT_BEFORE"); });
			}),
			MenuAction("Insert row after", {
				if (actionInsertPatternBox.notNil, { actionInsertPatternBox.value(this, "INSERT_AFTER"); });
			})
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

		sliderVolume = SliderViewFactory.createInstance(this)
		.spec_(\db.asSpec)
		.value_(volume)
		.labelText_(patternBoxView.patternBoxName)
		.action_({ |sender | this.onSliderVolumeChanged(sender.value); });
		mainLayout.addSpanning(sliderVolume, 0, 2, rowSpan: 2);
		mainLayout.setColumnStretch(1, 1);

		buttonShowPatternBox = ButtonFactory.createInstance(this, class: "btn-patternboxprojectitemview-showpatternbox");
		buttonShowPatternBox.action = { |sender| this.onButtonShowPatternBox(sender); };
		buttonShowPatternBox.toolTip = "Bring the PatternBox editor to the front.";
		layout.addSpanning(buttonShowPatternBox, 0, 3, rowSpan: 2);

		buttonRemove = ButtonFactory.createInstance(this, class: "btn-delete");
		buttonRemove.action = { |sender| this.onButtonRemove(sender); };
		layout.add(buttonRemove, 0, 4, align: \top);

		// Start drag & drop beahvior workaround
		prBeginDragAction =  { |view, x, y|
			this; // Current instance is the object to drag.
		};

		prCanReceiveDragHandler = {  |view, x, y|
			View.currentDrag.isKindOf(PatternBoxProjectItemView);
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
		sliderVolume.labelText = sender.patternBoxName;
		patternBoxName = sender.patternBoxName;
		this.setDragAndDropBehavior(this);
		this.setDragAndDropBehavior(dragBothPanel);
		this.setDragAndDropBehavior(togglePlay);
		this.setDragAndDropBehavior(buttonShowPatternBox);
		this.setDragAndDropBehavior(sliderVolume);
	}

	onPatternBoxPlayStateChanged { |sender|
		togglePlay.value = sender.playState;
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

	onSliderVolumeChanged { |sender|
		patternBoxView.volume = sender.value;
		volume = sender.value;
	}

	onButtonRemove { |sender|
		this.dispose();
	}

	getState {
		var state = Dictionary();
		state[\type] = "PatternBoxProjectItemView";
		state[\patternBoxState] = patternBoxView.getState();
		state[\patternBoxName] = patternBoxName;
		state[\volume] = volume;
		^state;
	}

	loadState { |state|
		state[\type] = "PatternBoxProjectItemView";
		patternBoxView.loadState(state[\patternBoxState]);
		patternBoxName = state[\patternBoxName];
		sliderVolume.labelText = patternBoxName;
		volume = state[\volume];
		sliderVolume.value = volume;
	}

	dispose {
		this.remove(); // removes itselfs from the layout
		CmdPeriod.remove(onCommandPeriodFunc);
		if (actionRemove.notNil, { actionRemove.value(); });
	}
}
