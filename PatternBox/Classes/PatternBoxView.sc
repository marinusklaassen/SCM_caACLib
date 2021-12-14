/*
keyFILENAME: PatternBoxView

DESCRIPTION: THe PatternBoxView is a dedicated reponsive editor to build patterns and assign controls to parameters.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

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

PatternBoxView : View {
	var <>lemurClient, <presetView, <controllers, <playingStream, <dictionaryPbindsByPatternTargetID;
	var mixerAmpProxy, eventStreamProxy, <eventStream, controllerProxies, eventParProxy, setValueFunction, <model, dependants, parentView;
	var scoreGui, mixerGui, <scoreControlMixerChannelView,layoutMain,layoutHeader,layoutFooter,scrollViewControls,layoutControlHeaderLabels,layoutChannels,textpatternBoxName, buttonPlay, buttonRandomize, presetView, textEnvirFieldView;
	var layoutControlHeaderLabels,labelParamNameControlHeader, errorLabelEnvirFieldView, buttonSpawnCopy, buttonClear, buttonShowProject, labelParamTargetpatternTargetIDControlHeader, labelParamControlScriptOrControllerHeader, labelParamControlSelectorsHeader, labelPatternLayers, numberBoxPatternLayers, buttonAddChannel;
	var <>index, <playState, >closeAction,<>removeAction, <patternBoxName, commandPeriodHandler, <>actionPlayStateChanged, <>actionNameChanged, <>actionVolumeChanged, <volume;

	classvar instanceCounter=0;

	*new { |parent, bounds, lemurClient|
		^super.new(parent, bounds).initialize(lemurClient);
	}

	initialize { |lemurClient|

		this.lemurClient = lemurClient;

		controllerProxies = IdentityDictionary();

		mixerAmpProxy = PatternProxy();
		mixerAmpProxy.source = 1;
		eventStreamProxy = PatternProxy();
		eventStreamProxy.source = Pbind(\dur, 1);
		eventParProxy = PatternProxy();
		eventParProxy.source = Ppar([eventStreamProxy]);
		eventStream = Pmul(\amp, mixerAmpProxy, eventParProxy); // In de toekomst via een routing synth. ivm insert, sends etc.

		instanceCounter = instanceCounter + 1;

		patternBoxName = "Box" + instanceCounter;
		this.name = "PatternBox: " ++ patternBoxName;

		model = (
			patternBoxName: patternBoxName,
			envirText: "",
			environment: Environment[],
			volume: 1,
			buttonPlay: 0
		);

		dependants = ();
		setValueFunction = ();

		[\envirText, \patternBoxName, \buttonPlay] do: { |key|
			setValueFunction[key] = { |inArg|
				model[key] = inArg;
				model.changed(key, inArg);
			};
		};

		setValueFunction [\volume] = { |volume|
			mixerAmpProxy.source = volume;
			model[\volume] = volume;
			model.changed(\volume, volume);
		};

		dependants[\interpresetenvirText] = {|theChanger, what, environmentCode|
			var environment;
			if (what == \envirText) {

				errorLabelEnvirFieldView.clear();
				environment =  "Environment.make({" ++ environmentCode ++ "})";

				try {
					environment = interpret(environment);
				} { environment = nil; };

				if (environment.notNil) {
					model[\environment] = environment;
					model[\envirText].postln;
					model[\environment].postln;
					controllers do: { |aCon|
						aCon.paramProxy.source = aCon.scriptFunc.value(
							aCon.controllerProxies['fader'],
							aCon.controllerProxies['rangeLo'],
							aCon.controllerProxies['rangeHi'],
							model[\environment]
						)
					};

				} {
					errorLabelEnvirFieldView.string = "Invalid input."
				};
			};
		};

		model.addDependant(dependants[\interpresetenvirText]);

		dependants[\patternBoxName] = {|theChanger, what, argpatternBoxName|
			if (what == \patternBoxName) {
				patternBoxName = argpatternBoxName;
				this.name = "PatternBox: " ++ patternBoxName;
				this.actionNameChanged.value(this);
			}
		};

		model.addDependant(dependants[\patternBoxName]);

		dependants[\buttonPlay] =  {|theChanger, what, value|
			if (what == \buttonPlay) {
				if (value > 0) {
					playingStream = eventStream.play(quant: 1);

				} { playingStream.stop };
			};
			playState = value;
			this.actionPlayStateChanged.value(this);
		};

		model.addDependant(dependants[\buttonPlay]);

		commandPeriodHandler = { setValueFunction[\buttonPlay].value(0); };

		CmdPeriod.add(commandPeriodHandler);

		controllers = List();

		dictionaryPbindsByPatternTargetID = IdentityDictionary();
		this.initializeView();

	}

	initializeView {

		layoutMain = VLayout();
		this.layout = layoutMain;
		this.deleteOnClose = false;

		textpatternBoxName = TextFieldFactory.createInstance(this);
		textpatternBoxName.string = model[\patternBoxName];
		textpatternBoxName.action = { |val| setValueFunction[\patternBoxName].value(val.string); };
		dependants[\textpatternBoxName] = {|theChanger, what, argpatternBoxName|
			if (what == \patternBoxName) {
				textpatternBoxName.string = argpatternBoxName;
			}
		};

		model.addDependant(dependants[\textpatternBoxName]);

		layoutMain.add(textpatternBoxName);

		// Header controls
		// Score Id input text field
		layoutHeader = HLayout();
		layoutMain.add(layoutHeader);

		buttonPlay = ButtonFactory.createInstance(this, class: "btn-toggle btn-large", buttonString1: "PLAY", buttonString2: "STOP");
		buttonPlay.action_({ |val| setValueFunction[\buttonPlay].value(val.value); });

		buttonRandomize = ButtonFactory.createInstance(this, class: "btn-large btn-random", buttonString1: "RANDOMIZE");
		buttonRandomize.action = { this.randomize(); };

		dependants[\buttonPlay] =  {|theChanger, what, value|
			if (what == \buttonPlay) {
				buttonPlay.value = value;
			};
		};

		model.addDependant(dependants[\buttonPlay]);

		layoutHeader.add(buttonPlay);
		layoutHeader.add(buttonRandomize);

		presetView = PresetViewFactory.createInstance(this);
		presetView.actionFetchPreset = {
			this.getState(skipProjectStuf: true);
		};
		presetView.actionLoadPreset = { |preset|
			this.loadState(preset, skipProjectStuf: true);
		};

		layoutMain.add(presetView);

		textEnvirFieldView = TextViewFactory.createInstance(this, class: "text-patternbox-environment-script");

		textEnvirFieldView.string = model[\envirText];
		textEnvirFieldView.keyDownAction = {| ... args| // maak duidelijker wat hier gebeurt.
			var bool = args[2] == 524288;
			bool = args[1].ascii == 13 && bool;
			if (bool) { setValueFunction[\envirText].value(textEnvirFieldView.string) };
		};
		textEnvirFieldView.enterInterpretsSelection = false;

		dependants[\textEnvirFieldView] = {|theChanger, what, script|
			if (what == \envirText) {
				textEnvirFieldView.string = script;
			};
		};
		model.addDependant(dependants[\textEnvirFieldView]);

		layoutMain.add(textEnvirFieldView);

		errorLabelEnvirFieldView = MessageLabelViewFactory.createInstance(this, class: "message-error");
		layoutMain.add(errorLabelEnvirFieldView, align: \right);

		layoutControlHeaderLabels = HLayout();
		layoutControlHeaderLabels.margins = [5, 5, 5, 0];

		labelParamTargetpatternTargetIDControlHeader = StaticTextFactory.createInstance(this, class: "columnlabel-patternbox-move", labelText: "MOVE");
		layoutControlHeaderLabels.add(labelParamTargetpatternTargetIDControlHeader, align: \left);


		labelParamTargetpatternTargetIDControlHeader = StaticTextFactory.createInstance(this, class: "columnlabel-patternbox-ptid", labelText: "PTID");
		layoutControlHeaderLabels.add(labelParamTargetpatternTargetIDControlHeader, align: \left);

		labelParamNameControlHeader = StaticTextFactory.createInstance(this, class: "columnlabel-patternbox", labelText: "NAME");
		layoutControlHeaderLabels.add(labelParamNameControlHeader, align: \left);

		labelParamControlScriptOrControllerHeader = StaticTextFactory.createInstance(this, class: "columnlabel-patternbox", labelText: "SCRIPT/CONTROLLER");
		layoutControlHeaderLabels.add(labelParamControlScriptOrControllerHeader, align: \left, stretch: 1.0);

		labelParamControlSelectorsHeader =  StaticTextFactory.createInstance(this, class: "columnlabel-patternbox", labelText: "SELECTORS");
		layoutControlHeaderLabels.add(labelParamControlSelectorsHeader, align: \right);

		layoutMain.add(layoutControlHeaderLabels);

		layoutChannels = VLayout([nil, stretch:1, align: \bottom]); // workaround. insert before stretchable space.
		layoutChannels.margins_([0,0,0,0]);
		layoutChannels.spacing_(2);

		scrollViewControls = ScrollViewFactory.createInstance(this);
		scrollViewControls.canvas.layout = layoutChannels;
		layoutMain.add(scrollViewControls);

		layoutFooter = HLayout();

		layoutMain.add(layoutFooter);

		labelPatternLayers = StaticTextFactory.createInstance(this, labelText: "layers:");

		layoutFooter.add(labelPatternLayers, align: \left);

		numberBoxPatternLayers = NumberBoxFactory.createInstance(this, class: "numberbox-patternbox-layers");

		numberBoxPatternLayers.action = { | sender | this.actionChangeLayers(sender.value) };

		layoutFooter.add(numberBoxPatternLayers, align: \left);
		layoutFooter.add(nil);

		buttonAddChannel = ButtonFactory.createInstance(this, class: "btn-add");
		buttonAddChannel.action = { this.addParamView(); };

		layoutFooter.add(buttonAddChannel, align: \right);
	}

	rebuildParallelPatternStreams { |sender|
		dictionaryPbindsByPatternTargetID = Dictionary();
		if (sender.paramProxy.isNil, {
			sender.paramProxy = PatternProxy(1) });
		controllers do: { |patternBoxParamView|
			var patternTargetID = if (patternBoxParamView.patternTargetID.size > 0, { patternBoxParamView.patternTargetID.asSymbol; }, { \default; });
			if (dictionaryPbindsByPatternTargetID[patternTargetID].isNil, {
				dictionaryPbindsByPatternTargetID[patternTargetID] = Dictionary();
			});
			dictionaryPbindsByPatternTargetID[patternTargetID][patternBoxParamView.keyName.asSymbol] = patternBoxParamView.paramProxy;
		};
		if (dictionaryPbindsByPatternTargetID.size == 0, {
			eventStreamProxy.source = Pbind();
		},
		{
			var pbinds = List();
			dictionaryPbindsByPatternTargetID.values do: { | pbindForTargetDict |
				pbinds.add(Pbind(*pbindForTargetDict.getPairs));
			};
			eventStreamProxy.source = Ppar(pbinds);
		});
	}

	addParamView {
		var paramChannel = PatternBoxParamView(this);

		paramChannel.actionPatternTargetIDChanged = { |sender|
			this.rebuildParallelPatternStreams(sender);
		};

		paramChannel.actionNameChanged = { |sender|
			this.rebuildParallelPatternStreams(sender);
		};

		paramChannel.actionButtonDelete = { | sender|
			controllers.remove(sender);
			sender.remove(); // Remove itself from the layout.
			this.rebuildParallelPatternStreams(sender);
		};

		paramChannel.actionMoveDown = { |sender|
			this.movePatternBoxParamView(sender, 1);
		};

		paramChannel.actionMoveUp = { |sender|
			this.movePatternBoxParamView(sender, -1);
		};

		layoutChannels.insert(paramChannel, controllers.size);
		controllers = controllers.add(paramChannel);
		^paramChannel;
	}

	movePatternBoxParamView { |patternBoxParamView, step|
		var currentPosition = controllers.indexOf(patternBoxParamView);
		var nextPosition = currentPosition + step;
		if (nextPosition >= 0 && (nextPosition < controllers.size), {
			controllers.removeAt(currentPosition);
			controllers.insert(nextPosition, patternBoxParamView);
			layoutChannels.insert(patternBoxParamView, nextPosition);
		});
	}

	randomize {
		controllers do: { |patternBoxParamView|
			patternBoxParamView.randomize();
		}
	}

	volume_ { |volume|
		setValueFunction.value(\volume, volume);
	}

	actionChangeLayers { |argLayers|
		eventParProxy.source = Ppar({eventStreamProxy}!argLayers);
	}

	play {
		setValueFunction[\buttonPlay].value(1);
	}

	stop {
		setValueFunction[\buttonPlay].value(0);
	}

	getState { |skipProjectStuf|
		var state = Dictionary();
		state[\type] = "PatternBoxView";
		if (skipProjectStuf == true, {
			state[\patternBoxName] = patternBoxName;
			state[\volume] = volume;
		});
		state[\layers] = numberBoxPatternLayers.value;
		state[\envirText] = model[\envirText];
		state[\controllerStates] = controllers collect: { | controller |
			controller.getState();
		};
		^state;
	}

	loadState { |state, skipProjectStuf|
		if (skipProjectStuf == true, {
			setValueFunction[\patternBoxName].value(state[\patternBoxName]);
			this.volume = state[\volume];
		});
		setValueFunction[\envirText].value(state[\envirText]);
		// Remove the scores that are to many.
		if (state[\controllerStates].size < controllers.size, {
			var amountToMany = controllers.size - state[\controllerStates].size;
			amountToMany do: {
				controllers.pop().dispose()
			};
		});
		state[\controllerStates] do: { |patternBoxParamState, position|
			var patternBoxParamView;
			if (controllers[position].isNil, {
				patternBoxParamView = this.addParamView();
			}, {
				patternBoxParamView = controllers[position];
			});
			patternBoxParamView.loadState(patternBoxParamState);
		};
		numberBoxPatternLayers.value = state[\layers];
		this.actionChangeLayers(state[\layers]);
	}

	dispose {
		this.deleteOnClose = true;
		this.close();
		this.remove(); // removes itselfs from the layout
		if (playingStream.notNil, { playingStream.stop(); });
		playingStream = nil;
		CmdPeriod.remove(commandPeriodHandler);
	}
}
