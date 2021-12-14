/*
FILENAME: PatternBoxParamControlGroupView

DESCRIPTION: Select an compose widgest.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

a = PatternBoxParamControlGroupView().front;
a.bounds
*/

PatternBoxParamControlGroupView : View {

	var mainLayout, buttonAdd, controlItems, <editMode, <>controlNameDefault, <>actionRemove;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	initialize {
		controlNameDefault = "default";
		controlItems = List();
		this.initializeView();
	}

	initializeView {
		mainLayout = VLayout();
		this.layout = mainLayout;
		buttonAdd = Button().string_("add widget").action_({ this.onButtonClick_AddPatternBoxParamControlItemView() });
		buttonAdd.visible = false;
		editMode = false;
		mainLayout.add(buttonAdd, align: \right);
	}

	editMode_ { |mode|
		buttonAdd.visible  = mode;
		controlItems do: { |item| item.editMode = mode; };
		editMode = mode;
	}

	onButtonClick_AddPatternBoxParamControlItemView { |state|
		var controlItem = PatternBoxParamControlItemView();
		controlItem.controlName = this.controlNameDefault;
		controlItem.actionRemove = { |sender|
			controlItems.remove(sender);
			controlItem.remove();
			if (actionRemove.notNil, { actionRemove.value(this); });
		};
		controlItems.add(controlItem);
		if (state.notNil, { controlItem.loadState(state) });
		mainLayout.insert(controlItem, controlItems.size - 1);

	}

	randomize {
		controlItems do: { |controlItem| controlItem.randomize(); };
	}

	getState {
		^controlItems collect: { |item| item.getState(); };
	}

	loadState { |state|
		controlItems do: { |item| item.remove(); };
		controlItems = List();
		state do: { |itemState|
			onButtonClick_AddPatternBoxParamControlItemView(itemState);
		}
	}
}

