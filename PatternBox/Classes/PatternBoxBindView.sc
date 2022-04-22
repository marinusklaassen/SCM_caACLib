/*
keyFILENAME: PatternBoxBindView

DESCRIPTION: THe PatternBoxBindView combines keys values pairs into a Pbind, Pmono or PmonoArtic  objects.

AUTHOR: Marinus Klaassen (2022Q4)

EXAMPLE:
s.boot;
PatternBoxProjectView().front;

s.boot;
m = PatternBoxView().front();
a = m.getState()
m.loadState(a);
m.model[\envirText]
a.keys do: { |key| a[key.postln].postln }
*/

PatternBoxBindView : View {
	var paramViews, mainLayout, layoutParams, headerLayout, dragBothPanel, textFieldTitle, buttonAddParamView, buttonDelete;
	var <title, <bindSource, <context;
	var <>actionOnBindChanged, <>actionButtonDelete, <>actionInsertPatternBoxBindView, <>actionMoveBindView;
	var prBeginDragAction, prCanReceiveDragHandler, prReceiveDragHandler; // workaround drag and drop

	*new { |context, parent, bounds|
		^super.new(parent, bounds).initialize(context).initializeView().initialized();
	}

	title_ { |argTitle|
		title = argTitle;
		textFieldTitle.string = title;
		this.toolTip = title;
	}

	editMode_ { |mode|
		paramViews do: { |paramView| paramView.editMode = mode; };
	}

	initialize { |argContext|
		context = argContext;
		paramViews = List();
		bindSource = PatternProxy(1);
		bindSource.source = Pbind();
	}

	initializeView {
		mainLayout = VLayout();

		this.layout = mainLayout;
		this.background = Color.black.alpha_(0.1);
		headerLayout = HLayout();
		headerLayout.margins = [5, 5, 20, 5];

		mainLayout.add(headerLayout);

		dragBothPanel = DragBoth();
		dragBothPanel.maxWidth = 24;
		dragBothPanel.background = Color.black.alpha_(0.5);
		dragBothPanel.toolTip = this.toolTip;
		headerLayout.add(dragBothPanel);

		textFieldTitle = TextFieldFactory.createInstance(this, class: "patternbox-bindview-name");
		textFieldTitle.action = { |sender| this.title = sender.string; };
		headerLayout.add(textFieldTitle);

		buttonAddParamView = ButtonFactory.createInstance(this, class: "btn-add-param");
		buttonAddParamView.toolTip = "Add a new PatternBox parameter";
		buttonAddParamView.action = { this.addParamView(); };
		headerLayout.add(buttonAddParamView);

		buttonDelete = ButtonFactory.createInstance(this, class: "btn-delete");
		buttonDelete.toolTip = "Remove this patter bind view.";
		buttonDelete.action = {
			if (actionButtonDelete.notNil, { actionButtonDelete.value(this) });
		};
		headerLayout.add(buttonDelete);

		layoutParams = VLayout();
		layoutParams.margins = 0!4;
		mainLayout.add(layoutParams);

		// Start drag & drop workaround
		prBeginDragAction =  { |view, x, y|
			this; // Current instance is the object to drag.
		};

		prCanReceiveDragHandler = {  |view, x, y|
			View.currentDrag.isKindOf(PatternBoxBindView);
		};

		prReceiveDragHandler = { |view, x, y|
			if (actionMoveBindView.notNil, { actionMoveBindView.value(this, View.currentDrag); });
		};

		this.setDragAndDropBehavior(this);
		this.setDragAndDropBehavior(dragBothPanel);
		this.setDragAndDropBehavior(textFieldTitle);
	}

	initialized {
	}

	setDragAndDropBehavior { |object|
		object.dragLabel = "A PatternBox parameter.";
		object.beginDragAction = prBeginDragAction;
		object.canReceiveDragHandler = prCanReceiveDragHandler;
		object.receiveDragHandler = prReceiveDragHandler;
	}


	addParamView { |positionInLayout|
		var paramChannel = PatternBoxParamView(this);

		paramChannel.actionNameChanged = { |sender|
			this.rebuildPatterns(sender);
		};

		paramChannel.actionButtonDelete = { | sender|
			paramViews.remove(sender);
			sender.remove(); // Remove itself from the layout.
			this.rebuildPatterns(sender);
		};

		paramChannel.actionInsertPatternBox = { |sender, insertType|
			var positionInLayout = paramViews.indexOf(sender);
			if (insertType == "INSERT_AFTER", {
				positionInLayout = positionInLayout + 1;
			});
			this.addParamView(positionInLayout);
		};

		paramChannel.actionMoveParamView = { |dragDestinationObject, dragObject|
			var targetPosition;
			if (dragDestinationObject !==  dragObject, {
				targetPosition = paramViews.indexOf(dragDestinationObject);
				paramViews.remove(dragObject);
				paramViews.insert(targetPosition, dragObject);
				layoutParams.insert(dragObject, targetPosition);
			});
		};

		if (positionInLayout.notNil, {
			layoutParams.insert(paramChannel, positionInLayout);
			paramViews = paramViews.insert(positionInLayout, paramChannel);
		},{
			layoutParams.insert(paramChannel, paramViews.size);
			paramViews = paramViews.add(paramChannel);
		});

		^paramChannel;
	}

	rebuildPatterns {
		var keyValuePairPatterns = List();
		paramViews do: { |paramView |
			keyValuePairPatterns.add(paramView.keyName.asSymbol);
			keyValuePairPatterns.add(paramView.paramProxy;);
		};
		if (keyValuePairPatterns.size == 0, {
			bindSource.source = Pbind();
		},
		{
			bindSource.source = Pbind(*keyValuePairPatterns.asArray());
		});
	}

	randomize {
		paramViews do: { |patternBoxParamView|
			patternBoxParamView.randomize();
		}
	}

	getState { |skipProjectStuf|
		var state = Dictionary();
		state[\type] = "PatternBoxBindView";
		state[\title] = title;
		state[\paramViewStates] = paramViews collect: { | paramView |
			paramView.getState();
		};
		^state;
	}

	loadState { |state|
		this.title = state[\title];
		// Remove the scores that are to many.
		if (state[\paramViewStates].size < paramViews.size, {
			var amountToMany = paramViews.size - state[\paramViewStates].size;
			amountToMany do: {
				paramViews.pop().dispose()
			};
		});
		state[\paramViewStates] do: { |paramViewState, position|
			var newParamView;
			if (paramViews[position].isNil, {
				newParamView = this.addParamView();
			}, {
				newParamView = paramViews[position];
			});
			newParamView.loadState(paramViewState);
		};
	}

	dispose {
		paramViews do: { |paramView| paramView.dispose(); };
		this.remove();
	}
}