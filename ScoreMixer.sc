/*
FILENAME: ScoreProjectView

DESCRIPTION:
         - Score Project View lists all the scores, project save en read capabilities via its view.
         - Main entry point.
         - Simple maintainance tasks like add, delete, open score view, volume control and play sections.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
s.boot;
m = ScoreProjectView(bounds:400@700).front();
*/

ScoreProjectView : View {
	var <scores, lemurClient, mainLayout, projectSaveAndLoadView, layoutMixerChannels, scrollViewMixerChannels, buttonAddScore, <eventAddScore;

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
		this.name = "Score Project";
		this.background = Color.new255(136, 172, 224); // light petrol blue
		this.alwaysOnTop_(true);
		this.deleteOnClose = false;

		mainLayout = VLayout();
		this.layout = mainLayout;

		projectSaveAndLoadView = ProjectSaveAndLoadView();
		projectSaveAndLoadView.mainLayout.margins = 0!4;
		mainLayout.add(projectSaveAndLoadView);

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
			sender.projectSettings = this.getProjectSettings();
		});
		projectSaveAndLoadView.eventSaveProject.addDependant({  | event, sender |
			this.loadProjectSettings(sender.projectSettings);
		});
	}

	getProjectSettings {
		^scores.collect({ |score| score.getState(); });
	}

	loadProjectSettings { |projectSettings|
		var scoreView;
		projectSettings do: { |scoreSetting, position|
			if (scores[position].isNil) {
				scoreView = this.addScore();
			} {
				scoreView = scores[position];
			};
			scoreView.loadState(scoreSetting);
		};
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
}
