/*
FILENAME: ScoreControl -> TODO rename ScoreEditorView

DESCRIPTION: Score Editor View

AUTHOR: Marinus Klaassen (2012, 2021Q3)
~test
EXAMPLE:
s.boot;
m = ScoreControlView().front();
a = m.getState()
m.loadState(a);
m.model[\envirText]
m.model
a.keys do: { |key| a[key.postln].postln }
Server.freeAll(evenRemote: false)
a[\controllerStates] do: { |state| state.postln; }
s.boot;
Needs some propper cleanup and reformatting.
*/

ScoreControlView : View {
    var <>lemurClient, <presetManagerView, <controllers, <scoreName, <playingStream, <keyAndPatternPairs;
    var mixerAmpProxy, eventStreamProxy, <eventStream, controllerProxies, eventParProxy, setValueFunction, <model, dependants, parentView;
    var scoreGui, mixerGui, <scoreControlMixerChannelView,layoutMain,layoutHeader,layoutFooter,scrollViewControls,layoutControlHeaderLabels,layoutChannels,textScoreId, buttonPlay, buttonRandomize, presetManagerView, textEnvirFieldView; // TODOP
    var layoutControlHeaderLabels,labelParamNameControlHeader, errorLabelEnvirFieldView, labelParamControlScriptOrControllerHeader, labelParamControlSelectorsHeader, labelPatternLayers, numberBoxPatternLayers, buttonAddChannel;
    var <>index, >closeAction,<>removeAction, scoreId, commandPeriodHandler;

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

                model[key.asSymbol] = inArg;
                model.changed(key, inArg);
            };
        };

        dependants[\interpresetenvirText] = {|theChanger, what, environmentCode|
            var environment;
			if (what == \envirText) {

                errorLabelEnvirFieldView.visible = false;
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
                    errorLabelEnvirFieldView.visible = true; // Show error.
                };
            };
        };

        model.addDependant(dependants[\interpresetenvirText]);

        dependants[\scoreId] = {|theChanger, what, argScoreName|
            if (what == \scoreId) {
                scoreName = argScoreName;
            }
        };

        model.addDependant(dependants[\scoreId]);

        dependants[\buttonPlay] =  {|theChanger, what, value|
            if (what == \buttonPlay) {
                if (value > 0) {
                    playingStream = eventStream.play

                } { playingStream.stop };
            };
        };
        model.addDependant(dependants[\buttonPlay]);

        dependants[\faderScoreControlVolume] =  {|theChanger, what, value|
            if (what == \faderScoreControlVolume) {
                mixerAmpProxy.source = value;
            };
        };
        model.addDependant(dependants[\faderScoreControlVolume]);

		commandPeriodHandler = { setValueFunction[\buttonPlay].value(0); };

	    CmdPeriod.add(commandPeriodHandler);

        controllers = Array();

        keyAndPatternPairs = IdentityDictionary();
        this.initializeView();

    }

    initializeView {

        // This object View (base class) settings
        this.bounds = 700@800; // TODO
        this.background = Color.new255(136, 172, 224); // medium petrol blue
        this.deleteOnClose = false;

        layoutMain = VLayout();
        this.layout = layoutMain;

        // Header controls
        // Score Id input text field
        layoutHeader = HLayout();
        layoutMain.add(layoutHeader);

        textScoreId = TextField();
        textScoreId.minWidth = 150;
        textScoreId.minHeight = 50;
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
        buttonPlay.font = Font("Menlo", 14);  // scoreprojectviewsettings  Font("Menlo", 14);  Rename naar ScoreControlConfiguration. En pas toe voor meerdere defaults.
        buttonPlay.states = [["PLAY", Color.red, Color.black],["STOP", Color.black, Color.red]];
        buttonPlay.minWidth = 106;
        buttonPlay.minHeight = 50;
        buttonPlay.action_({ |val| setValueFunction[\buttonPlay].value(val.value); });

        buttonRandomize = Button();
        buttonRandomize.font = Font("Menlo", 14);  // scoreprojectviewsettings  Font("Menlo", 14);
        buttonRandomize.states = [["RANDOMIZE", Color.red, Color.black]];
        buttonRandomize.minWidth = 106;
        buttonRandomize.minHeight = 50;
        buttonRandomize.action = { this.randomize(); };

        layoutHeader.add(nil, stretch: 1.0);
        layoutHeader.add(buttonRandomize, align: \right);

        dependants[\buttonPlay] =  {|theChanger, what, value|
            if (what == \buttonPlay) {
                buttonPlay.value = value;
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
		textEnvirFieldView.enterInterpretsSelection = false;

        dependants[\textEnvirFieldView] = {|theChanger, what, script|
            if (what == \envirText) {
                textEnvirFieldView.string = script;
            };
        };
        model.addDependant(dependants[\textEnvirFieldView]);

        layoutMain.add(textEnvirFieldView);

        errorLabelEnvirFieldView = StaticText();
        errorLabelEnvirFieldView.visible = false;
        errorLabelEnvirFieldView.string = "Invalid input.";
        errorLabelEnvirFieldView.stringColor = Color.red;

        layoutMain.add(errorLabelEnvirFieldView, align: \right);

        layoutControlHeaderLabels = HLayout();
        layoutControlHeaderLabels.margins = [5, 5, 5, 0];
        labelParamNameControlHeader = StaticText();
        labelParamNameControlHeader.font = ScoreWidgetSettings.settings[\font];
        labelParamNameControlHeader.string = "NAME";
        labelParamNameControlHeader.maxWidth = 93;
        labelParamNameControlHeader.minWidth = 93;
        layoutControlHeaderLabels.add(labelParamNameControlHeader, align: \left);

        labelParamControlScriptOrControllerHeader = StaticText();
        labelParamControlScriptOrControllerHeader.font = ScoreWidgetSettings.settings[\font];
        labelParamControlScriptOrControllerHeader.string = "SCRIPT/CONTROLLER";
        layoutControlHeaderLabels.add(labelParamControlScriptOrControllerHeader, align: \left, stretch: 1.0);

        labelParamControlSelectorsHeader = StaticText();
        labelParamControlSelectorsHeader.font = ScoreWidgetSettings.settings[\font];
        labelParamControlSelectorsHeader.maxWidth = 145;
        labelParamControlSelectorsHeader.minWidth = 145;
        labelParamControlSelectorsHeader.string = "SELECTORS";
        layoutControlHeaderLabels.add(labelParamControlSelectorsHeader, align: \right);

        layoutMain.add(layoutControlHeaderLabels);

        layoutChannels = VLayout([nil, stretch:1, align: \bottom]); // workaround. insert before stretchable space.
        layoutChannels.margins_([0,0,0,0]);
        layoutChannels.spacing_(2);

        scrollViewControls = ScrollView();
        scrollViewControls.canvas = View();
        scrollViewControls.canvas.layout = layoutChannels;
        scrollViewControls.canvas.background = this.background;
        scrollViewControls.background = this.background;

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
        .action_({|b| setValueFunction[\buttonPlay].value(b.value)});

        dependants[\togglePlayScore] =  {|theChanger, what, value|
            if (what == \buttonPlay) {
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
        .action_({ this.removeAction.value(this); }); // Zorg ook dat indien Play aanstaat deze wordt uitgezet.
        // Letop dat close de widgets nog steeds in leven houdt. dus zorg dat de ScoreControl een dispose heeft.

        layout.add(mixerGui[\removeScore], align: \topRight);

        mixerGui[\colorSection] = UserView().maxWidth_(15).minWidth_(15);

        mixerGui[\colorSection].background_(Color.black.alpha_(0.1));
        layout.add(mixerGui[\colorSection]);
        ^scoreControlMixerChannelView;
    }

    addChannel {
        var paramChannel = ScoreParamView();

        paramChannel.controllerProxies = (
            fader: PatternProxy(0),
            rangeLo: PatternProxy(0),
            rangeHi: PatternProxy(1)
        );

        paramChannel.rangeSliderAction = { | rangeList |
            paramChannel.controllerProxies[\rangeLo].source = rangeList[0];
            paramChannel.controllerProxies[\rangeHi].source = rangeList[1];
        };

        paramChannel.paramProxy = PatternProxy(1);

        paramChannel.sliderAction = { | val |
            paramChannel.controllerProxies[\fader].source = val
        };

        paramChannel.actionNameChanged = { |sender|
            keyAndPatternPairs = Dictionary();
            if (paramChannel.paramProxy.isNil) { paramChannel.paramProxy = PatternProxy(1) };
            controllers do: { |aChannel|
                keyAndPatternPairs[aChannel.keyName.asSymbol] = aChannel.paramProxy;
            };
            eventStreamProxy.source = Pbind(*keyAndPatternPairs.getPairs);
        };

        paramChannel.actionPatternScriptChanged = { | sender |
            var func = nil;
            sender.clearError();
            try {
                func = interpret("{ |fader, rangeLo, rangeHi, env| " ++ sender.string ++ "}");
            };

            if (func.notNil) {
                paramChannel.scriptFunc = func;
                paramChannel.paramProxy.source = func.value(
                    paramChannel.controllerProxies['fader'],
                    paramChannel.controllerProxies['rangeLo'],
                    paramChannel.controllerProxies['rangeHi'],
                    model[\environment]);
            } {
                sender.setErrorText("Invalid input.");
            };
        };

        paramChannel.actionButtonDelete = { | sender|
            var tempChannel = controllers.remove(paramChannel);
            paramChannel.remove(); // Remove itself from the layout.
            keyAndPatternPairs = IdentityDictionary();
            controllers do: { | scoreParamView |
                keyAndPatternPairs[scoreParamView.keyName.asSymbol] = scoreParamView.paramProxy;
            };
            if (keyAndPatternPairs.size >= 2) {
                eventStreamProxy.source = Pbind(*keyAndPatternPairs.getPairs);
            } {
                eventStreamProxy.source = Pbind();
            }

        };

        layoutChannels.insert(paramChannel, controllers.size);
        controllers = controllers.add(paramChannel);
        ^paramChannel;
    }

    randomize {
        controllers do: { |patternBoxParamView|
            patternBoxParamView.randomize();
        }
    }

    actionChangeLayers { |argLayers|
        eventParProxy.source = Ppar({eventStreamProxy}!argLayers);
    }

    getState {
        var state = Dictionary();
        state[\type] = "PatternBoxView";
        state[\layers] = numberBoxPatternLayers.value;
        state[\scoreId] = model[\scoreId];
        state[\envirText] = model[\envirText];
        state[\faderScoreControlVolume] = model[\faderScoreControlVolume];
        state[\controllerStates] = controllers collect: { | controller |
            controller.getState();
        };
        ^state;
    }

    loadState { |state|
        setValueFunction[\scoreId].value(state[\scoreId]);
		state[\envirText].postln;
        setValueFunction[\envirText].value(state[\envirText]);
		[\scorecontrol, state[\envirText]].postln;
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
                patternBoxParamView = this.addChannel();
            }, {
                patternBoxParamView = controllers[position];
            });
            patternBoxParamView.loadState(patternBoxParamState);
        };
	    numberBoxPatternLayers.value = state[\layers];
        this.actionChangeLayers(state[\layers]);
		setValueFunction[\faderScoreControlVolume].value(state[\faderScoreControlVolume]);
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
