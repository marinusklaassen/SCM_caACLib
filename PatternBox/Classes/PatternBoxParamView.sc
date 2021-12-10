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
	var buttonDelete, <scriptFieldView, controlSpec, model, setValueFunction, dependants, <paramController, <controlSpecView, ez4Buttons, <name, <>currentLayerIndex, <>currentWidgetType, <>currentWidgetIndex, previousLayer;
	var <>actionNameChanged, <>removeAction, <>index, <>paramProxy, <>controllerProxies, <>scriptFunc, <>actionButtonDelete, <>rangeSliderAction, <>sliderAction, <>actionPatternScriptChanged, <>actionpatternTargetIDChanged;
	var layoutStackControlSection, controlNoControl, controlSlider, controlRangeSlider, <keyName, <patternTargetID, mainLayout, textpatternTargetID, textPatternKeyname, layoutStackVariableSection, scorePatternScriptEditorView;
    var buttonSelectScriptView, buttonSelectScriptOrSpecOpControlStack, buttonShowSpecEditor, buttonSelectControlType, <>actionMoveUp, <>actionMoveDown;

	*new { | parent, bounds |
		^super.new(parent, bounds).initialize();
	}

	keyName_ { | string |
		textPatternKeyname.string = string;
		keyName = string;
	}

	patternTargetID_ { | string |
		textpatternTargetID.string = string;
		patternTargetID = string;
	}

	initialize {
		controlSpec = ControlSpec();
		this.initializeModelAndEvents();
		this.initializeView();
     }

	initializeModelAndEvents {

		model = (sliderValue: 0, rangeSliderValues: [0, 1]);
		dependants = ();
		setValueFunction = ();

		setValueFunction[\sliderValue] = { |value|
			model[\sliderValue] = value;
			model.changed(\sliderValue, value);
		};
		setValueFunction[\rangeSliderValues] = { |value|
			model[\rangeSliderValues] = value;
			model.changed(\rangeSliderValues, value);
		};

		model.addDependant({|theChanger, what, val|
			if (what == \sliderValue) {
				if (sliderAction.notNil) { sliderAction.value(controlSpec.map(val)) };
			};
		});

		model.addDependant({|theChanger, what, val|
			if (what == \rangeSliderValues) {
				if (rangeSliderAction.notNil) { rangeSliderAction.value(controlSpec.map(val)); };
			};
		});
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
			.action_({  if (actionMoveUp.notNil, { actionMoveUp.value(this) }); }));

		mainLayout.add(
			Button()
			.fixedWidth_(20)
			.string_("↓")
			.action_({  if (actionMoveDown.notNil, { actionMoveDown.value(this) }); }));

		textpatternTargetID = TextFieldFactory.createInstance(this, "text-patternboxpatterntargetid");
		textpatternTargetID.action = { | sender |
			var patternTargetIDStripped = sender.string.stripWhiteSpace();
			this.patternTargetID = patternTargetIDStripped;
			textpatternTargetID.string = patternTargetIDStripped;
			actionpatternTargetIDChanged.value(this);
		};

		mainLayout.add(textpatternTargetID, align: \top);

		textPatternKeyname = TextFieldFactory.createInstance(this, "text-patternboxparamview");
		textPatternKeyname.action = { | sender |
			var keyNameStripped = sender.string.stripWhiteSpace();
			controlSpecView.setSpecByString(keyNameStripped);
			this.keyName = keyNameStripped;
			textPatternKeyname.string = keyNameStripped;
			actionNameChanged.value(this);
		};

        mainLayout.add(textPatternKeyname, align: \top);

        layoutStackVariableSection = StackLayout();
		layoutStackVariableSection.margins = 0!4;

        mainLayout.add(layoutStackVariableSection, align: \top, stretch: 1.0);

		scriptFieldView = ScriptFieldViewFactory.createInstance(this, "script-patternboxparamview");
		scriptFieldView.action = { | sender |
			this.actionPatternScriptChanged.value(this);
		};
		layoutStackVariableSection.add(scriptFieldView);

	    controlSpecView = ControlSpecViewFactory.createInstance(this);
		controlSpecView.action = { | sender |
			controlSpec = sender.controlSpec;
			model.changed(\sliderValue, model[\sliderValue]);
			model.changed(\rangeSliderValues, model[\rangeSliderValues]);
		};

		layoutStackVariableSection.add(controlSpecView);

		// Controls within a separate stacklayout.
		layoutStackControlSection = StackLayout();
        layoutStackControlSection.margins = 0!4;

		layoutStackVariableSection.add(View().layout_(layoutStackControlSection));

		controlNoControl = StaticTextFactory.createInstance(this);
		controlNoControl.string = "No control";

		layoutStackControlSection.add(controlNoControl);

		controlSlider = SliderFactory.createInstance(this, class: "slider-horizontal");
		controlSlider.value = model[\sliderValue];
		controlSlider.action = { |val|
		  setValueFunction[\sliderValue].value(val.value);
	    };

	    model.addDependant({ |theChanger, what, val|
			if (what == \sliderValue, {
			{ controlSlider.value = val; }.defer; // fire async
		    });
	    });
	    layoutStackControlSection.add(controlSlider);

	    controlRangeSlider = RangeSliderFactory.createInstance(this, class: "slider-horizontal");
	    controlRangeSlider.lo = model[\rangeSliderValues][0];
	    controlRangeSlider.hi = model[\rangeSliderValues][1];
		controlRangeSlider.action = { |val|
		    setValueFunction[\rangeSliderValues].value([val.lo,val.hi])
        };

        model.addDependant({|theChanger, what, range |
		    if (what == \rangeSliderValues, {
		      {
					controlRangeSlider.lo_(range[0]).hi_(range[1]);
				}.defer; // fire async
            });
		});
        layoutStackControlSection.add(controlRangeSlider);

        buttonSelectScriptView = Button();
	    buttonSelectScriptView.maxWidth = 24;
		buttonSelectScriptView.states = [[""] ++ Color.red.dup(2)];
	    buttonSelectScriptView.action = {
		    layoutStackVariableSection.index = 0;
	    };
        mainLayout.add(buttonSelectScriptView, align: \top);

	    buttonSelectScriptOrSpecOpControlStack = Button();
			buttonSelectScriptOrSpecOpControlStack.maxWidth = 24;
		buttonSelectScriptOrSpecOpControlStack.states = [[""] ++ Color.blue.dup(2)];
	    buttonSelectScriptOrSpecOpControlStack.action = {
		   layoutStackVariableSection.index = layoutStackVariableSection.index + 1 % layoutStackVariableSection.count;
	    };
		mainLayout.add(buttonSelectScriptOrSpecOpControlStack, align: \top);

	    buttonShowSpecEditor = Button();
		buttonShowSpecEditor.maxWidth = 24;
		buttonShowSpecEditor.states = [[""] ++ Color.yellow.dup(2)];
	    buttonShowSpecEditor.action = {
		   layoutStackVariableSection.index = 1;
	    };
        mainLayout.add(buttonShowSpecEditor, align: \top);

	    buttonSelectControlType = Button();
		buttonSelectControlType.maxWidth = 24;
		buttonSelectControlType.states = [[""] ++ Color.black.dup(2)];
	    buttonSelectControlType.action = {
		  layoutStackVariableSection.index = 2;
          layoutStackControlSection.index = layoutStackControlSection.index + 1 % layoutStackControlSection.count;
	    };
        mainLayout.add(buttonSelectControlType, align: \top);

		buttonDelete = ButtonFactory.createInstance(this, class: "btn-delete");
		buttonDelete.action = {
		  if (actionButtonDelete.notNil, { actionButtonDelete.value(this) });
	    };
		mainLayout.add(buttonDelete, align: \top);
	}

	randomize {
		setValueFunction[\sliderValue].value(1.0.rand); // setValue moet gewoon een setValue, setRange, setControlSpec method word. En deze method invoke een event.
		setValueFunction[\rangeSliderValues].value(sort({1.0.rand}!2));
	}

	getState {
		var state = Dictionary();
        state[\type] = "PatternBoxParam";
		state[\layoutStackVariableSectionIndex] = layoutStackVariableSection.index;
		state[\layoutStackControlSectionIndex] = layoutStackControlSection.index;
		state[\patternTargetID] = patternTargetID;
		state[\paramName] = keyName;
		state[\value] = model[\sliderValue];
		state[\range] = model[\rangeSliderValues];
		state[\scriptView] = scriptFieldView.getState();
		state[\controlSpecView] = controlSpecView.getState();
		^state;
	}

	loadState { |state|
		controlSpecView.setSpecByString(state[\paramName]);
		this.patternTargetID = state[\patternTargetID];
		actionpatternTargetIDChanged.value(this);
		this.keyName = state[\paramName];
		actionNameChanged.value(this);
		controlSpecView.loadState(state[\controlSpecView]); // TODO Call method SetControlSpec
		setValueFunction[\sliderValue].value(state[\value]); // Call TODO method SetValue
		setValueFunction[\rangeSliderValues].value(state[\range]); // TODO Call method SetRange
		scriptFieldView.loadState(state[\scriptView]);  // TODO Call method SetScript
		layoutStackVariableSection.index = state[\layoutStackVariableSectionIndex];
		layoutStackControlSection.index = state[\layoutStackControlSectionIndex];
	}

	dispose {
		this.remove();
	}
}

