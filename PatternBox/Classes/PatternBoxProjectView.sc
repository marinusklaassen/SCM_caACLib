/*
FILENAME: PatternBoxProjectView

DESCRIPTION: Maintains a project of PatternBox instances.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
s.boot;
PatternBoxProjectView(bounds:400@700).front();
*/

PatternBoxProjectView : View {
	var <patternBoxViews, lemurClient, <eventAddPatternBox;
	var mainLayout, projectSaveAndLoadView, layoutMixerChannels, scrollViewMixerChannels, layoutHeader, buttonAddPatternBox, buttonPanic, buttonServerNodes, buttonServerMeter, buttonServerBoot;

	*new { |parent, bounds, lemurClient|
		^super.new(parent, bounds).initialize(lemurClient);
	}

	initialize { |lemurClient|
		patternBoxViews = List();
		lemurClient = lemurClient;
		this.initializeEvents();
		this.initializeView();
		this.registerEventHandlers();
	}

	initializeView {
		this.name = "PatternBox Project";
		this.background = Color.new255(136, 172, 224); // light petrol blue
		this.deleteOnClose = false;

		mainLayout = VLayout();
		this.layout = mainLayout;

		projectSaveAndLoadView = ProjectSaveAndLoadView();
		projectSaveAndLoadView.mainLayout.margins = 0!4;
		mainLayout.add(projectSaveAndLoadView);

		layoutHeader = HLayout();
		layoutHeader.margins = 0!4;
		mainLayout.add(layoutHeader);

		buttonPanic = Button();
	    buttonPanic.font = Font("Menlo", 14);
        buttonPanic.states = [["PANIC", Color.red, Color.black]];
		buttonPanic.minHeight = 40;
		buttonPanic.action = {
			CmdPeriod.run;
            Server.freeAll(evenRemote: false);
		};
		layoutHeader.add(buttonPanic);

		buttonServerNodes = Button();
	    buttonServerNodes.font = Font("Menlo", 14);
        buttonServerNodes.states = [["SERVER\nNODES", Color.red, Color.black]];
		buttonServerNodes.minHeight = 40;
		buttonServerNodes.action = {
			Server.default.plotTree();
		};
		layoutHeader.add(buttonServerNodes);

		buttonServerMeter = Button();
	    buttonServerMeter.font = Font("Menlo", 14);
        buttonServerMeter.states = [["SERVER\nMETER", Color.red, Color.black]];
		buttonServerMeter.minHeight = 40;
		buttonServerMeter.action = {
			Server.default.meter();
		};
		layoutHeader.add(buttonServerMeter);

		buttonServerBoot = Button();
	    buttonServerBoot.font = Font("Menlo", 14);
        buttonServerBoot.states = [["SERVER\nBOOT", Color.red, Color.black]];
		buttonServerBoot.minHeight = 40;
		buttonServerBoot.action = {
			Server.default.reboot();
		};
		layoutHeader.add(buttonServerBoot);

		layoutMixerChannels = VLayout([nil, stretch:1, align: \bottom]); // workaround. insert before stretchable space.
		layoutMixerChannels.margins = 0!4;
		layoutMixerChannels.spacing = 2;

		scrollViewMixerChannels = ScrollView();
		scrollViewMixerChannels.canvas = View();
		scrollViewMixerChannels.canvas.layout = layoutMixerChannels;
		scrollViewMixerChannels.canvas.background = this.background;
		mainLayout.add(scrollViewMixerChannels);

		buttonAddPatternBox = PlusButton();
		buttonAddPatternBox.fixedHeight = 30;
		buttonAddPatternBox.fixedWidth = 70;
		buttonAddPatternBox.action = { this.invokeEvent(this.eventAddPatternBox); };
		mainLayout.add(buttonAddPatternBox,  align: \bottomRight);
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
			this.loadState(sender.data);
		});
		projectSaveAndLoadView.eventSaveProject.addDependant({  | event, sender /* projectSaveAndLoadView */ |
			sender.data = this.getState();
		});
	}

	addPatternBox {
		var patternBoxView = PatternBoxView(lemurClient);
		var patternBoxProjectEntryView = patternBoxView.getMixerChannelControl();
		patternBoxView.removeAction = { | sender |
			patternBoxProjectEntryView.remove();
			patternBoxView.dispose();
			patternBoxViews.remove(patternBoxView);
		};
		layoutMixerChannels.insert(patternBoxProjectEntryView, patternBoxViews.size); // workaround. insert before stretchable space.
		patternBoxViews.add(patternBoxView);
		^patternBoxView;
	}

	getState {
		var state = Dictionary();
		state[\type] = "PatternBoxProjectView";
		state[\patternBoxStates] = patternBoxViews.collect({ |patternBoxView| patternBoxView.getState(); });
		^state;
	}

	loadState{ |state|
		var patternBoxView;
		if (state.isKindOf(Dictionary) && state[\type] == "PatternBoxProjectView",
			{
				// Remove the patternBoxViews that are to many.
				if (state[\patternBoxStates].size	< patternBoxViews.size, {
					var amountToMany = patternBoxViews.size - state[\patternBoxStates].size;
					amountToMany do: {
						patternBoxViews.pop().removeAction.value();
					};
				});
				// Reuse existing patternBoxViews or add a new PatternBox.
				state[\patternBoxStates] do: { |state, position|
					if (patternBoxViews[position].isNil, {
						patternBoxView = this.addPatternBox();
					}, {
						patternBoxView = patternBoxViews[position];
					});
					patternBoxView.loadState(state);
				};
		});
	}
}
