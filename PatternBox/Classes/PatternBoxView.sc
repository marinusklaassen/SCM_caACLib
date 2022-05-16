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
    var <>bufferpool, <>lemurClient, <presetView, <bindViews, <playingStream;
    var mixerAmpProxy, eventStreamProxy, <eventStream, eventParProxy, setValueFunction, <model, dependants, parentView;
    var layoutMain, layoutHeader, envirChangeRequiresRecompilation, layoutFooter, buttonExpandAll, buttonCollapseAll, scrollViewBodyBindViews, layoutControlHeaderLabels,layoutBindViews,textpatternBoxName, buttonPlay, buttonRandomize, presetView, textEnvirFieldView;
    var layoutControlHeaderLabels,labelParamNameControlHeader, errorLabelEnvirFieldView, buttonAllEditModeOn, buttonAllEditModeOff, buttonCollapseExpandEnvir, labelParamTargetpatternTargetIDControlHeader, labelParamControlScriptOrControllerHeader, labelParamControlSelectorsHeader, buttonAddBindView;
    var <>index, <playState, >closeAction,<>removeAction, <patternBoxName, envirHeader, commandPeriodHandler, <>actionPlayStateChanged, <>actionNameChanged, <>actionVolumeChanged, <volume;

    classvar instanceCounter=0;

    *new { |parent, bounds, bufferpool|
        ^super.new(parent, bounds).initialize(bufferpool);
    }

    setName { |name|
        setValueFunction[\patternBoxName].value(name);
    }

    initialize { |bufferpool|
        this.bufferpool = bufferpool;
        playState =  0;
        mixerAmpProxy = PatternProxy();
        mixerAmpProxy.source = 1;
        eventStreamProxy = PatternProxy();
        eventStreamProxy.source = Pbind(\dur, 1);
        eventStream = Pmul(\amp, mixerAmpProxy, eventStreamProxy); // In de toekomst via een routing synth. ivm insert, sends etc.
        envirChangeRequiresRecompilation = true;
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

        setValueFunction[\volume] = { |volume|
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
                    if (envirChangeRequiresRecompilation == true) {
                        bindViews do: { |bindView|
                            bindView.compileAll();
                            bindView.rebuildPatterns();
                        };
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
			    playState = value;
				this.actionPlayStateChanged.value(this);
            };
        };

        model.addDependant(dependants[\buttonPlay]);

        commandPeriodHandler = { setValueFunction[\buttonPlay].value(0); };

        CmdPeriod.add(commandPeriodHandler);

        bindViews = List();

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

        buttonPlay = ButtonFactory.createInstance(this, class: "btn-toggle btn-large", buttonString1: "▶", buttonString2: "◼");
        buttonPlay.font = Font("Menlo", 24);
        buttonPlay.action_({ |val| setValueFunction[\buttonPlay].value(val.value); });

        buttonRandomize = ButtonFactory.createInstance(this, class: "btn-large btn-random", buttonString1: "RANDOMIZE");
        buttonRandomize.action = { this.randomize(); };

        dependants[\buttonPlay] =  {|theChanger, what, value|
            if (what == \buttonPlay) {
				defer({  buttonPlay.value = value; })
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
        textEnvirFieldView.visible = false;
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

        labelParamTargetpatternTargetIDControlHeader = StaticTextFactory.createInstance(this, class: "columnlabel-patternbox-move", labelText: "DRS");
        layoutControlHeaderLabels.add(labelParamTargetpatternTargetIDControlHeader, align: \left);

        labelParamNameControlHeader = StaticTextFactory.createInstance(this, class: "columnlabel-patternbox", labelText: "NAME");
        layoutControlHeaderLabels.add(labelParamNameControlHeader, align: \left);

        labelParamControlScriptOrControllerHeader = StaticTextFactory.createInstance(this, class: "columnlabel-patternbox", labelText: "SCRIPT/CONTROLLER");
        layoutControlHeaderLabels.add(labelParamControlScriptOrControllerHeader, align: \left, stretch: 1.0);

        labelParamControlSelectorsHeader =  StaticTextFactory.createInstance(this, class: "columnlabel-patternbox", labelText: "SELECTORS");
        layoutControlHeaderLabels.add(labelParamControlSelectorsHeader, align: \right);

        layoutMain.add(layoutControlHeaderLabels);

        layoutBindViews = VLayout([nil, stretch:1, align: \bottom]); // workaround. insert before stretchable space.
        layoutBindViews.margins_([0,0,0,0]);
        layoutBindViews.spacing_(5);

        scrollViewBodyBindViews = ScrollViewFactory.createInstance(this);
        scrollViewBodyBindViews.canvas.layout = layoutBindViews;
        scrollViewBodyBindViews.canvas.canReceiveDragHandler = {  |view, x, y|
            View.currentDrag.isKindOf(PatternBoxBindView);
        };
        scrollViewBodyBindViews.canvas.receiveDragHandler = { |view, x, y|
            var targetPosition = bindViews.size - 1;
            bindViews.remove(View.currentDrag);
            bindViews.insert(targetPosition, View.currentDrag);
            layoutBindViews.insert(View.currentDrag, targetPosition);
        };
        layoutMain.add(scrollViewBodyBindViews);

        layoutFooter = HLayout();

        layoutMain.add(layoutFooter);

        buttonCollapseExpandEnvir = ButtonFactory.createInstance(this, class: "btn-collapse-expand");
        buttonCollapseExpandEnvir.action_({ |sender|
            textEnvirFieldView.visible = sender.value == 1;
        });
        layoutFooter.add(buttonCollapseExpandEnvir, align: \left);

        buttonAllEditModeOn = ButtonFactory.createInstance(this, class: "btn-patternbox-footer", buttonString1: "show edit all");
        buttonAllEditModeOn.action_({ |sender|
            bindViews do: { | bindView| bindView.editMode = true; };
        });
        layoutFooter.add(buttonAllEditModeOn);

        buttonAllEditModeOff = ButtonFactory.createInstance(this, class: "btn-patternbox-footer", buttonString1: "hide edit all");
        buttonAllEditModeOff.action_({ |sender|
            bindViews do: { | bindView| bindView.editMode = false; };
        });

        layoutFooter.add(buttonAllEditModeOff);

        buttonExpandAll = ButtonFactory.createInstance(this, class: "btn-patternbox-footer", buttonString1: "expand all");
        buttonExpandAll.action_({ |sender|
            bindViews do: { | bindView| bindView.setBodyIsVisible(true); };
        });

        layoutFooter.add(buttonExpandAll);

        buttonCollapseAll = ButtonFactory.createInstance(this, class: "btn-patternbox-footer", buttonString1: "collapse all");
        buttonCollapseAll.action_({ |sender|
            bindViews do: { | bindView| bindView.setBodyIsVisible(false); };
        });

        layoutFooter.add(buttonCollapseAll);

        layoutFooter.add(nil);

        buttonAddBindView = ButtonFactory.createInstance(this, class: "btn-add");
        buttonAddBindView.toolTip = "Add a new PatternBox binding";
        buttonAddBindView.action = { this.addBindView(); };

        layoutFooter.add(buttonAddBindView, align: \right);
    }

    rebuildPatterns {
        var patterns = bindViews collect: { |bindView| bindView.bindSource };
        if (patterns.size == 0, { patterns.add(Pbind()); });
        eventStreamProxy.source = Ppar(patterns);
    }

    addBindView { |positionInLayout|
        var newBindView = PatternBoxBindView(this, bufferpool: bufferpool);

        newBindView.actionOnBindChanged = { |sender|
            this.rebuildPatterns();
        };

        newBindView.actionButtonDelete = { | sender|
            bindViews.remove(sender);
            sender.remove(); // Remove itself from the layout.
            this.rebuildPatterns();
        };

        newBindView.actionInsertPatternBoxBindView = { |sender, insertType|
            var positionInLayout = bindViews.indexOf(sender);
            if (insertType == "INSERT_AFTER", {
                positionInLayout = positionInLayout + 1;
            });
            this.addBindView(positionInLayout);
        };

        newBindView.actionMoveBindView = { |dragDestinationObject, dragObject|
            var targetPosition;
            if (dragDestinationObject !==  dragObject, {
                targetPosition = bindViews.indexOf(dragDestinationObject);
                bindViews.remove(dragObject);
                bindViews.insert(targetPosition, dragObject);
                layoutBindViews.insert(dragObject, targetPosition);
            });
        };

        newBindView.actionOnSoloStateChanged = { |sender|
            bindViews do: {  |bindView|
                if (bindView != sender, {
                    if (sender.soloState == 1, {
                        bindView.setSoloState(0, skipAction: true);
                        bindView.setMuteState(1, skipAction: true);
                    },
                    {
                        bindView.setMuteState(0, skipAction: true);
                    });
                });
            };
        };

        newBindView.actionRestartPatterns = { |sender|
            if (playingStream.isPlaying) {
                playingStream.stop;
                playingStream = eventStream.play(quant: 0.25);
            };
        };

        newBindView.actionInsertBindView = { |sender, type|
            if (type == "INSERT_AFTER", {
                var newPosition = bindViews.indexOf(sender);
                newPosition = newPosition + 1;
                this.addBindView(newPosition);
            });
            if (type == "INSERT_BEFORE", {
                var newPosition = bindViews.indexOf(sender);
                this.addBindView(newPosition);
            });
            if (type == "INSERT_AFTER_DUPLICATIE",
                {
                    var newPosition = bindViews.indexOf(sender);
                    var state = sender.getState();
                    newPosition = newPosition + 1;
                    if (state[\title].notNil, {
                        state[\title] = state[\title] + " - COPY";
                    });
                    this.addBindView(newPosition).loadState(state);
            });
        };

        if (positionInLayout.notNil, {
            layoutBindViews.insert(newBindView, positionInLayout);
            bindViews = bindViews.insert(positionInLayout, newBindView);
        },{
            layoutBindViews.insert(newBindView, bindViews.size);
            bindViews = bindViews.add(newBindView);
        });
        newBindView.addParamView();
        this.rebuildPatterns();
        ^newBindView;
    }

    randomize {
        bindViews do: { |bindView|
            bindView.randomize();
        }
    }

    volume_ { |volume|
        setValueFunction[\volume].value(volume);
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
        state[\envirText] = model[\envirText];
        state[\bindViewStates] = bindViews collect: { | bindView |
            bindView.getState();
        };
        state[\envirTextVisible] = textEnvirFieldView.visible;
        ^state;
    }

    loadState { |state, skipProjectStuf|
        if (skipProjectStuf != true, {
            this.setName(state[\patternBoxName]);
            setValueFunction[\volume].value(state[\volume]);
        });
        // Remove the scores that are to many.
        if (state[\bindViewStates].size < bindViews.size, {
            var amountToMany = bindViews.size - state[\bindViewStates].size;
            amountToMany do: {
                bindViews.pop().dispose()
            };
        });
        envirChangeRequiresRecompilation = false;
        setValueFunction[\envirText].value(state[\envirText]);
        envirChangeRequiresRecompilation = true;

        state[\bindViewStates] do: { |patternBoxParamState, position|
            var bindViewCreateOrUpdate;
            if (bindViews[position].isNil, {
                bindViewCreateOrUpdate =  this.addBindView();
            }, {
                bindViewCreateOrUpdate = bindViews[position];
            });
            bindViewCreateOrUpdate.loadState(patternBoxParamState);
        };

        textEnvirFieldView.visible = state[\envirTextVisible] == true;
        buttonCollapseExpandEnvir = if (state[\envirTextVisible] == true, 1, 0);

        this.rebuildPatterns();
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


