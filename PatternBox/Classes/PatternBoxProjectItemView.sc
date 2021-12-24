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

	var mainLayout, <>actionMoveUp, <>actionMoveDown, patternBoxView, togglePlay, sliderVolume, buttonShowPatternBox, buttonRemove, buttonMoveUp, buttonMoveDown;
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
		mainLayout.margins_([2,2,20,2]);
		mainLayout.vSpacing_(0);
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

		buttonMoveUp = Button()
			.fixedWidth_(20)
			.fixedHeight_(24)
			.states_([["↑", Color.black, Color.white.alpha_(0.5)]])
			.action_({  if (actionMoveUp.notNil, { actionMoveUp.value(this) }); });


		mainLayout.add(buttonMoveUp, 0, 0);

		buttonMoveDown = Button()
			.fixedWidth_(20)
			.fixedHeight_(24)
			.states_([["↓", Color.black, Color.white.alpha_(0.5)]])
			.action_({  if (actionMoveDown.notNil, { actionMoveDown.value(this) }); });

		mainLayout.add(buttonMoveDown, 1, 0);

		togglePlay = ButtonFactory.createInstance(this, class: "toggle-play-patternboxprojectitemview");
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
        layout.addSpanning(buttonShowPatternBox, 0, 3, rowSpan: 2);

		buttonRemove = ButtonFactory.createInstance(this, class: "btn-delete");
		buttonRemove.action = { |sender| this.onButtonRemove(sender); };
		layout.add(buttonRemove, 0, 4, align: \top);

		// Start drag & drop workaround
		prBeginDragAction =  { |view, x, y|
			this; // Current instance is the object to drag.
		};

		prCanReceiveDragHandler = {  |view, x, y|
			View.currentDrag.isKindOf(PatternBoxProjectItemView);
		};

		prReceiveDragHandler = { |view, x, y|
			if (actionMovePatternBox.notNil, { actionMovePatternBox.value(this, View.currentDrag); });
		};

		this.dragLabel = patternBoxView.patternBoxName;
		this.beginDragAction = prBeginDragAction;
		this.canReceiveDragHandler = prCanReceiveDragHandler;
		this.receiveDragHandler = prReceiveDragHandler;

		buttonMoveUp.dragLabel = patternBoxView.patternBoxName;
		buttonMoveUp.beginDragAction = prBeginDragAction;
		buttonMoveUp.canReceiveDragHandler = prCanReceiveDragHandler;
		buttonMoveUp.receiveDragHandler = prReceiveDragHandler;

	    buttonMoveDown.dragLabel = patternBoxView.patternBoxName;
		buttonMoveDown.beginDragAction = prBeginDragAction;
		buttonMoveDown.canReceiveDragHandler = prCanReceiveDragHandler;
		buttonMoveDown.receiveDragHandler = prReceiveDragHandler;

		togglePlay.dragLabel = patternBoxView.patternBoxName;
		togglePlay.beginDragAction = prBeginDragAction;
		togglePlay.canReceiveDragHandler = prCanReceiveDragHandler;
		togglePlay.receiveDragHandler = prReceiveDragHandler;

		buttonShowPatternBox.dragLabel = patternBoxView.patternBoxName;
		buttonShowPatternBox.beginDragAction = prBeginDragAction;
		buttonShowPatternBox.canReceiveDragHandler = prCanReceiveDragHandler;
		buttonShowPatternBox.receiveDragHandler = prReceiveDragHandler;

		sliderVolume.dragLabel = patternBoxView.patternBoxName;
		sliderVolume.beginDragAction = prBeginDragAction;
		sliderVolume.canReceiveDragHandler = prCanReceiveDragHandler;
		sliderVolume.receiveDragHandler = prReceiveDragHandler;
	}

	onPatternBoxNameChanged { |sender|
		sliderVolume.labelText = sender.patternBoxName;
		patternBoxName = sender.patternBoxName;
		this.dragLabel = sender.patternBoxName;
		buttonShowPatternBox.dragLabel = sender.patternBoxName;
		buttonMoveUp.dragLabel = sender.patternBoxName;
		buttonMoveDown.dragLabel = sender.patternBoxName;
		togglePlay.dragLabel = sender.patternBoxName;
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
       patternBoxName = state[\patternBoxName].postln;
	   sliderVolume.labelText = patternBoxName.postln;
	   volume = state[\volume];
	   sliderVolume.value = volume;
    }

    dispose {
        this.remove(); // removes itselfs from the layout
        CmdPeriod.remove(onCommandPeriodFunc);
		if (actionRemove.notNil, { actionRemove.value(); });
    }
}
