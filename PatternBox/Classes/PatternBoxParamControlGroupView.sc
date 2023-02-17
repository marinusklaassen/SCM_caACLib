/*
FILENAME: PatternBoxParamControlGroupView

DESCRIPTION: Select an compose widgest.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

a = PatternBoxParamControlGroupView().front;
a.bounds
*/

PatternBoxParamControlGroupView : View {

	var <>bufferpool, mainLayout, buttonAdd, <controlItems, <editMode, <>controlNameDefault, <>actionControlCRUD;

	*new { |bufferpool, parent, bounds|
		^super.new(parent, bounds).initialize(bufferpool);
	}

	initialize { |bufferpool|
		this.bufferpool = bufferpool;
		controlNameDefault = "default";
		controlItems = List();
		this.initializeView();
	}

	initializeView {
		mainLayout = VLayout();
		this.layout = mainLayout;
		buttonAdd = ButtonFactory.createInstance(this, class: "btn-add");
		buttonAdd.fixedSize_(16);
		buttonAdd.toolTip = "Add a control";
		buttonAdd.action_({ this.onButtonClick_AddPatternBoxParamControlItemView() });
		buttonAdd.visible = false;
		editMode = false;
		mainLayout.add(buttonAdd, align: \right);
	}

	editMode_ { |mode|
		buttonAdd.visible  = mode;
		controlItems do: { |item| item.editMode = mode; };
		editMode = mode;
	}

	onButtonClick_AddPatternBoxParamControlItemView { |state, positionInLayout|
		var controlItem = PatternBoxParamControlItemView(name: "control" ++ (controlItems.size + 1), bufferpool: bufferpool);
		controlItem.actionRemove = { |sender|
			controlItems.remove(sender);
			controlItem.remove();
			if (actionControlCRUD.notNil, { actionControlCRUD.value(this); });
		};
		controlItem.actionControlItemChanged = {
			if (actionControlCRUD.notNil, { actionControlCRUD.value(this); });
		};
		controlItem.actionControlNameChanged = {
			if (actionControlCRUD.notNil, { actionControlCRUD.value(this); });
		};
		controlItem.actionMoveControlItem = { |dragDestinationObject, dragObject|
			var targetPosition;
			if (dragDestinationObject !==  dragObject, {
				targetPosition = controlItems.indexOf(dragDestinationObject);
				controlItems.remove(dragObject);
				controlItems.insert(targetPosition, dragObject);
				mainLayout.insert(dragObject, targetPosition);
			});
		};
		controlItem.actionInsertControlItem = { |sender, insertType|
			var positionInLayout = controlItems.indexOf(sender);
			var stateToDuplicate = nil;
			if (insertType == "INSERT_AFTER", {
				positionInLayout = positionInLayout + 1;
			});
			if (insertType == "DUPLICATE", {
				positionInLayout = positionInLayout + 1;
				stateToDuplicate = sender.getState();
				stateToDuplicate[\textFieldControlName] = stateToDuplicate[\textFieldControlName] ++ "Copy";
			});
			this.onButtonClick_AddPatternBoxParamControlItemView(stateToDuplicate, positionInLayout);
		};

		if (state.notNil, { controlItem.loadState(state) });
		controlItem.editMode = editMode;
		if (positionInLayout.notNil, {
			mainLayout.insert(controlItem, positionInLayout);
			controlItems.insert(positionInLayout, controlItem);
		},{
			controlItems.add(controlItem);
			mainLayout.insert(controlItem, controlItems.size - 1);
		});
		if (actionControlCRUD.notNil, { actionControlCRUD.value(this); });
	}

	randomize {
		controlItems do: { |controlItem| controlItem.randomize(); };
	}

	getProxies {
		var result = Dictionary();
		controlItems do: { |item| result.putAll(item.getProxies()); };
		^result;
	}

	getState {
		var state = Dictionary();
		state[\visible] = this.visible;
		state[\editMode] = editMode;
		state[\controlItems] = controlItems collect: { |item| item.getState(); };
			state[\controlItems];
		^state;
	}

	loadState { |state|
		controlItems do: { |item| item.remove; };
		controlItems.clear;
		if (state.notNil, {
		if(state[\editMode].notNil, { this.editMode = state[\editMode]; });
		if(state[\visible].notNil, { this.visible = state[\visible]; });
		state[\controlItems] do: { |itemState|
			this.onButtonClick_AddPatternBoxParamControlItemView(itemState);
		};
		}, {  this.editMode  = false; });
	}
}


