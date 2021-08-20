
ScoreMixer : ScoreWidgetBase {
	var <scores, lemurClient, projectStateManager, yOffset=55, userControl, <window;

	*new { |lemurClient|
		^super.newCopyArgs.init(lemurClient);
	}

	init { |lemurClient|
		scores = List();
		projectStateManager = ProjectStateManager();
		projectStateManager.storeAction = { this.getProjectState(); }; // storeProjectStateAction
		projectStateManager.loadAction = { |projectState| this.loadProjectState(projectState); }; // loadProjectStateAction
		lemurClient = lemurClient;
	}

	getProjectState {
		^scores.collect({ |score| score.getState(); });
	}

	loadProjectState { |projectState|
		projectState do: { |scoreState, position|
			var newScore;
			if (scores[position].isNil) {
				newScore = ScoreControl(lemurClient, position);
				newScore.makeScoreMixerChannelGui(parent, position * 50 + yOffset, 48);
				scores.add(newScore);
			} {
				scores[position].loadState(scoreState);
			};
		};
		userControl[\addCanvas].moveTo(10, scores.size * 50 + yOffset + 5);
	}

	addScoreControl {
		var newScore = ScoreControl(lemurClient);
		newScore.gui();
		newScore.closeAction = { | self |
			scores.remove(self).dispose();
		};
		userControl[\layoutMixerChannels].insert(newScore.getMixerChannelControl(), scores.size); // workaround. insert before stretchable space.
		scores.add(newScore);
	}

	gui {
		window = Window("Score Mixer", Rect(1000, 300, 426.0, 600));
		window.background_(Color.new255(* (150!3 ++ 230)));
		window.alwaysOnTop_(true);
		// layouts
	    userControl = ();
	    userControl[\layoutMain] = VLayout();
        userControl[\layoutMixerChannels] = VLayout([nil, stretch:1, align: \bottom]); // workaround. insert before stretchable space.
		userControl[\layoutMixerChannels].margins_(0!4);
		// controls;
		userControl[\projectStateManagerUserControl] = projectStateManager.userControl;

		userControl[\buttonAdd] = PlusButton()
		.fixedHeight_(30)
		.fixedWidth_(70)
		.action_({ this.addScoreControl(); })
		.refresh();


		userControl[\mixerChannelsScrollView] = ScrollView();
		userControl[\mixerChannelsScrollView].canvas = View();
		userControl[\mixerChannelsScrollView].canvas.layout = userControl[\layoutMixerChannels];
		userControl[\mixerChannelsScrollView].canvas.background_(Color.new255(* (150!3 ++ 230)));

		// Add controls to main layout.
		userControl[\layoutMain].add(userControl[\projectStateManagerUserControl], align: \top);
		userControl[\layoutMain].add(userControl[\mixerChannelsScrollView]);
		userControl[\layoutMain].add(userControl[\buttonAdd], align: \bottomRight);

		// Add mixer channels to mixer channel layout.
		scores do: { |score, position| userControl[\layoutMixerChannels].insert(score.makeScoreMixerChannelGui(), position); };

	    window.layout = userControl[\layoutMain];
		window.front;
	}

	closeGui {
		scores do: (_.closeMixerChannelGui) // TODO just close will be fine. This should remove all connections.
	}
}
