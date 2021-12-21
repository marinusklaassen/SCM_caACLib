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
	var skipRegenerate = false, patternBoxParamControlSectionView, buttonSelectScriptView, buttonSelectScriptOrSpecOpControlStack, buttonSwitchEditingMode, buttonRandomizeControls, <>actionMoveUp, <>actionMoveDown;

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
		textpatternTargetID.toolTip = "Set the target ID of the pbind. For each unique ID a pbind is constructed.";
		textpatternTargetID.action = { | sender |
			var patternTargetIDStripped = sender.string.stripWhiteSpace();
			this.patternTargetID = patternTargetIDStripped;
			textpatternTargetID.string = patternTargetIDStripped;
			actionPatternTargetIDChanged.value(this);
		};

		mainLayout.add(textpatternTargetID, align: \top);

		textPatternKeyname = TextFieldFactory.createInstance(this, "text-patternboxparamview");
		textPatternKeyname.toolTip = "The key where a fixed value, a pattern or controls (via pattern proxies) can be assigned.";
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
		scriptFieldView.toolTip = "Set a fixed value, a pattern or controls (via pattern proxies) here.";
		scriptFieldView.action = { | sender |
			this.regenerateAndInterpretedParamScript();
		};

		layoutScriptControllerSection.add(scriptFieldView);

		patternBoxParamControlSectionView = PatternBoxParamControlGroupView();
		patternBoxParamControlSectionView.layout.margins = 0!4;
		patternBoxParamControlSectionView.visible = false;
		patternBoxParamControlSectionView.editMode = false;
		patternBoxParamControlSectionView.actionControlCRUD = { |sender|
			this.regenerateAndInterpretedParamScript();
		};

		layoutScriptControllerSection.add(patternBoxParamControlSectionView);

		buttonSelectScriptView = Button();
		buttonSelectScriptView.maxWidth = 24;
		buttonSelectScriptView.toolTip = "Print available script env & control parameters to console.";
		buttonSelectScriptView.states = [[""] ++ Color.red.dup(2)];
		buttonSelectScriptView.action = {
			"Not yet implemented: Print available script env & control parameters to console.".postln;
		};
		mainLayout.add(buttonSelectScriptView, align: \top);

		buttonSelectScriptOrSpecOpControlStack = Button();
		buttonSelectScriptOrSpecOpControlStack.toolTip = "Switch show or hide controls.";
		buttonSelectScriptOrSpecOpControlStack.maxWidth = 24;
		buttonSelectScriptOrSpecOpControlStack.states = [[""] ++ Color.blue.dup(2)];
		buttonSelectScriptOrSpecOpControlStack.action = {
			if (patternBoxParamControlSectionView.visible, {
				patternBoxParamControlSectionView.visible = false;
			},{
				patternBoxParamControlSectionView.visible = true;
				if (patternBoxParamControlSectionView.controlItems.size == 0, {
					patternBoxParamControlSectionView.editMode = true;
				});
			});
		};
		mainLayout.add(buttonSelectScriptOrSpecOpControlStack, align: \top);

		buttonSwitchEditingMode = Button();
		buttonSwitchEditingMode.toolTip = "Switch enable or disable editmode.";
		buttonSwitchEditingMode.maxWidth = 24;
		buttonSwitchEditingMode.states = [[""] ++ Color.yellow.dup(2)];
		buttonSwitchEditingMode.action = {
			if (patternBoxParamControlSectionView.editMode, {
				patternBoxParamControlSectionView.editMode =  false;
				if (patternBoxParamControlSectionView.controlItems.size == 0, {
					patternBoxParamControlSectionView.visible = false;
				});
			},{
				patternBoxParamControlSectionView.editMode = true;
				patternBoxParamControlSectionView.visible = true;
			});
		};
		mainLayout.add(buttonSwitchEditingMode, align: \top);

		buttonRandomizeControls = Button();
		buttonRandomizeControls.toolTip = "Randomize controls";
		buttonRandomizeControls.maxWidth = 24;
		buttonRandomizeControls.states = [[""] ++ Color.black.dup(2)];
		buttonRandomizeControls.action = {
			this.randomize();
		};
		mainLayout.add(buttonRandomizeControls, align: \top);

		buttonDelete = ButtonFactory.createInstance(this, class: "btn-delete");
		buttonDelete.toolTip = "Remove this pattern key control section.";
		buttonDelete.action = {
			if (actionButtonDelete.notNil, { actionButtonDelete.value(this) });
		};
		mainLayout.add(buttonDelete, align: \top);
	}

	regenerateAndInterpretedParamScript {
		// Evalueer deze code ook bij wijziging van een UI control
		var func, funcAsString, proxies, paramString, keyValuesProxyPairs;
		if (skipRegenerate.notNil && scriptFieldView.string.notNil, {
			try {
				scriptFieldView.clearError();
				proxies = patternBoxParamControlSectionView.getProxies();
				proxies[\env] = patternBoxContext.model[\environment];
				keyValuesProxyPairs = proxies.getPairs();
				keyValuesProxyPairs do: { |item, i|
					if (i % 2 == 0, {
						paramString = paramString ++ if(i == 0, "", " , ") ++ item;
					});
				};
				funcAsString = "{ |" + paramString + "|" +  scriptFieldView.string + "}";
				funcAsString.postln;
				func = interpret(funcAsString);
			};
			if (func.notNil) {
				this.scriptFunc = func;
				paramProxy.source = func.performKeyValuePairs(\value, keyValuesProxyPairs.postln);
			} {
				scriptFieldView.setError("Invalid input.");
			};
		});
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
		skipRegenerate =true;
		this.patternTargetID = state[\patternTargetID];
		this.keyName = state[\paramName];
		actionNameChanged.value(this);
		actionPatternTargetIDChanged.value(this);
		patternBoxParamControlSectionView.loadState(state[\patternBoxParamControlSectionView]);
		scriptFieldView.loadState(state[\scriptView]);
		skipRegenerate = false;
		this.regenerateAndInterpretedParamScript();
	}

	dispose {
		this.remove();
	}
}

