/*
FILENAME: PatternBoxProjectView

DESCRIPTION:
- PatternBoxProjectView lists all the scores, project save en read capabilities via its view.
- Main entry point.
- Simple maintainance tasks like add, delete, open score view, volume control and play sections.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
s.boot;
m = PatternBoxProjectView(bounds:400@700).front();
a = m.getState()
m.loadState(a);
*/

PatternBoxProjectView : View {
	var <scores, lemurClient, mainLayout, projectSaveAndLoadView, layoutMixerChannels, scrollViewMixerChannels, buttonAddScore, <eventAddScore, layoutHeader, buttonPanic, buttonServerNodes, buttonServerMeter,buttonServerBoot;

	*new { |parent, bounds, lemurClient|
		^super.new(parent, bounds).initialize(lemurClient);
	}

	initialize { |lemurClient|
		scores = List();
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
	    buttonPanic.font = Font("Menlo", 14);  // scoreprojectviewsettings  Font("Menlo", 14);  Rename naar ScoreControlConfiguration. En pas toe voor meerdere defaults.
        buttonPanic.states = [["PANIC", Color.red, Color.black]];
		buttonPanic.minHeight = 40;
		buttonPanic.action = {
			CmdPeriod.run;
            Server.freeAll(evenRemote: false);
		};
		layoutHeader.add(buttonPanic);

		buttonServerNodes = Button();
	    buttonServerNodes.font = Font("Menlo", 14);  // scoreprojectviewsettings  Font("Menlo", 14);  Rename naar ScoreControlConfiguration. En pas toe voor meerdere defaults.
        buttonServerNodes.states = [["SERVER\nNODES", Color.red, Color.black]];
		buttonServerNodes.minHeight = 40;
		buttonServerNodes.action = {
			Server.default.plotTree();
		};
		layoutHeader.add(buttonServerNodes);

		buttonServerMeter = Button();
	    buttonServerMeter.font = Font("Menlo", 14);  // scoreprojectviewsettings  Font("Menlo", 14);  Rename naar ScoreControlConfiguration. En pas toe voor meerdere defaults.
        buttonServerMeter.states = [["SERVER\nMETER", Color.red, Color.black]];
		buttonServerMeter.minHeight = 40;
		buttonServerMeter.action = {
			Server.default.meter();
		};
		layoutHeader.add(buttonServerMeter);

		buttonServerBoot = Button();
	    buttonServerBoot.font = Font("Menlo", 14);  // scoreprojectviewsettings  Font("Menlo", 14);  Rename naar ScoreControlConfiguration. En pas toe voor meerdere defaults.
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

		buttonAddScore = PlusButton();
		buttonAddScore.fixedHeight = 30;
		buttonAddScore.fixedWidth = 70;
		buttonAddScore.action = { this.invokeEvent(this.eventAddScore); };
		mainLayout.add(buttonAddScore,  align: \bottomRight);
	}

	invokeEvent { |event|
		event.changed(this);
	}

	initializeEvents {
		eventAddScore = ();
	}

	registerEventHandlers {
		eventAddScore.addDependant({
			this.addScore();
		});
		projectSaveAndLoadView.eventLoadProject.addDependant({  | event, sender |
			this.loadState(sender.projectData);
		});
		projectSaveAndLoadView.eventSaveProject.addDependant({  | event, sender |
			sender.projectData = this.getState();
		});
	}

	addScore {
		var scoreView = ScoreControlView(lemurClient);
		var scoreMixerChannelView = scoreView.getMixerChannelControl();
		scoreView.removeAction = { | sender |
			scoreMixerChannelView.remove();
			scoreView.dispose();
			scores.remove(scoreView);
		};
		layoutMixerChannels.insert(scoreMixerChannelView, scores.size); // workaround. insert before stretchable space.
		scores.add(scoreView);
		^scoreView;
	}

	getState {
		var scoreStates = scores.collect({ |score|
			score.getState();
		});
		var projectState = Dictionary();
		projectState[\type] = "scoreprojectview";
		projectState[\scoreStates] = scoreStates;
		^projectState;
	}

	loadState{ |projectState|
		var scoreView;
		if (projectState.isKindOf(Dictionary) && projectState[\type] == "scoreprojectview",
			{
				// Remove the scores that are to many.
				if (projectState[\scoreStates].size	< scores.size, {
					var amountToMany = scores.size - projectState[\scoreStates].size;
					amountToMany do: {
						scores.pop().removeAction.value();
					};
				});
				projectState[\scoreStates] do: { |scoreState, position|
					if (scores[position].isNil, {
						scoreView = this.addScore();
					}, {
						scoreView = scores[position];
					});
					[\loadState, scoreState].postln;
					scoreView.loadState(scoreState);
				};
		});
	}
}
