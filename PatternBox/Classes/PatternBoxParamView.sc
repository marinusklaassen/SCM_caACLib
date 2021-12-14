/*
FILENAME: PatternBoxParamView

DESCRIPTION: View to compose patterns and assign view controls to synth arguments/event keys.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
(
w = View(bounds:500@700).front;
l = VLayout([nil, stretch:1, align: \bottom]);
w.layout = l;
10 do: { | position| l.insert(ScoreParamView().actionButtonDelete_({ | sender | "delete".postln; }).actionNameChanged_({ | sender | sender.keyName.postln; }).actionPatternScriptChanged_({ |sender| sender.string.postln; }), position) };
)
TODO:
Slider en range value hernoemen naar gewoon value & range (KISS).

a = PatternBoxParamView().front;
*/

PatternBoxParamView : View {
	var <>patternBoxContext, buttonDelete, <scriptFieldView, setValueFunction, dependants, <paramController, <name, <>currentLayerIndex, <>currentWidgetType, <>currentWidgetIndex, previousLayer;
	var <>actionNameChanged, <>removeAction, <>index, <>paramProxy, <>controllerProxies, <>scriptFunc, <>actionButtonDelete, <>rangeSliderAction, <>sliderAction, <>actionPatternScriptChanged, <>actionPatternTargetIDChanged;
	var layoutStackControlSection, controlNoControl, controlSlider, controlRangeSlider, <keyName, <patternTargetID, mainLayout, textpatternTargetID, textPatternKeyname, layoutScriptControllerSection, scorePatternScriptEditorView;
	var patternBoxParamControlSectionView, buttonSelectScriptView, buttonSelectScriptOrSpecOpControlStack, buttonYellow, buttonSelectControlType, <>actionMoveUp, <>actionMoveDown;

	*new { | patternBoxContext, parent, bounds |
		^super.new(parent, bounds).initialize(patternBoxContext);
	}

	keyName_ { | string |
		textPatternKeyname.string = string;
		keyName = string;
	}

	patternTargetID_ { | string |
		textpatternTargetID.string = string;
		patternTargetID = string;
	}

	initialize { | patternBoxContext |
		this.patternBoxContext = patternBoxContext;
		paramProxy = PatternProxy(1); // patternProxy
		this.initializeView();
	}

	initializeView {

		this.background = Color.black.alpha_(0.15);
		mainLayout = HLayout();
		mainLayout.margins = [5, 5, 20, 5];
		this.layout = mainLayout;

		mainLayout.add(
			Button()
			.fixedWidth_(20)
			.string_("↑")
			.action_({  if (actionMoveUp.notNil, { actionMoveUp.value(this) }); })
			,align: \top);

		mainLayout.add(
			Button()
			.fixedWidth_(20)
			.string_("↓")
			.action_({  if (actionMoveDown.notNil, { actionMoveDown.value(this) }); })
		    ,align: \top);

		textpatternTargetID = TextFieldFactory.createInstance(this, "text-patternboxpatterntargetid");
		textpatternTargetID.action = { | sender |
			var patternTargetIDStripped = sender.string.stripWhiteSpace();
			this.patternTargetID = patternTargetIDStripped;
			textpatternTargetID.string = patternTargetIDStripped;
			actionPatternTargetIDChanged.value(this);
		};

		mainLayout.add(textpatternTargetID, align: \top);

		textPatternKeyname = TextFieldFactory.createInstance(this, "text-patternboxparamview");
		textPatternKeyname.action = { | sender |
			var keyNameStripped = sender.string.stripWhiteSpace();
			patternBoxParamControlSectionView.controlNameDefault = keyNameStripped;
			this.keyName = keyNameStripped;
			textPatternKeyname.string = keyNameStripped;
			actionNameChanged.value(this);
		};

		mainLayout.add(textPatternKeyname, align: \top);

		layoutScriptControllerSection = VLayout();
		layoutScriptControllerSection.margins = 0!4;

		mainLayout.add(layoutScriptControllerSection, align: \top, stretch: 1.0);

		scriptFieldView = ScriptFieldViewFactory.createInstance(this, "script-patternboxparamview");
		scriptFieldView.action = { | sender |
			this.onActionPatternScriptChanged(sender);
		};

		layoutScriptControllerSection.add(scriptFieldView);

		patternBoxParamControlSectionView = PatternBoxParamControlGroupView();
        patternBoxParamControlSectionView.layout.margins = 0!4;
		layoutScriptControllerSection.add(patternBoxParamControlSectionView);

		buttonSelectScriptView = Button();
		buttonSelectScriptView.maxWidth = 24;
		buttonSelectScriptView.states = [[""] ++ Color.red.dup(2)];
		buttonSelectScriptView.action = {
			layoutScriptControllerSection.index = 0;
		};
		mainLayout.add(buttonSelectScriptView, align: \top);

		buttonSelectScriptOrSpecOpControlStack = Button();
		buttonSelectScriptOrSpecOpControlStack.maxWidth = 24;
		buttonSelectScriptOrSpecOpControlStack.states = [[""] ++ Color.blue.dup(2)];
		buttonSelectScriptOrSpecOpControlStack.action = {
			patternBoxParamControlSectionView.visible = patternBoxParamControlSectionView.visible.not;
		};
		mainLayout.add(buttonSelectScriptOrSpecOpControlStack, align: \top);

		buttonYellow = Button();
		buttonYellow.maxWidth = 24;
		buttonYellow.states = [[""] ++ Color.yellow.dup(2)];
		buttonYellow.action = {
			// tobe specified
		};
		mainLayout.add(buttonYellow, align: \top);

		buttonSelectControlType = Button();
		buttonSelectControlType.maxWidth = 24;
		buttonSelectControlType.states = [[""] ++ Color.black.dup(2)];
		buttonSelectControlType.action = {
			patternBoxParamControlSectionView.editMode = patternBoxParamControlSectionView.editMode.not;
		};
		mainLayout.add(buttonSelectControlType, align: \top);

		buttonDelete = ButtonFactory.createInstance(this, class: "btn-delete");
		buttonDelete.action = {
			if (actionButtonDelete.notNil, { actionButtonDelete.value(this) });
		};
		mainLayout.add(buttonDelete, align: \top);
	}

	onActionPatternScriptChanged { | sender |
		// Evalueer deze code ook bij wijziging van een UI control
		var func = nil;
		sender.clearError();
		try {
			//
			// TODO fader, rangeLo, rangeHi, param namen op basis van de controlitemgroup
			func = interpret("{ | env| " ++  sender.string ++ "}");
		};

		if (func.notNil) {
			this.scriptFunc = func;
			paramProxy.source = func.value(
				// controlgroupparams injecteren
				patternBoxContext.model[\environment]);
		} {
			scriptFieldView.setError("Invalid input.");
		};
	}

	randomize {
		patternBoxParamControlSectionView.randomize();
	}

	getState {
		var state = Dictionary();
		state[\type] = "PatternBoxParam";
		state[\patternTargetID] = patternTargetID;
		state[\paramName] = keyName;
		state[\patternBoxParamControlSectionView] = patternBoxParamControlSectionView.getState();
		state[\scriptView] = scriptFieldView.getState();

		^state;
	}

	loadState { |state|
		this.patternTargetID = state[\patternTargetID];
		actionPatternTargetIDChanged.value(this);
		this.keyName = state[\paramName];
		actionNameChanged.value(this);
		state[\patternBoxParamControlSectionView] = patternBoxParamControlSectionView.loadState();
		scriptFieldView.loadState(state[\scriptView]);
	}

	dispose {
		this.remove();
	}
}

