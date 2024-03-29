/*
FILENAME: PatternBoxParamControlItemView

DESCRIPTION: Select an compose widgest.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

a = PatternBoxParamControlItemView().front;
a.bounds
*/

PatternBoxParamControlItemView : View {

    var <>bufferpool, <>spec, <>keyName, buttonRandomize, popupSelectControl, labelControlName, controlSpecView, <controlView, textFieldControlName, mainLayout, buttonRemove, <editMode, <patternProxy, <value;
    var <>actionRemove, <>actionMoveDown, <>actionMoveUp, <>actionControlItemChanged, <>actionControlNameChanged, <controlName, <>selectedControlType;
    var editMode = false;
	var prCanReceiveDragHandler, prReceiveDragHandler, prBeginDragAction, <>actionMoveControlItem, <>actionInsertControlItem;

    *new { |name, bufferpool, parent, bounds|
        ^super.new(parent, bounds).initialize(name, bufferpool);
    }

    initialize { |name, bufferpool|
        patternProxy = PatternProxy();
		this.bufferpool = bufferpool;
        this.spec = ControlSpec();
        this.initializeView();
		this.controlName = name;
        this.onItemChanged_PopupSelectControl("slider");
    }

    initializeView {
		this.background = Color.black.alpha = 0.2;
		this.toolTip = "Configure control behavior.";
		mainLayout = GridLayout();
		mainLayout.margins = 4!4;
        this.layout = mainLayout;

		this.setContextMenuActions(
			MenuAction.separator.string_("Item"),
			MenuAction("Insert control before", {
				if (actionInsertControlItem.notNil, { actionInsertControlItem.value(this, "INSERT_BEFORE"); });
			}),
			MenuAction("Insert control after", {
				if (actionInsertControlItem.notNil, { actionInsertControlItem.value(this, "INSERT_AFTER"); });
			}),
			MenuAction("Duplicate control", {
				if (actionInsertControlItem.notNil, { actionInsertControlItem.value(this, "DUPLICATE"); });
			}),
		    MenuAction("Remove control", {
				if (actionRemove.notNil, { actionRemove.value(this); }); }),
			MenuAction.separator.string_("Control"),
			MenuAction("Randomize", {
				this.randomize();
			}),
			MenuAction("Low", {
				this.toLow();
			}),
			MenuAction("High", {
				this.toHigh();
			}),
			MenuAction("Center", {
				this.toCenter();
			})
		);

		labelControlName = StaticText();
		mainLayout.add(labelControlName, 0, 0);
		popupSelectControl = PopUpMenu();
		popupSelectControl.toolTip = "Select a control.";
		popupSelectControl.items = ["slider", "range", "steps", "multislider", "multiknob", "buffer selector", "midiout selector" ];
        popupSelectControl.action = { |sender| this.onItemChanged_PopupSelectControl(sender.item); };
        mainLayout.add(popupSelectControl, 1, 0);
        textFieldControlName = TextField();
		textFieldControlName.toolTip = "Sets the parameter name of the control.";
        textFieldControlName.action = { |sender| this.onControlNameChanged_TextField(sender.string.stripWhiteSpace) };
        mainLayout.add(textFieldControlName, 1, 1);



        buttonRemove = ButtonFactory.createInstance(this, class: "btn-delete");
        buttonRemove.action = { if (actionRemove.notNil, { actionRemove.value(this); }); };
        mainLayout.add(buttonRemove, 0, 1, align: \right);

		controlSpecView = SCMControlSpecView();
		controlSpecView.toolTip = "Sets the mapping using a ControlSpec.";
		controlSpecView.action = { |sender| this.onSpecChanged_ControlSpecView(sender); };
	 	mainLayout.addSpanning(controlSpecView, 2, columnSpan: 2);

		prBeginDragAction =  { |view, x, y|
			this // Current instance is the object to drag.
		};

		prCanReceiveDragHandler = {  |view, x, y|
			var result = false;
			if (View.currentDrag.isKindOf(PatternBoxParamControlItemView), {
				result = View.currentDrag.parent === this.parent;
			});
			result;
		};

		prReceiveDragHandler = { |view, x, y|
			if (actionMoveControlItem.notNil, { actionMoveControlItem.value(this, View.currentDrag); });
		};

		this.setDragAndDropBehavior(this);
		this.setDragAndDropBehavior(controlSpecView.textInput);
		this.setDragAndDropBehavior(textFieldControlName);
		this.setDragAndDropBehavior(popupSelectControl);
		this.setDragAndDropBehavior(labelControlName);
	}

	setDragAndDropBehavior { |object|
		object.dragLabel = textFieldControlName.string;
		object.beginDragAction = prBeginDragAction;
		object.canReceiveDragHandler = prCanReceiveDragHandler;
		object.receiveDragHandler = prReceiveDragHandler;
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
		this.setDragAndDropBehavior(this);
		this.setDragAndDropBehavior(controlSpecView);
		this.setDragAndDropBehavior(textFieldControlName);
		this.setDragAndDropBehavior(popupSelectControl);
		this.setDragAndDropBehavior(labelControlName);
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
		if (controlView.notNil, { controlView.dispose.remove; });
		selectedControlType = type;
        case
		{ type == "slider" } { controlView = SCMSliderView(); }
		{ type == "range" } { controlView = SCMRangeSliderView(); }
		{ type == "steps" } {controlView = SCMMultiStepView(); }
		{ type == "multislider" } { controlView = SCMSliderSequencerView(); }
		{ type == "multiknob" } { controlView = SCMKnobSequencerView(); }
		{ type == "buffer selector" } { controlView = bufferpool.createBufferSelectorView(); }
		{ type == "midiout selector" } { controlView = SCMMIDIOutSelectorView(); };

		controlView.name = this.controlName;
		controlView.spec = spec;
		controlView.uiMode(\brief);
		controlView.editMode = this.editMode;
		controlSpecView.visible = controlView.needsControlSpec;
		mainLayout.addSpanning(controlView, 3, 0, columnSpan: 2);
		if (actionControlItemChanged.notNil, { this.actionControlItemChanged.value(this); });

	}

	setMainSequencerPosition { |position|
        if (controlView.notNil, { controlView.setMainSequencerPosition(position); });
    }

	setMainSequencerMode{ |mode|
        if (controlView.notNil, { controlView.setMainSequencerMode(mode); });
    }

    randomize {
        if (controlView.notNil, { controlView.randomize(); });
    }

	toLow {
        if (controlView.notNil, { controlView.toLow(); });
    }

	toHigh {
        if (controlView.notNil, { controlView.toHigh(); });
    }

	toCenter {
        if (controlView.notNil, { controlView.toCenter(); });
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
		this.controlName = state[\textFieldControlName];
		controlSpecView.loadState(state[\controlSpecView]);
		this.onItemChanged_PopupSelectControl(state[\selectedControlType]);
		popupSelectControl.items do: { |item, i| if (item == state[\selectedControlType], { index = i; }); };
		popupSelectControl.value = index;
		controlView.loadState(state[\controlView]);
	}
}

