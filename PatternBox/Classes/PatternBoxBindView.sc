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
	var <>bufferpool, <paramViews, mainLayout, bodyLayout, bodyContainer, headerLayout, headerView, buttonCollapsable, buttonRandomize, dragBothPanel, textFieldTitle, numberBoxParallelLayers, buttonAddParamView, buttonDelete;
	var <title, <bindSource, <context, <muteState, <parallelLayers, collapsableState;
	var <>actionOnBindChanged, <>actionRestartPatterns, <>actionInsertBindView, <>actionButtonDelete, currentPattern, toggleMute, toggleSolo, <soloState, <>actionInsertPatternBoxBindView, <>actionMoveBindView;
	var availablePatternModes, prBeginDragAction, patternMode, popupMenuPatternMode, prCanReceiveDragHandler, prReceiveDragHandler, <>actionOnSoloStateChanged; // workaround drag and drop

	*new { |context, bufferpool, parent, bounds|
		^super.new(parent, bounds).initialize(context, bufferpool).initializeView().initialized();
	}

	title_ { |argTitle|
		title = argTitle;
		textFieldTitle.string = title;
		this.toolTip = title;
	}

	// rename to setSoloStateAction?
	setSoloState { |state, skipAction|
		soloState = state;
		toggleSolo.value = state;
		if (soloState == 1, {
			this.unmute();
		});
		if (skipAction.isNil, {
			if (actionOnSoloStateChanged.notNil, { actionOnSoloStateChanged.value(this); });
		});
	}

	setPatternMode { |mode|
		if (patternMode != mode, {
			patternMode = mode;
			popupMenuPatternMode.value = availablePatternModes.indexOf(mode);
			this.rebuildPatterns();
		});
	}

	setMuteState { |state, skipAction|
		muteState = state;
		toggleMute.value = state;
		if (muteState == 1, {
			// if (skipSoloState.isNil, { this.setSoloState(0); });
			bindSource.source = Pset(\type, \rest, currentPattern);
		},{
			bindSource.source = currentPattern;
		});
	}

	setBodyIsVisible { |visible|
		bodyContainer.visible = visible;
		collapsableState = visible;
		buttonCollapsable.value = if(visible, 1, 0);
	}

	mute {
		this.setMuteState(1);
	}

	unmute {
		this.setMuteState(0);
	}

	editMode_ { |mode|
		paramViews do: { |paramView| paramView.editMode = mode; };
	}

	parallelLayers_ { |layers|
		parallelLayers = layers;
		numberBoxParallelLayers.value = layers;
		this.rebuildPatterns();
	}

	initialize { |argContext, bufferpool|
		this.bufferpool = bufferpool;
		context = argContext;
		soloState = 0;
		muteState = 0;
		collapsableState = false;
		parallelLayers = 1;
		paramViews = List();
		currentPattern = Pbind();
		patternMode = \Pbind;
		availablePatternModes =  [\Pbind, \Pmono];
		bindSource = PatternProxy();
		bindSource.source = currentPattern;
		bindSource.asStream.next();
	}

	initializeView {

		this.setContextMenuActions(
			MenuAction("Insert new binding item above", {
				if (actionInsertBindView.notNil, { actionInsertBindView.value(this, "INSERT_BEFORE"); });
			}),
			MenuAction("Insert new binding item below", {
				if (actionInsertBindView.notNil, { actionInsertBindView.value(this, "INSERT_AFTER"); });
			}),
			MenuAction("Duplicate this binding item", {
				if (actionInsertBindView.notNil, { actionInsertBindView.value(this, "INSERT_AFTER_DUPLICATIE"); });
			}),
			MenuAction("Remove this binding item", {
				if (actionButtonDelete.notNil, { actionButtonDelete.value(this) });
			})
		);

		mainLayout = VLayout();
		mainLayout.margins = [0, 0, 0, 10];
		mainLayout.spacing = 2;
		this.layout = mainLayout;
		this.background = Color.black.alpha_(0.1);
		headerView = View();
		headerView.background = Color(0.45490196078431, 0.55686274509804, 0.87843137254902);
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

		buttonCollapsable = Button();
		buttonCollapsable.toolTip = "Expand or collapse all param views";
		buttonCollapsable.maxWidth = 24;
		buttonCollapsable.states = [["▲", nil, Color.clear.alpha_(0)], ["▼", nil, Color.clear.alpha_(0)]];
		buttonCollapsable.action = { | sender |
			this.setBodyIsVisible(if(sender.value == 1, true, false));
		};
		headerLayout.add(buttonCollapsable);

		popupMenuPatternMode = PopUpMenuFactory.createInstance(this);
		popupMenuPatternMode.items = availablePatternModes;
		popupMenuPatternMode.value = 0;
		popupMenuPatternMode.action = { | sender |
			this.setPatternMode(sender.item);
		};

		headerLayout.add(popupMenuPatternMode);

		buttonRandomize = Button();
		buttonRandomize.toolTip = "Randomize";
		buttonRandomize.maxWidth = 24;
		buttonRandomize.states = [[""] ++ Color.black.dup(2)];
		buttonRandomize.action = {
			this.randomize();
		};

		headerLayout.add(buttonRandomize);

		toggleSolo = Button();
		toggleSolo.toolTip = "Solo this bind view";
		toggleSolo.maxWidth = 24;
		toggleSolo.states = [["S", Color.black, Color.black.alpha_(0.1)], ["S", Color.black, Color.yellow]];
		toggleSolo.action = { |sender|
			this.setSoloState(sender.value);
		};

		headerLayout.add(toggleSolo);

		toggleMute = Button();
		toggleMute.toolTip = "Mute";
		toggleMute.maxWidth = 24;
		toggleMute.states = [["M", Color.black, Color.black.alpha_(0.1)], ["M", Color.black, Color.red]];
		toggleMute.action = { |sender|
			this.setMuteState(sender.value);
		};

		headerLayout.add(toggleMute);

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

		bodyContainer = View();
		bodyContainer.layout = bodyLayout;

		mainLayout.add(bodyContainer);

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
		this.setDragAndDropBehavior(buttonRandomize);
		this.setDragAndDropBehavior(toggleMute);
		this.setDragAndDropBehavior(toggleSolo);
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
		var paramChannel = PatternBoxParamView(this, bufferpool: this.bufferpool);

		paramChannel.actionNameChanged = { |sender|
			this.rebuildPatterns(sender);
		};

		paramChannel.actionPatternScriptChanged = { |sender|
			this.rebuildPatterns();
		};

		paramChannel.actionButtonDelete = { | sender|
			paramViews.remove(sender);
			sender.remove(); // Remove itself from the layout.
			this.rebuildPatterns(sender);
		};

		paramChannel.actionInsertPatternBox = { |sender, insertType|
			var positionInLayout = paramViews.indexOf(sender), newParam;
			if ((insertType == "INSERT_AFTER") || (insertType == "DUPLICATE_PARAM"), {
				positionInLayout = positionInLayout + 1;
			});

			newParam = this.addParamView(positionInLayout);

			if (insertType == "DUPLICATE_PARAM", {
				newParam.loadState(sender.getState());
			});
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
						targetPosition = targetPosition + 1;
					    dragObject.context.rebuildPatterns();
						dragObject.context = this;
					});
					paramViews.insert(targetPosition, dragObject);
					bodyLayout.insert(dragObject, targetPosition);
				    this.rebuildPatterns();
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

		this.setBodyIsVisible(true);

		^paramChannel;
	}

	rebuildPatterns {
		var keyValuePairPatterns = Dictionary();
		var pbindPairsList = List();
		var newPbind;
		paramViews do: { |paramView|
			if (paramView.isPbind == true, {
				if (paramView.pbind.patternpairs.notEmpty(),
					{
						pbindPairsList.add(paramView.pbind.patternpairs);
				});
			},{
				if (paramView.keyName.notEmpty && paramView.scriptFieldView.string.stripWhiteSpace().notEmpty, {
					keyValuePairPatterns[paramView.keyName.asSymbol] = paramView.paramProxy;
				});
			});
		};
		if (keyValuePairPatterns.size == 0, {
			switch (patternMode,
				\Pbind, { currentPattern = Pbind(); },
				\Pmono, { currentPattern = Pmono(\default); });
		},
		{
			switch (patternMode,
				\Pbind, { currentPattern = Pbind(*keyValuePairPatterns.asKeyValuePairs().asArray()); },
				\Pmono, {
					var arrayKeyValues, instrumentName = \default;
					if (keyValuePairPatterns[\instrument].notNil, {
						instrumentName = keyValuePairPatterns[\instrument].asStream.next;
						keyValuePairPatterns[\instrument] = nil;
					});
					arrayKeyValues = keyValuePairPatterns.asKeyValuePairs().insert(0, instrumentName);
					currentPattern = Pmono(*arrayKeyValues);
			});
		});
		pbindPairsList do: { |patternpairs|
			currentPattern = Pbindf(*asArray([currentPattern] ++ patternpairs));
		};
		if (parallelLayers > 1, {
			currentPattern = Ppar({currentPattern}!parallelLayers);
		});
		this.setMuteState(muteState);
		bindSource.source = currentPattern;
		if (actionRestartPatterns.notNil, { actionRestartPatterns.value(this); });
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
		state[\muteState] = muteState;
		state[\soloState] = soloState;
		state[\patternMode] = patternMode;
		state[\collapsableState] = collapsableState;
		^state;
	}

	loadState { |state|
		this.title = state[\title];
		this.parallelLayers = if (state[\parallelLayers].isNil, 1, state[\parallelLayers]);
		// Remove the scores that are to many.
		this.setPatternMode(if(state[\patternMode].isNil, \Pbind, state[\patternMode]));
		if (state[\muteState].notNil, {
			this.setMuteState(state[\muteState], skipAction: true);
		},{
			this.setMuteState(0, skipAction: true);
		});
		if (state[\soloState].notNil, {
			this.setSoloState(state[\soloState], skipAction: true);
		},{
			this.setSoloState(0, skipAction: true);
		});
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
		if (state[\collapsableState].notNil, {
			this.setBodyIsVisible(state[\collapsableState]);
		},{
			this.setBodyIsVisible(true);
		});
	}

	dispose {
		paramViews do: { |paramView| paramView.dispose(); };
		this.remove();
	}
}

