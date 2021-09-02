/*
 * FILENAME: ScoreControl -> TODO rename ScoreEditorView
 *
 * DESCRIPTION:
 *         - Score Editor View usercontrol
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 *
   ScoreControlView().front();

 TODO // Hier in de score control alle lemur code.
 */

ScoreControlView : View {
	var <>lemurClient, <presetManagerView, <controllers, <scoreName, <playingStream, <keyAndPatternPairs;
	var mixerAmpProxy, eventStreamProxy, <eventStream, controllerProxies, eventParProxy, setValueFunction, model, dependants, parentView;
	var scoreGui, mixerGui, <scoreControlMixerChannelView,layoutMain,layoutHeader,layoutFooter,scrollViewControls,layoutControlHeaderLabels,layoutChannels,textScoreId, buttonPlay, buttonRandomize, presetManagerView, textEnvirFieldView; // TODOP
	var layoutControlHeaderLabels,labelParamNameControlHeader, labelParamControlScriptOrControllerHeader, labelParamControlSelectorsHeader, labelPatternLayers, numberBoxPatternLayers, buttonAddChannel;
	var <>index, >closeAction,<>removeAction, scoreId;

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

		scoreId = "Score" ++ instanceCounter;

		model = (
			scoreId: scoreId,
			envirText: "",
			environment: Environment[],
			faderScoreControlVolume: 1,
			buttonPlay: 0
		);

		dependants = ();
		setValueFunction = ();
		[\envirText, \faderScoreControlVolume, \scoreId, \buttonPlay] do: { |key|
			setValueFunction[key] = { |inArg|
				model[key] = inArg;
				model.changed(key, inArg);
			};
		};

		dependants[\interpresetenvirText] = {|theChanger, what, environment|
			if (what == \envirText) {
				environment =  "Environment.make({" ++ environment ++ "})".postln;
				environment = interpret(environment);
				if (environment.notNil) {
					///////////////////////////////////////// TEXTFIELD ENVIRONMENT DEPEDANT FUNCTION <- I have no glue why I wrote this back in the day haha!
					model[\environment] = environment.postln;

					controllers do: { |aCon|
						aCon.paramProxy.source = aCon.scriptFunc.value(
						aCon.controllerProxies['fader'],
						aCon.controllerProxies['rangeLo'],
						aCon.controllerProxies['rangeHi'],
						model[\environment]
						)
					};
				} {
					"Debug envirField!!".postln;
				};
			};
		};
		model.addDependant(dependants[\interpresetenvirText]);

		dependants[\scoreId] = {|theChanger, what, argScoreName|
			if (what == \scoreId) {
				controllers do: { |i|
					i.paramController.pagename = argScoreName;

				};
				scoreName = argScoreName;
			}
		};

		model.addDependant(dependants[\scoreId]);

		dependants[\buttonPlay] =  {|theChanger, what, value|
			if (what == \buttonPlay) {
				if (value > 0) {
					playingStream = eventStream.play } { playingStream.stop };
			};
		};
		model.addDependant(dependants[\buttonPlay]);

		dependants[\faderScoreControlVolume] =  {|theChanger, what, value|
			if (what == \faderScoreControlVolume) {
				mixerAmpProxy.source = value;
			};
		};
		model.addDependant(dependants[\faderScoreControlVolume]);

		controllers = Array();

		keyAndPatternPairs = IdentityDictionary();
    	this.initializeView();

	}

	initializeView {

		// This object View (base class) settings
		this.bounds = 700@800; // TODO
		this.background = (Color.new255(* ({ 150 }!3 ++ 230)));
		this.deleteOnClose = false;

		layoutMain = VLayout();
		this.layout = layoutMain;

		// Header controls
		// Score Id input text field
		layoutHeader = HLayout();
		layoutMain.add(layoutHeader);

		textScoreId = TextField();
		textScoreId.minWidth = 150;
		textScoreId.string = model[\scoreId];
		textScoreId.action = { |val| setValueFunction[\scoreId].value(val.string); };

		dependants[\textScoreId] = {|theChanger, what, argScoreName|
			if (what == \scoreId) {
				textScoreId.string = argScoreName;
			}
		};

		model.addDependant(dependants[\textScoreId]);

		layoutHeader.add(textScoreId, align: \left);

		buttonPlay = Button();
		buttonPlay.value = model[\buttonPlay];
		buttonPlay.font = ScoreWidgetSettings.settings[\font]; // Rename naar ScoreControlConfiguration. En pas toe voor meerdere defaults.
		buttonPlay.states = [["PLAY", Color.red, Color.black],["STOP", Color.black, Color.red]];
		buttonPlay.minWidth = 106;
		buttonPlay.action_({ |val| setValueFunction[\buttonPlay].value(val.value); });

		buttonRandomize = Button();
		buttonRandomize.font = ScoreWidgetSettings.settings[\font];
		buttonRandomize.states = [["RANDOMIZE", Color.red, Color.black]];
		buttonRandomize.minWidth = 106;
		buttonRandomize.action = { this.randomize(); };

		layoutHeader.add(nil, stretch: 1.0);
		layoutHeader.add(buttonRandomize, align: \right);

		dependants[\buttonPlay] =  {|theChanger, what, value|
			if (what == \buttonPlay) {
				scoreGui[\buttonPlay].value = value;
			};
		};
		model.addDependant(dependants[\buttonPlay]);

		layoutHeader.add(buttonPlay, align: \right);

		presetManagerView = PresetManagerView();
		presetManagerView.storeAction = {
			var preset = Dictionary();
			controllers do: { |channel|
				preset[channel.name.asSymbol] = channel.paramController.getState;
			};
			preset.copy;
		};

		presetManagerView.action = { |aPreset|
			var nameArray = [];
			controllers do: { |channel| nameArray = nameArray.add(channel.name.asSymbol); };
			aPreset.keys do: { |key|
				var nameIndex = nameArray.indexOf(key);
				if (nameIndex.notNil) {
					controllers[nameIndex].paramController.loadState(aPreset[key])
				};
			};
		};

		layoutMain.add(presetManagerView);

		textEnvirFieldView = TextView();
		textEnvirFieldView.minHeight = 150;
		textEnvirFieldView.maxHeight = 150;
		textEnvirFieldView.background = Color.white.alpha_(0.5);
		textEnvirFieldView.string = model[\envirText];
		textEnvirFieldView.keyDownAction = {| ... args| // maak duidelijker wat hier gebeurt.
			var bool = args[2] == 524288;
			bool = args[1].ascii == 13 && bool;
			if (bool) { setValueFunction[\envirText].value(textEnvirFieldView.string) };
		};

		dependants[\textEnvirFieldView] = {|theChanger, what, script|
			if (what == \envirText) {
				textEnvirFieldView.string = script;
			};
		};
		model.addDependant(dependants[\textEnvirFieldView]);

		layoutMain.add(textEnvirFieldView);

		layoutControlHeaderLabels = HLayout();
		layoutControlHeaderLabels.margins = 0!4;

		labelParamNameControlHeader = StaticText();
		labelParamNameControlHeader.font = ScoreWidgetSettings.settings[\font];
        labelParamNameControlHeader.string = "NAME";
		layoutControlHeaderLabels.add(labelParamNameControlHeader, align: \left);

	    labelParamControlScriptOrControllerHeader = StaticText();
		labelParamControlScriptOrControllerHeader.font = ScoreWidgetSettings.settings[\font];
        labelParamControlScriptOrControllerHeader.string = "SCRIPT/CONTROLLER";
		layoutControlHeaderLabels.add(labelParamControlScriptOrControllerHeader, align: \left);

		labelParamControlSelectorsHeader = StaticText();
		labelParamControlSelectorsHeader.font = ScoreWidgetSettings.settings[\font];
        labelParamControlSelectorsHeader.string = "SELECTORS";
	    layoutControlHeaderLabels.add(labelParamControlSelectorsHeader, align: \right);

		layoutMain.add(layoutControlHeaderLabels);

		layoutChannels = VLayout([nil, stretch:1, align: \bottom]); // workaround. insert before stretchable space.
		layoutChannels.margins_([0,0,0,0]);
		layoutChannels.spacing_(2);

		scrollViewControls = ScrollView();
		scrollViewControls.canvas = View();
		scrollViewControls.canvas.layout = layoutChannels;
		scrollViewControls.canvas.background_(Color.new255(* (150!3 ++ 230)));
		scrollViewControls.background = Color.new255(* (150!3 ++ 230));

		layoutMain.add(scrollViewControls);

		layoutFooter = HLayout();

		layoutMain.add(layoutFooter);

		labelPatternLayers = StaticText();
		labelPatternLayers.string = "LAYERS";
        labelPatternLayers.font = ScoreWidgetSettings.settings[\font];
		labelPatternLayers.background = Color.clear;
		labelPatternLayers.stringColor = Color.white;

		layoutFooter.add(labelPatternLayers, align: \left);

		numberBoxPatternLayers = NumberBox();
		numberBoxPatternLayers.clipLo = 1;
		numberBoxPatternLayers.clipHi = 20;
		numberBoxPatternLayers.maxWidth = 25;

		numberBoxPatternLayers.action = { | sender | this.actionChangeLayers(sender.value) };

		layoutFooter.add(numberBoxPatternLayers, align: \left);
     	layoutFooter.add(nil);

		buttonAddChannel = PlusButton();
		buttonAddChannel.fixedHeight = 30;
		buttonAddChannel.fixedWidth = 70;
		buttonAddChannel.action = { this.addChannel(); };

		layoutFooter.add(buttonAddChannel, align: \right);
	}

	getMixerChannelControl {

		var font = Font("Menlo", 14); // Aanpassen. In een generieke settings object ofzo

		var layout = HLayout();
		layout.margins_(0!4);
		scoreControlMixerChannelView = View()
		.background_(Color.black.alpha_(0.2)); // TODO static configuration class

		scoreControlMixerChannelView.layout = layout;

		mixerGui = ();
		mixerGui[\togglePlayScore] = Button()
		.font_(font)
		.minWidth_(50).maxWidth_(45).minHeight_(50)
		.states_([
			["PLAY", Color.red.alpha_(0.8), Color.black],
			["STOP", Color.black,Color.red.alpha_(0.8)]])
		.action_({|b| setValueFunction[\togglePlayScore].value(b.value)});

		dependants[\togglePlayScore] =  {|theChanger, what, value|
			if (what == \togglePlayScore) {
				mixerGui[\togglePlayScore].value = value;
			};
		};

		layout.add(mixerGui[\togglePlayScore], align: \left);

		model.addDependant(dependants[\togglePlayScore]);

		mixerGui[\faderScoreControlVolume] = SCMSlider(controlSpec: \db.asSpec, initVal: 1, labelText: model[\scoreId])
		.value_(model[\faderScoreControlVolume])
		.action_({ |v| setValueFunction[\faderScoreControlVolume].value(v.value) });

		mixerGui[\faderScoreControlVolume].numberBoxView
	    .background_(Color.white.alpha_(0.5))
		.maxWidth_(60).minWidth_(60);

		layout.add(mixerGui[\faderScoreControlVolume], stretch: 1);

		dependants[\faderScoreControlVolumeGui] =  {|theChanger, what, value|
			if (what == \faderScoreControlVolume) {
				mixerGui[\faderScoreControlVolume].value = value;
			};
		};

		model.addDependant(dependants[\faderScoreControlVolumeGui]);

		dependants[\mixerLabelView] =  {|theChanger, what, value|
			if (what == \scoreId) {
				mixerGui[\faderScoreControlVolume].labelText = value;
			};
		};
		model.addDependant(dependants[\mixerLabelView]);

		mixerGui[\popupScore] = Button()
		.font_(font)
		.states_([["SCORE", Color.red.alpha_(0.8), Color.black]])
	    .minWidth_(50).maxWidth_(45).minHeight_(50)
		.action_({
			this.front();
		});

		layout.add(mixerGui[\popupScore]);

		mixerGui[\removeScore] = DeleteButton()
		.fixedSize_(10)
		.action_({ this.removeAction(this); }); // Zorg ook dat indien Play aanstaat deze wordt uitgezet.
		// Letop dat close de widgets nog steeds in leven houdt. dus zorg dat de ScoreControl een dispose heeft.

		layout.add(mixerGui[\removeScore], align: \topRight);

		mixerGui[\colorSection] = UserView().maxWidth_(15).minWidth_(15);

		mixerGui[\colorSection].background_(Color.black.alpha_(0.1));
		layout.add(mixerGui[\colorSection]);
		^scoreControlMixerChannelView;
	}

	addChannel {
		var currentIndex = controllers.size;
		var paramName = "Param" ++ currentIndex;
		var paramChannel = ScoreParamWidget(
			argName: paramName,
			argIndex: currentIndex,
			argLemur: this.lemurClient,
			argLemurXoffset: currentIndex * 110,
			argPageName: model[\scoreId],
			argObjectReferenceName: "Object" ++ currentIndex
		);

		paramChannel.controllerProxies = (
			fader: PatternProxy(0),
			rangeLo: PatternProxy(0),
			rangeHi: PatternProxy(1)
		);

		paramChannel.paramController.rangeAction = { | val |
			paramChannel.controllerProxies[\rangeLo].source = val[0].postln;
			paramChannel.controllerProxies[\rangeHi].source = val[1].postln;
		};

		paramChannel.paramProxy = PatternProxy(1);

		paramChannel.paramController.faderAction = { | val |
			paramChannel.controllerProxies[\fader].source = val.postln

		};

		paramChannel.nameAction = { |argName|
			argName.postln;
			keyAndPatternPairs = Dictionary();
			if (paramChannel.paramProxy.isNil) { paramChannel.paramProxy = PatternProxy(1) };
			controllers do: { |aChannel|
				keyAndPatternPairs[aChannel.name.asSymbol] = aChannel.paramProxy;
			};
			eventStreamProxy.source = Pbind(*keyAndPatternPairs.getPairs.postln);
		};

		paramChannel.pScript.action = { |code|
			var func = interpret("{ |fader, rangeLo, rangeHi, env| " ++ code ++ "}");
			paramChannel.scriptFunc = func;

			paramChannel.paramProxy.source = func.value(
				paramChannel.controllerProxies['fader'],
				paramChannel.controllerProxies['rangeLo'],
				paramChannel.controllerProxies['rangeHi'],
				model[\environment]
			);
		};

		paramChannel.removeAction = { |index|
			var tempChannel = controllers.removeAt(index);

			keyAndPatternPairs = IdentityDictionary();
			controllers do: { |i|
				keyAndPatternPairs[i.name.asSymbol] = i.paramProxy;
			};
			if (keyAndPatternPairs.size >= 2) {
				eventStreamProxy.source = Pbind(*keyAndPatternPairs.getPairs);
			} {
				eventStreamProxy.source = Pbind();
			}

		};

		controllers = controllers.add(paramChannel);

		// Add the view.
		layoutChannels.insert(NumberBox(), 0);
		layoutChannels.insert(NumberBox(), 1);
		layoutChannels.insert(NumberBox(), 2);

		/*
		   ScoreControlView().front
		*/

		paramChannel;

	}

	randomize {
		controllers do: { |i|
			i.paramController.loadState((fader: 1.0.rand, range: sort({1.0.rand}!2)))
		}
	}

	actionChangeLayers { |argLayers|
		eventParProxy.source = Ppar({eventStreamProxy}!argLayers);
		argLayers.postln;
	}

    getState {
		var scoreState = Dictionary();
		scoreState[\scoreId] = model[\scoreId].copy;
		scoreState[\presetMenu] = presetManagerView.getState.postln;
		scoreState[\envirText] = model[\envirText].copy;
		scoreState[\faderScoreControlVolume] = model[\faderScoreControlVolume].copy;

		scoreState[\controllers] = Dictionary();
		controllers do: { |conCh, i|
			scoreState[\controllers][i.asSymbol] = conCh.getState;
		};
		^scoreState;
	}

	loadState { |argPreset|
		setValueFunction[\envirText].value(argPreset[\envirText]);
		setValueFunction[\scoreId].value(argPreset[\scoreId]);
		setValueFunction[\faderScoreControlVolume].value(argPreset[\faderScoreControlVolume]);
		presetManagerView.loadState(argPreset[\presetMenu].postln);
		argPreset[\controllers].size do: { |i|
			if (controllers[i].isNil) { this.addChannel; };
		};
		controllers do: { |aScore,i |
			aScore.loadState(argPreset[\controllers][i.asSymbol])
		};
	}
}
