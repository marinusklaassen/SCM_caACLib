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

	var mainLayout, patternBoxView, togglePlay, sliderVolume, buttonShowPatternBox, buttonRemove;
	var onCommandPeriodFunc, <>actionRemove;
	var volume = 1, patternBoxName, playState = 0, lemurClient;

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
		mainLayout.margins_([0,0,20,0]);
		this.layout = mainLayout;
		this.background = Color.black.alpha_(0.2);

		togglePlay = ButtonFactory.createInstance(this, class: "toggle-play-patternboxprojectitemview");
		togglePlay.action = {|sender| this.onTogglePlay(sender); };
		mainLayout.add(togglePlay, 0, 0);

		sliderVolume = SliderViewFactory.createInstance(this, controlSpec: \db.asSpec, initVal: volume, labelText: patternBoxView.patternBoxName)
		.action_({ |sender | this.onSliderVolumeChanged(sender.value); });
        mainLayout.add(sliderVolume, 0, 1);
		mainLayout.setColumnStretch(1, 1);

		buttonShowPatternBox = ButtonFactory.createInstance(this, class: "btn-patternboxprojectitemview-showpatternbox");
		buttonShowPatternBox.action = { |sender| this.onButtonShowPatternBox(sender); };
        layout.add(buttonShowPatternBox, 0, 2);

		buttonRemove = ButtonFactory.createInstance(this, class: "btn-delete");
		buttonRemove.action = { |sender| this.onButtonRemove(sender); };
		layout.add(buttonRemove, 0, 3, align: \top);
    }

	onPatternBoxNameChanged { |sender|
		sliderVolume.labelText = sender.patternBoxName;
		patternBoxName = sender.patternBoxName;
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
