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
	var <paramViews, mainLayout, bodyLayout, headerLayout, headerView, buttonRandomize, dragBothPanel, textFieldTitle, numberBoxParallelLayers, buttonAddParamView, buttonDelete;
	var <title, <bindSource, <context, <parallelLayers;
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

	parallelLayers_ { |layers|
		parallelLayers = layers;
		numberBoxParallelLayers.value = layers;
		this.rebuildPatterns();
	}

	initialize { |argContext|
		context = argContext;
		parallelLayers = 1;
		paramViews = List();
		bindSource = PatternProxy(1);
		bindSource.source = Pbind();
	}

	initializeView {
		mainLayout = VLayout();
		mainLayout.margins = [0, 0, 0, 10];
		mainLayout.spacing = 2;
		this.layout = mainLayout;
		this.background = Color.black.alpha_(0.1);
		headerView = View();
		headerView.background = Color.blue.alpha_(0.2);
		headerLayout = HLayout();
		headerLayout.margins = [5, 10, 20, 10];
		headerView.layout = headerLayout;
		mainLayout.add(headerView);

		dragBothPanel = DragBoth();
		dragBothPanel.maxWidth = 24;
		dragBothPanel.background = Color.black.alpha_(0.5);
		dragBothPanel.toolTip = this.toolTip;
		headerLayout.add(dragBothPanel);

		textFieldTitle = TextFieldFactory.createInstance(this, class: "patternbox-bindview-name");
		textFieldTitle.action = { |sender| this.title = sender.string; };
		textFieldTitle.keyUpAction = textFieldTitle.action;
		headerLayout.add(textFieldTitle);

		buttonRandomize = Button();
		buttonRandomize.toolTip = "Randomize";
		buttonRandomize.maxWidth = 24;
		buttonRandomize.states = [[""] ++ Color.black.dup(2)];
		buttonRandomize.action = {
			this.randomize();

		};
		headerLayout.add(buttonRandomize);

		numberBoxParallelLayers = NumberBoxFactory.createInstance(this, class: "numberbox-patternbox-layers");
		numberBoxParallelLayers.value = parallelLayers;
		numberBoxParallelLayers.toolTip = "Amount of parallel pattern streams";
		numberBoxParallelLayers.action = { | sender | this.parallelLayers = sender.value; };
		headerLayout.add(numberBoxParallelLayers);

		buttonAddParamView = ButtonFactory.createInstance(this, class: "btn-add-param");
		buttonAddParamView.toolTip = "Add a new PatternBox parameter";
		buttonAddParamView.action = { this.addParamView(); };
		headerLayout.add(buttonAddParamView);

		buttonDelete = ButtonFactory.createInstance(this, class: "btn-delete-group");
		buttonDelete.toolTip = "Remove this patter bind view.";
		buttonDelete.action = {
			if (actionButtonDelete.notNil, { actionButtonDelete.value(this) });
		};
		headerLayout.add(buttonDelete);

		bodyLayout = VLayout();
		bodyLayout.spacing = 2;
		bodyLayout.margins = [10,0,0,0];
		mainLayout.add(bodyLayout);

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
		this.setDragAndDropBehavior(numberBoxParallelLayers);
		this.setDragAndDropBehavior(buttonAddParamView);
		this.setDragAndDropBehavior(buttonDelete);
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
				if (dragDestinationObject.context != dragObject.context, {
						dragObject.context.paramViews.remove(dragObject);
				});
				paramViews do: { |view, i|
					if (dragDestinationObject == view, {
						targetPosition = i;
					});
				};
				if( targetPosition.notNil, {
					if (dragDestinationObject.context == dragObject.context, {
						paramViews.remove(dragObject);
					}, {
						targetPosition = targetPosition + 1; // testje
					});
					dragObject.context = this;
					paramViews.insert(targetPosition, dragObject);
					bodyLayout.insert(dragObject, targetPosition);
				});
			});
		};

		if (positionInLayout.notNil, {
			bodyLayout.insert(paramChannel, positionInLayout);
			paramViews = paramViews.insert(positionInLayout, paramChannel);
		},{
			bodyLayout.insert(paramChannel, paramViews.size);
			paramViews = paramViews.add(paramChannel);
		});

		^paramChannel;
	}

	rebuildPatterns {
		var keyValuePairPatterns = List();
		var newPbind;
		paramViews do: { |paramView |
			keyValuePairPatterns.add(paramView.keyName.asSymbol);
			keyValuePairPatterns.add(paramView.paramProxy;);
		};
		if (keyValuePairPatterns.size == 0, {
			bindSource.source = Pbind();
		},
		{
			newPbind = Pbind(*keyValuePairPatterns.asArray());
			if (parallelLayers > 1, {
				bindSource.source = Ppar({newPbind}!parallelLayers);
			},{
				bindSource.source = newPbind;
			});
		});
	}

	compileAll {
		paramViews do: { |view| view.regenerateAndInterpretedParamScript(); };
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
		state[\parallelLayers] = parallelLayers;
		state[\paramViewStates] = paramViews collect: { | paramView |
			paramView.getState();
		};
		^state;
	}

	loadState { |state|
		this.title = state[\title];
		this.parallelLayers = if (state[\parallelLayers].isNil, 1, state[\parallelLayers]);
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

