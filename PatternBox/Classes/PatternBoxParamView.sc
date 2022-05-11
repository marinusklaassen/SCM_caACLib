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
	var <>bufferpool, <>context, buttonDelete, <scriptFieldView, dragBothPanel, setValueFunction, dependants, <paramController, <name, <>currentLayerIndex, <>currentWidgetType, <>currentWidgetIndex, previousLayer;
	var <>actionNameChanged, <>removeAction, <>index, <>paramProxy, <>controllerProxies, <>scriptFunc, <>actionButtonDelete, <>rangeSliderAction, <>sliderAction, <>actionPatternScriptChanged, <>actionPatternTargetIDChanged;
	var layoutStackControlSection, controlNoControl, controlSlider, controlRangeSlider, <keyName, mainLayout, textpatternTargetID, textPatternKeyname, layoutScriptControllerSection, scorePatternScriptEditorView;
	var canInterpret = false, patternBoxParamControlSectionView, buttonSelectScriptView, buttonSelectScriptOrSpecOpControlStack, buttonSwitchEditingMode, buttonRandomizeControls;
	var <>actionMoveParamView, <isPbind, <pbind, prBeginDragAction, prCanReceiveDragHandler, prReceiveDragHandler, <>actionInsertPatternBox;

	*new { | context, bufferpool, parent, bounds |
		^super.new(parent, bounds).initialize(context, bufferpool);
	}

	keyName_ { | string |
		textPatternKeyname.string = string;
		keyName = string;
	}

	initialize { | context, bufferpool |
		this.context = context;
		this.bufferpool = bufferpool;
		keyName = "";
		paramProxy = PatternProxy(1); // patternProxy
		this.initializeView();
		canInterpret = true;
	}

	initializeView {

		this.background = Color.black.alpha_(0.15);
		this.toolTip = "Press CMD + drag to move this item to another position.";
		mainLayout = HLayout();
		mainLayout.margins = [5, 5, 20, 5];
		this.layout = mainLayout;

		this.setContextMenuActions(
			MenuAction("Insert param row before", {
				if (actionInsertPatternBox.notNil, { actionInsertPatternBox.value(this, "INSERT_BEFORE"); });
			}),
			MenuAction("Insert param row after", {
				if (actionInsertPatternBox.notNil, { actionInsertPatternBox.value(this, "INSERT_AFTER"); });
			})
		);

		dragBothPanel = DragBoth();
		dragBothPanel.maxWidth = 24;
		dragBothPanel.minWidth = 24;
		dragBothPanel.background = Color.black.alpha_(0.5);
		dragBothPanel.toolTip = this.toolTip;
		mainLayout.add(dragBothPanel, align: \top);

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

		patternBoxParamControlSectionView = PatternBoxParamControlGroupView(bufferpool: bufferpool);
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
			this.editMode = patternBoxParamControlSectionView.editMode.not;
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

		// Start drag & drop workaround
		prBeginDragAction =  { |view, x, y|
			this; // Current instance is the object to drag.
		};

		prCanReceiveDragHandler = {  |view, x, y|
			var canReceive = View.currentDrag.isKindOf(PatternBoxParamView);
			if (View.currentDrag.isKindOf(PatternBoxParamView), {
				if ((View.currentDrag.context != context) && (View.currentDrag.keyName.size > 0), {
						context.paramViews do: { |view| if (canReceive, { canReceive = View.currentDrag.keyName != view.keyName; }) };
				});
			});
			canReceive;
		};

		prReceiveDragHandler = { |view, x, y|
			if (actionMoveParamView.notNil && View.currentDrag.isKindOf(PatternBoxParamView), { actionMoveParamView.value(this, View.currentDrag); });
		};

		this.setDragAndDropBehavior(this);
		this.setDragAndDropBehavior(dragBothPanel);
		this.setDragAndDropBehavior(buttonSelectScriptOrSpecOpControlStack);
		this.setDragAndDropBehavior(buttonSelectScriptView);
		this.setDragAndDropBehavior(buttonSwitchEditingMode);
		this.setDragAndDropBehavior(buttonRandomizeControls);
		this.setDragAndDropBehavior(textPatternKeyname);
		this.setDragAndDropBehavior(scriptFieldView.textEditing);
	}

	setDragAndDropBehavior { |object|
		object.dragLabel = "A PatternBox parameter.";
		object.beginDragAction = prBeginDragAction;
		object.canReceiveDragHandler = prCanReceiveDragHandler;
		object.receiveDragHandler = prReceiveDragHandler;
	}

	isKeyNameChanged {
		^textPatternKeyname.string != keyName;
	}

	regenerateAndInterpretedParamScript {
		// Evalueer deze code ook bij wijziging van een UI control
		var func, funcAsString, proxies, paramString = "", keyValuesProxyPairs;
		if (canInterpret  && scriptFieldView.string.stripWhiteSpace().notEmpty, {
			try {

				scriptFieldView.clearError();
				proxies = patternBoxParamControlSectionView.getProxies();
				if (context.notNil, {
					proxies[\env] = context.context.model[\environment]; });
				keyValuesProxyPairs = proxies.getPairs();
				keyValuesProxyPairs do: { |item, i|
					if (i % 2 == 0, {
						paramString = paramString ++ if(i == 0, "", " , ") ++ item;
					});
				};
				if (paramString.notEmpty, {
					paramString = "|"+paramString+"|";
				});
				funcAsString = "{ " + paramString +  scriptFieldView.string + "}";
				func = interpret(funcAsString);
			};
			if (func.notNil) {
				var result = func.performKeyValuePairs(\value, keyValuesProxyPairs);
				isPbind = result.isKindOf(Pbind);
				pbind = nil;
				if (isPbind == true, {
					pbind = result;
				});
				this.scriptFunc = func;
				paramProxy.source = result;
				if (this.isKeyNameChanged(), {
					this.keyName = textPatternKeyname.string;
				});
				actionPatternScriptChanged.value(this);
			} {
				scriptFieldView.setError("Invalid input.");
			};
		});
	}

	editMode_ { |mode|
		if (mode, {
			patternBoxParamControlSectionView.editMode = true;
			patternBoxParamControlSectionView.visible = true;
		}, {
			patternBoxParamControlSectionView.editMode =  false;
			if (patternBoxParamControlSectionView.controlItems.size == 0, {
				patternBoxParamControlSectionView.visible = false;
			});
		});
	}
	randomize {
		patternBoxParamControlSectionView.randomize();
	}

	getState {
		var state = Dictionary();
		state[\type] = "PatternBoxParam";
		state[\paramName] = keyName;
		state[\patternBoxParamControlSectionView] = patternBoxParamControlSectionView.getState();
		state[\scriptView] = scriptFieldView.getState();
		^state;
	}

	loadState { |state|
		canInterpret = false;
		this.keyName = state[\paramName];
		actionNameChanged.value(this);
		patternBoxParamControlSectionView.loadState(state[\patternBoxParamControlSectionView]);
		scriptFieldView.loadState(state[\scriptView]);
		canInterpret = true;
		this.regenerateAndInterpretedParamScript();
	}

	dispose {
		this.remove();
	}
}

