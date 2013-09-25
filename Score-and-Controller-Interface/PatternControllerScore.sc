PatternControllerScore : ScoreWidget {
	var isOpen, <>lemur, <scorePresetMenu, <controllers, <scoreName, <playingStream, <keyAndPatternPairs;
	var mixerAmpProxy, eventStreamProxy, eventStream, controllerProxies;
	var <parent, scoreGui, mixerGui, mixerCanvas;
	var <>index;

	*new { |argLemur, argIndex|
		^super.newCopyArgs.init(argLemur, argIndex);
	}

	initProxies {
		mixerAmpProxy = PatternProxy.new;
		mixerAmpProxy.source = 1;
		eventStreamProxy = PatternProxy.new;
		eventStreamProxy.source = Pbind(\dur, 1);
		eventStream = Pmul(\amp, mixerAmpProxy, eventStreamProxy);
	}

	init { |argLemur, argIndex|

		isOpen = false;
		lemur = argLemur;

		index = argIndex;

		controllerProxies = IdentityDictionary.new;

		scorePresetMenu = ScorePresetMenu.new;
		scorePresetMenu.storeAction = {
			var preset = IdentityDictionary.new;
			controllers do: { |channel|
				preset[channel.name.postln] = channel.paramController.getState.postln;
			};
			preset.copy.postln;
		};

		scorePresetMenu.action = { |preset|
			var nameArray = [];
			controllers do: { |channel| nameArray = nameArray.add(channel.name); };
			preset = preset.copy;
			preset.keys do: { |key|
				var nameIndex = nameArray.indexOf(key).postln;
				if (nameIndex.notNil) {
					controllers[nameIndex].paramController.loadState(preset[key].postln)
				};
			};
		};

		this.initProxies;

		scoreName = "Score" ++ argIndex;

		model = (
			scoreName: scoreName,
			envirTextField: "",
			mixerAmpFader: 1,
			playButton: 0
		);
		dependants = ();
		setValueFunction = ();
		[\envirTextField, \mixerAmpFader, \scoreName, \playButton] do: { |key|
			setValueFunction[key] = { |inArg|
				model[key] = inArg;
				model.changed(key, inArg);
			};
		};
		dependants[\interpresetEnvirTextField] = {|theChanger, what, script|
			if (what == \envirTextField) {
				script=  "Environment.make({" ++ script ++ "})";
				script = interpret(script);
				if (script.isNil) {
					"Debug envirField".postln;
				} {
					"Add environment output to Pbind and stuff".postln;
					script.postln;
				};
			};
		};
		model.addDependant(dependants[\interpresetEnvirTextField]);
		dependants[\scoreName] = {|theChanger, what, argScoreName|
			if (what == \scoreName) {
				controllers do: { |i|
					i.paramController.pagename = argScoreName;

				};
				lemur.removePage(scoreName);
				scoreName = argScoreName;
			}
		};
		model.addDependant(dependants[\scoreName]);
		dependants[\playButton] =  {|theChanger, what, value|
			if (what == \playButton) {
				if (value > 0) {
					playingStream = eventStream.play } { playingStream.stop };
			};
		};
		model.addDependant(dependants[\playButton]);

		dependants[\mixerAmpFader] =  {|theChanger, what, value|
			if (what == \mixerAmpFader) {
				mixerAmpProxy.source = value;
			};
		};
		model.addDependant(dependants[\mixerAmpFader]);

		controllers = Array.new;

		keyAndPatternPairs = IdentityDictionary.new;

	}

	makeScoreGui {

		isOpen = true;
		parent = Window.new("", Rect(200,400,700,800))
		.background_(Color.new255(* ({ 150 }!3 ++ 230)));

		scoreGui = ();
		scoreGui[\scoreNameField] = TextField(parent, Rect(10,10,110,30))
		.string_(model[\scoreName])
		.action_({|v| setValueFunction[\scoreName].value(v.string)});
		dependants[\scoreNameScoreGui] = {|theChanger, what, argScoreName|
			if (what == \scoreName) {
				scoreGui[\scoreNameField].string = argScoreName;
			}
		};
		model.addDependant(dependants[\scoreNameScoreGui]);

		scoreGui[\playButton] = Button(parent, Rect(parent.bounds.width - 62,10,50,30))
		.value_(model[\playButton])
		.font_(MUI.settings[\font])
		.states_([["PLAY", Color.red, Color.black],["STOP", Color.black, Color.red]])
		.action_({|b| setValueFunction[\playButton].value(b.value)});
		dependants[\playButtonScoreGui] =  {|theChanger, what, value|
			if (what == \playButton) {
				scoreGui[\playButton].value = value;
			};
		};
		model.addDependant(dependants[\playButtonScoreGui]);

		scoreGui[\randomizeButton] = Button(parent, Rect(220,10,80,30))
		.font_(MUI.settings[\font])
		.states_([["RANDOMIZE", Color.red, Color.black]])
		.action_({ this.randomize; });


		scorePresetMenu.makeGui(parent, Rect(10, 44, parent.bounds.width - 12, 30));

		scoreGui[\envirTextField] = TextView(parent, Rect(10, 80, parent.bounds.width - 20, 130))
		.background_(Color.white.alpha_(0.5))
		.string_(model[\envirTextField])
		.keyDownAction_({| ... args|
			var bool = args[2] == 131072;
			bool = postln(args[1].ascii == 13) && bool.postln;
			if (bool) { setValueFunction[\envirTextField].value(scoreGui[\envirTextField].string) };
		});

		dependants[\envirTextFieldScoreGui] = {|theChanger, what, script|
			if (what == \envirTextField) {
				scoreGui[\envirTextField].string = script;
			};
		};
		model.addDependant(dependants[\envirTextFieldScoreGui]);

		[\nameView, \typeView, \cntrView] do: { |key, i|
			scoreGui[key] = StaticText(parent,
				Rect(
					MUI.settings[[\xName, \xLayers, \xButtons][i]],
					MUI.settings[\chOffset] - MUI.settings[\chHeight],
					[70, 180, 70][i],
					MUI.settings[\chHeight])
			)
			.font_(MUI.settings[\font])
			.string = ["NAME", "SCRIPT OR WIDGET?", "CONTROLS"][i];
		};

		controllers do: { |con, i|
			con.makeGui(
				parent,
				Rect(
					0,
					i * 42 + MUI.settings[\chOffset],
					parent.bounds.width,
					40));
		};

		scoreGui[\addChannelView] = CompositeView(parent, Rect(
			MUI.settings[\xLayers],
			controllers.size * 40 + MUI.settings[\chOffset] + 44,
			40,
			30)).background_(Color.clear);

		scoreGui[\addChannelButton] = MButtonP(scoreGui[\addChannelView],scoreGui[\addChannelView].bounds.extent)
		.action_({ this.addChannel });

		parent.onClose = { this.closeGui };
		parent.front;

	}

	makeScoreMixerChannelGui { |parent, yOffset = 200, height = 50|
		var font = Font("Menlo", 14);
		mixerCanvas = CompositeView(parent, Rect(0, yOffset, parent.bounds.width,height))
		.background_(Color.black.alpha_(0.2));

		mixerGui = ();
		mixerGui[\mixerScorePlay] = Button(mixerCanvas,Rect(0,0,40,40))
		.font_(font)
		.states_([
			["PLAY", Color.red.alpha_(0.8), Color.black],
			["STOP", Color.black,Color.red.alpha_(0.8)]])
		.action_({|b| setValueFunction[\playButton].value(b.value)});
		dependants[\mixerScorePlay] =  {|theChanger, what, value|
			if (what == \playButton) {
				mixerGui[\mixerScorePlay].value = value;
			};
		};
		model.addDependant(dependants[\mixerScorePlay]);

		mixerGui[\mixerAmpFader] = EZSlider(
			mixerCanvas,
			Rect(50,0,300,40),
			"",
			\db.asSpec.step_(0.01),
			unitWidth:30,
			numberWidth:60,
			layout:
			\line2,
			margin: nil)
		.setColors(Color.black.alpha_(0),Color.black, Color.black.alpha_(0),Color.black.alpha_(0), Color.red,Color.black.alpha_(1),nil,nil, Color.black.alpha_(0))
		.value_(model[\mixerAmpFader])
		.action_({ |v| setValueFunction[\mixerAmpFader].value(v.value) })
		.labelView.string_(model[\scoreName]);

		dependants[\mixerAmpFaderGui] =  {|theChanger, what, value|
			if (what == \mixerAmpFader) {
				mixerGui[\mixerAmpFader].value = value;
			};
		};
		model.addDependant(dependants[\mixerAmpFaderGui]);

		mixerGui[\popupScore] = Button(mixerCanvas,Rect(350,0,50,40))
		.font_(font)
		.states_([["SCORE", Color.red.alpha_(0.8), Color.black]])
		.action_({ if (isOpen) {
			parent.front
			} {
				this.makeScoreGui;
		}});
		scoreGui[\removeScore] = MButtonV(mixerCanvas, Rect(350,42,6,6))
		.action_({ this.removeScore });
	}

	addChannel {
		var currentIndex = controllers.size;

		var paramName = "Param" ++ currentIndex;
		var paramChannel = ParamChannel.new(
			argName: paramName,
			argIndex: currentIndex,
			argLemur: lemur,
			argLemurXoffset: currentIndex * 110,
			argPageName: model[\scoreName],
			argObjectReferenceName: "Object" ++ currentIndex
		);

		paramChannel.controllerProxies = (
			fader: PatternProxy.new(1),
			rangeLo: PatternProxy.new(1),
			rangeHi: PatternProxy.new(1)
		);

		paramChannel.paramController.rangeAction = { | val |
			paramChannel.controllerProxies[\rangeLo] = val[0];
			paramChannel.controllerProxies[\rangeHi] = val[1];
		};

		paramChannel.paramController.faderAction = { | val |
			paramChannel.controllerProxies[\fader] = val
		};

		paramChannel.nameAction = {
			paramChannel.name.postln;
			keyAndPatternPairs = Dictionary.new;
			if (paramChannel.paramProxy.isNil) { paramChannel.paramProxy = PatternProxy.new(1) };
			controllers do: { |i|
				keyAndPatternPairs[i.name.asSymbol] = paramChannel.paramProxy;
			};
			eventStreamProxy.source = Pbind(*keyAndPatternPairs.getPairs);
		};

		paramChannel.pScript.action = { |code|
			code = "{ |fader, rangeLo, rangeHi| " ++ code ++ "}";
			code.postln;
			code = interpret(code);
			if (paramChannel.paramProxy.isNil) { paramChannel.paramProxy = PatternProxy.new(1) };
			paramChannel.paramProxy.source = code.value(
				controllerProxies[\fader],
				controllerProxies[\rangeLo],
				controllerProxies[\rangeHi]);
		};

		paramChannel.removeAction = { |index|
			var tempChannel = controllers.removeAt(index);
			tempChannel.closeGui;
			tempChannel.closeLemur;
			this.positionChannels;
			keyAndPatternPairs = IdentityDictionary.new;
			controllers do: { |i|
				keyAndPatternPairs[i.name.asSymbol] = i.controllerProxy;
			};
			if (keyAndPatternPairs.size >= 2) {
				eventStreamProxy.source = Pbind(*keyAndPatternPairs.getPairs);
			} {
				eventStreamProxy.source = Pbind();
			}
		};

		controllers = controllers.add(paramChannel);

		scoreGui[\addChannelView].moveTo(
			MUI.settings[\xLayers],
			controllers.size * 42 + MUI.settings[\chOffset],
			40,
			30);

		if (isOpen != false) { paramChannel.makeGui(parent,
			Rect(
				0,
				currentIndex * 42 + MUI.settings[\chOffset],
				parent.bounds.width,
				40))
		};

		paramChannel;

	}

	randomize {
		controllers do: { |i|
			i.paramController.loadState((fader: 1.0.rand, range: sort({1.0.rand}!2)))
		}
	}

	positionChannels {
		controllers do: { |instance, index|
			instance.index = index;
			instance.moveBounds(0,index * 42 + MUI.settings[\chOffset]);
			instance.moveLemur(index * 110);

		};
		scoreGui[\addChannelView].moveTo(
			MUI.settings[\xLayers],
			controllers.size * 42 + MUI.settings[\chOffset],
			40,
			30);
	}

	closeMixerChannelGui {
		model.removeDependant(dependants[\mixerScorePlay]);
		model.removeDependant(dependants[\mixerAmpFaderGui]);
	}

	closeScoreGui {
		isOpen = false;
		model.removeDependant(dependants[\scoreNameScoreGui]);
		model.removeDependant(dependants[\envirTextFieldScoreGui]);
		model.removeDependant(dependants[\playButtonScoreGui]);
	}

	getState {
		var scoreState = Dictionary.new;
		// Basic PatternControllerScore state:
		scoreState[\scoreName] = model[\scoreName].copy;
		scoreState[\presetMenu] = Dictionary.new;
		scoreState[\presetMenu][\currentPresetIndex] = scorePresetMenu.currentPresetIndex.copy;
		scoreState[\presetMenu][\presets] = scorePresetMenu.presets.copy;
		scoreState[\presetMenu][\presetMenuItems] = scorePresetMenu.presetMenuItems.copy;
		scoreState[\envirTextField] = model[\envirTextField].copy;
		scoreState[\mixerAmpFader] = model[\mixerAmpFader].copy;
		// retrieve controller settings by iteration over the controller array
		scoreState[\controllers] = IdentityDictionary.new;
		controllers do: { |conCh, i|
			scoreState[\controllers][i] = Dictionary.new;

			scoreState[\controllers][i][\name] = conCh.name.copy;
			scoreState[\controllers][i][\script] = conCh.pScript.getState;
			scoreState[\controllers][i][\paramController] = conCh.paramController.getState.copy;
			scoreState[\controllers][i][\paramControllerCurrentWidget] = conCh.currentWidgetType.copy;
			scoreState[\controllers][i][\spec] = conCh.pSpec.spec.copy;
		};
		^scoreState;
	}
}
