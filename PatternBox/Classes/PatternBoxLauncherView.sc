/*
FILENAME: PatternBoxLauncherView

DESCRIPTION: Maintain and launcher of PatternBox instances.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
s.boot;
PatternBoxLauncherView(bounds:400@700).front();
*/

PatternBoxLauncherView : View {
	var <patternBoxLauncherItemViews, <eventAddPatternBox;
	var mainLayout, footerLayout, <>bufferpool, toggleMIDIedit, projectSaveAndLoadView, menuFile, layoutPatternBoxItems, scrollViewPatternBoxItems, buttonAddPatternBox, layoutHeader, serverControlView, tempoClockView;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	initialize {
		MIDIClient.init;
		MIDIIn.connectAll;
		patternBoxLauncherItemViews = List();
		this.initializeEvents();
		this.initializeView();
		this.registerEventHandlers();
	    projectSaveAndLoadView.autoLoad();
	}

	initializeView {
		this.name = "PatternBox Launcher";
		this.deleteOnClose = false;

		mainLayout = VLayout();
		this.layout = mainLayout;

		bufferpool = SCMBufferPoolView(bounds: 700@700);

		projectSaveAndLoadView = SCMProjectPersistanceView(contextID: "PatternBoxLauncherView");
		projectSaveAndLoadView.layout.margins = 0!4;
		projectSaveAndLoadView.actionChanged = { |sender| this.name = "PatternBox Launcher - " ++ PathName(sender.projectfile).fileName; };
		projectSaveAndLoadView.actionClearAll = { this.clearAll(); };
		projectSaveAndLoadView.actionNewItem = {
			this.invokeEvent(this.eventAddPatternBox);
		};

		projectSaveAndLoadView.actionCloseAllViews = { this.closeViews(); };

		projectSaveAndLoadView.actionMidiEditingStateChanged = { |sender|
			patternBoxLauncherItemViews do: { |view| view.editMIDI(sender.value); }
		};

		projectSaveAndLoadView.addView(Button().string_("Bufferpool").action_({ bufferpool.front; }));

		mainLayout.add(projectSaveAndLoadView);

		serverControlView = SCMServerControlView(this);
		serverControlView.layout.margins = 0!4;
		mainLayout.add(serverControlView);

		layoutPatternBoxItems = VLayout([nil, stretch:1, align: \bottom]); // workaround. insert before stretchable space.
		layoutPatternBoxItems.margins = 0!4;
		layoutPatternBoxItems.spacing = 5;

		scrollViewPatternBoxItems = ScrollViewFactory.createInstance(this);
		scrollViewPatternBoxItems.canvas.layout = layoutPatternBoxItems;
		scrollViewPatternBoxItems.background = Color.black.alpha_(0.1);

		mainLayout.add(scrollViewPatternBoxItems);

		footerLayout = HLayout();
		footerLayout.margins = 0!4;

		tempoClockView = SCMTempoClockView(this);
		footerLayout.add(tempoClockView,  align: \left);

		buttonAddPatternBox = ButtonFactory.createInstance(this, "btn-add");
		buttonAddPatternBox.toolTip = "Add a new PatternBox.";
		buttonAddPatternBox.action = { this.invokeEvent(this.eventAddPatternBox); };
		footerLayout.add(buttonAddPatternBox,  align: \right);

		mainLayout.add(footerLayout);
	}

	invokeEvent { |event|
		event.changed(this);
	}

	initializeEvents {
		eventAddPatternBox = ();
	}

	registerEventHandlers {
		eventAddPatternBox.addDependant({
			this.addPatternBox();
		});
		projectSaveAndLoadView.eventLoadProject.addDependant({  | event, sender /* projectSaveAndLoadView */ |
			this.clearAll();
			this.loadState(sender.data);
		});
		projectSaveAndLoadView.eventSaveProject.addDependant({  | event, sender /* projectSaveAndLoadView */ |
			sender.data = this.getState();
		});
	}

	addPatternBox { |positionInLayout, sourcePatterBoxItemView, duplicate|
		var patternBoxLauncherItemView = PatternBoxLauncherItemView(bufferpool: bufferpool);
		if(duplicate == true, {
			var state = sourcePatterBoxItemView.getState();
			state[\patternBoxName] = state[\patternBoxName] + " - COPY";
			patternBoxLauncherItemView.loadState(state);
		});

		patternBoxLauncherItemView.actionRemove = { | sender |
			patternBoxLauncherItemViews.remove(patternBoxLauncherItemView);
		};

		patternBoxLauncherItemView.actionInsertPatternBox = { |sender, insertType|
			var positionInLayout = patternBoxLauncherItemViews.indexOf(sender);
			var duplicate = false;
			if (insertType == "INSERT_AFTER", {
				positionInLayout = positionInLayout + 1;
			});
			if (insertType == "INSERT_AFTER_DUPLICATIE", {
				positionInLayout = positionInLayout + 1;
				duplicate = true;
			});
			this.addPatternBox(positionInLayout, sender, duplicate);
		};

		patternBoxLauncherItemView.actionMovePatternBox = { |dragDestinationObject, dragObject|
			var targetPosition;
			if (dragDestinationObject !==  dragObject, {
				targetPosition = patternBoxLauncherItemViews.indexOf(dragDestinationObject);
				patternBoxLauncherItemViews.remove(dragObject);
				patternBoxLauncherItemViews.insert(targetPosition, dragObject);
				layoutPatternBoxItems.insert(dragObject, targetPosition);
			});
		};

		if (positionInLayout.notNil, {
			layoutPatternBoxItems.insert(patternBoxLauncherItemView, positionInLayout);
			patternBoxLauncherItemViews.insert(positionInLayout, patternBoxLauncherItemView);
		},{
			layoutPatternBoxItems.insert(patternBoxLauncherItemView, patternBoxLauncherItemViews.size); // workaround. insert before stretchable space.
			patternBoxLauncherItemViews.add(patternBoxLauncherItemView);
		});
		^patternBoxLauncherItemView;
	}

	clearAll {
		patternBoxLauncherItemViews.copy do: { | patternBox| patternBox.dispose(); };
	}

	closeViews {
		patternBoxLauncherItemViews do: { |view| view.closeView(); };
	}

	getState {
		var state = Dictionary();
		state[\type] = "PatternBoxLauncherView";
		state[\patternBoxLauncherItemViewsStates] = patternBoxLauncherItemViews.collect({ |patternBoxLauncherItemView| patternBoxLauncherItemView.getState(); });
		state[\patternBoxBufferpool] = bufferpool.getState();
		^state;
	}

	loadState{ |state|
		var patternBoxLauncherItemView;
		if (state.isKindOf(Dictionary),
			{
				if (state[\patternBoxBufferpool].notNil, { bufferpool.loadState(state[\patternBoxBufferpool]); });
				// Remove the patternBoxViews that are to many.
				if (state[\patternBoxLauncherItemViewsStates].size < patternBoxLauncherItemViews.size, {
					var amountToMany = patternBoxLauncherItemViews.size - state[\patternBoxLauncherItemViewsStates].size;
					amountToMany do: {
						patternBoxLauncherItemViews.pop().dispose();
					};
				});
				// Reuse existing patternBoxViews or add a new PatternBox.
				state[\patternBoxLauncherItemViewsStates] do: { |state, position|
					if (patternBoxLauncherItemViews[position].isNil, {
						patternBoxLauncherItemView = this.addPatternBox();
					}, {
						patternBoxLauncherItemView = patternBoxLauncherItemViews[position];
					});
					patternBoxLauncherItemView.loadState(state);
				};
		});
	}
}
