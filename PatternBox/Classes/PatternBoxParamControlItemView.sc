/*
FILENAME: PatternBoxParamControlGroupView

DESCRIPTION: Select an compose widgest.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

a = PatternBoxParamControlItemView().front;
a.bounds
*/

PatternBoxParamControlItemView : View {

    var <>spec, <>keyName, popupSelectControl, labelControlName, controlSpecView, controlView, textFieldControlName, mainLayout, buttonRemove, <editMode, <patternProxy, <value;
    var <>actionRemove, <>actionControlItemChanged, <>actionControlNameChanged, <controlName, <>selectedControlType;
    var editMode = false;
    *new { |name, parent, bounds|
        ^super.new(parent, bounds).initialize(name);
    }

    initialize { |name|
        patternProxy = PatternProxy();
        this.spec = ControlSpec();
        this.initializeView();
		this.controlName = name;
        this.onItemChanged_PopupSelectControl("slider");
    }

    initializeView {
		this.background = Color.black.alpha = 0.2;
        mainLayout = GridLayout();
		mainLayout.margins = 4!4;
        this.layout = mainLayout;
		labelControlName = StaticText();
		mainLayout.add(labelControlName, 0, 0);
		popupSelectControl = PopUpMenu();
        popupSelectControl.items = ["slider", "range", "steps", "multislider" ];
        popupSelectControl.action = { |sender| this.onItemChanged_PopupSelectControl(sender.item); };
        mainLayout.add(popupSelectControl, 1, 0);
        textFieldControlName = TextField();
        textFieldControlName.action = { |sender| this.onControlNameChanged_TextField(sender.string.stripWhiteSpace) };
        mainLayout.add(textFieldControlName, 1, 1);
        buttonRemove = ButtonFactory.createInstance(this, class: "btn-delete");
        buttonRemove.action = { if (actionRemove.notNil, { actionRemove.value(this); }); };
        mainLayout.add(buttonRemove, 0, 1, align: \right);
	    controlSpecView = ControlSpecView();
		controlSpecView.action = { |sender| this.onSpecChanged_ControlSpecView(sender); };
	 	mainLayout.addSpanning(controlSpecView, 2, columnSpan: 2);
	}

    editMode_ { |mode|
        buttonRemove.visible  = mode;
        popupSelectControl.visible = mode;
        textFieldControlName.visible = mode;
        controlSpecView.visible = mode;
		editMode = mode;
        if (controlView.notNil, { controlView.editMode = mode; });
    }

	controlName_ { |name|
		controlName = name;
		textFieldControlName.string = name;
		labelControlName.string = name;
	}

    controlNameAction_ { |name|
        this.onControlNameChanged_TextField(name);
    }

	onSpecChanged_ControlSpecView { |sender|
		if (controlView.notNil, { controlView.spec = sender.spec; });
		this.spec = sender.spec;
	}

    onControlNameChanged_TextField { |name|
        this.controlName = name;
		if (controlView.notNil, { controlView.name = name; });
        actionControlNameChanged.value(this);
    }

    onItemChanged_PopupSelectControl { |type|
        controlView.remove;
		selectedControlType = type;
        case
		{ type == "slider" } { controlView = SliderView(); }
		{ type == "range" } { controlView = RangeSliderView(); }
		{ type == "steps" } {controlView = MultiStepView(); }
		{ type == "multislider" } { controlView = SliderSequencerView(); };
		controlView.name = this.controlName;
		controlView.spec = spec;
		controlView.uiMode(\brief);
		controlView.editMode = this.editMode;
        mainLayout.addSpanning(controlView, 3, 0, columnSpan: 2);
		if (actionControlItemChanged.notNil, { this.actionControlItemChanged.value(this); });
    }

    randomize {
        if (controlView.notNil, { controlView.randomize(); });
    }

	getProxies {
		if (controlView.notNil, {
			^controlView.getProxies(); });
	}

    getState {
		var state = Dictionary();
		state[\selectedControlType] = selectedControlType;
		state[\controlSpecView] = controlSpecView.getState();
		state[\textFieldControlName] = controlName;
		state[\controlView] = controlView.getState();
		^state;
    }

    loadState { |state|
		var index = 0;
		this.onItemChanged_PopupSelectControl(state[\selectedControlType]);
		popupSelectControl.items do: { |item, i| if (item == state[\selectedControlType], { index = i; }); };
		popupSelectControl.value = index;
		controlSpecView.loadState(state[\controlSpecView]);
		this.controlNameAction = state[\textFieldControlName];
		controlView.loadState(state[\controlView]);
    }
}
