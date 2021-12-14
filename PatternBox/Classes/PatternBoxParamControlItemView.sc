/*
FILENAME: PatternBoxParamControlGroupView

DESCRIPTION: Select an compose widgest.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

a = PatternBoxParamControlItemView().front;
a.bounds
*/

PatternBoxParamControlItemView : View {

    var <>spec, <>keyName, popupSelectControl, controlSpecView, controlView, textFieldControlName, mainLayout, buttonRemove, <editMode, <patternProxy, <value;
    var <>actionRemove, <>actionItemChanged, <>actionControlNameChanged, <controlName, <>selectedControlType;
    var editMode = false;
    *new { |parent, bounds|
        ^super.new(parent, bounds).initialize();
    }

    initialize {
        patternProxy = PatternProxy();
        this.spec = ControlSpec();
        this.initializeView();
        this.onItemChanged_PopupSelectControl("slider");
    }

    initializeView {
        mainLayout = GridLayout();
        mainLayout.margins = 0!4;
        this.layout = mainLayout;
        popupSelectControl = PopUpMenu();
        popupSelectControl.items = ["slider", "range", "steps", "multislider" ];
        popupSelectControl.action = { |sender| this.onItemChanged_PopupSelectControl(sender.item); };
        mainLayout.add(popupSelectControl, 0, 0);
        textFieldControlName = TextField();
        textFieldControlName.action = { |sender| this.onControlNameChanged_TextField(sender.string.stripWhiteSpace) };
        mainLayout.add(textFieldControlName, 0, 1);
        buttonRemove = ButtonFactory.createInstance(this, class: "btn-delete");
        buttonRemove.action = { if (actionRemove.notNil, { actionRemove.value(this); }); };
        mainLayout.add(buttonRemove, 0, 2);
	    controlSpecView = ControlSpecView();
		controlSpecView.action = { |sender| this.onSpecChanged_ControlSpecView(sender); };
	 	mainLayout.addSpanning(controlSpecView, 1, columnSpan: 3);
	}

    editMode_ { |mode|
        buttonRemove.visible  = mode;
        popupSelectControl.visible = mode;
        textFieldControlName.visible = mode;
        controlSpecView.visible = mode;
        if (controlView.notNil, { controlView.editMode = mode; });
        editMode = mode;
        if (controlView.notNil, { controlView.editMode = mode; });
    }

	controlName_ { |name|
		controlName = name;
		textFieldControlName.string = name;
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
		controlView.spec = spec;
		controlView.uiMode(\brief);
        mainLayout.addSpanning(controlView, 2, 0, columnSpan: 3);
    }

    randomize {
        if (controlView.notNil, { controlView.randomize(); });
    }

    getState {
		// selectedControlType
		// controlSpecView state
		// textFieldControlName
    }

    loadState {
		// selectedControlType
		// controlSpecView stae
		// textFieldControlName value action
    }
}
