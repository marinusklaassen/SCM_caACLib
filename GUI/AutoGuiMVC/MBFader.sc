MBFader {
	var gui, model, depedants, <>spec, <name, <>action, midiResp, midiFlag;

	*new { |argName, argSpec|
		^super.newCopyArgs.init(argName,argSpec);
	}

	init { |argName, argSpec|
		if (argSpec.isNil) { argSpec = \amp.asSpec };
		if (argName.isNil) { argName = \default };
		spec = argSpec;
		name = argName;
		midiFlag = 0;
		model = (value: 0.0);
		model[\setValueFunction] = { |value|
			model[\value] = value;
			model.changed(\value, value);
		};
		depedants = ();
		depedants[\actionFunc] = {|theChanger, what, val|
			if (action.notNil) { action.value(spec.map(val)) };
		};
		model.addDependant(depedants[\actionFunc]);
	}

	makeGui { |parent, bounds|
		var sliderWidth = bounds.asRect.width - 160;
		bounds = bounds.asRect;
		gui = Dictionary.new;
		gui[\canvas] = CompositeView(parent,bounds);
		gui[\nameView] = StaticText.new(gui[\canvas], Rect(4,0,54,bounds.height));
		gui[\nameView].string_(name);
		gui[\sliderView] = Slider(gui[\canvas], Rect(60,0,sliderWidth-2,bounds.height));
		gui[\sliderView].value_(model[\value]);
		gui[\sliderView].action_({ |sl| model[\setValueFunction].value(sl.value) });
		gui[\boxView] = NumberBox(gui[\canvas], Rect(60+sliderWidth,0,68,bounds.height));
		gui[\boxView].value_(model[\value]);
		gui[\boxView].minDecimals_(1);
		gui[\boxView].action_({ |sl| model[\setValueFunction].value(spec.unmap(sl.value)) });
		gui[\midiView] = Button(gui[\canvas], Rect(130+sliderWidth,0,28,bounds.height))
		.states_([["OFF", Color.red, Color.black],
			["ON", Color.black, Color.red]])
		.action_({ arg butt;
			if (butt.value == 1) { this.midiLearn; } { this.midiUnlearn; };
		})
		.value_(midiFlag);
		depedants[\updateView] = {|theChanger, what, val|
			gui[\sliderView].value_(val);
			gui[\boxView].value_(spec.map(val));
		};
		model.addDependant(depedants[\updateView]);
		model[\setValueFunction].value(model[\value]);
	}

	midiLearn {
		if (midiResp.isNil) {
			midiResp = CCResponder({ |src,chan,num,value|
				{ model[\setValueFunction].value(value / 127); }.defer;
			});
		};
		midiResp.learn; // wait for the first controller
		midiFlag = 1;
	}

	midiUnlearn {
		midiResp.remove; midiResp = nil; midiFlag = 0;
	}

	value_ {|argValue|
		model[\setValueFunction].value(argValue)
	}

	value {
		^model[\value]
	}

	name_ {|argName|
		name = argName;
		if (gui.notNil) { gui[\nameView].string_(name); };
	}

	closeGui { model.removeDependant(depedants[\updateView]); }
}

MBNumberBox {
	var gui, <name, <>action, <value;

	*new { ^super.newCopyArgs.init; }

	init {
		name = "default";
		value = 0;
	}

	makeGui { |parent,bounds|
		var boxWidth = bounds.asRect.width - 160;
		bounds = bounds.asRect;
		gui = Dictionary.new;
		gui[\canvas] = CompositeView(parent,bounds);
		gui[\numberBox] = NumberBox.new(gui[\canvas],Rect(60,0,boxWidth,bounds.height));
		gui[\numberBox].action_({ |num| if (action.notNil) { action.value(num.value); value = num.value } });
		gui[\numberBox].value = value;
		gui[\nameView] = StaticText.new(gui[\canvas],Rect(4,0,54,bounds.height));
		gui[\nameView].string_(name);
	}

	value_ {|argValue|
		gui[\numberBox].value = argValue;
	}

	name_ {|argName|
		name = argName;
		if (gui.notNil) { gui[\nameView].string_(name); };
	}
}

MBControlPanel {
	var <>metaData, <gui, defName, <currentPresetIndex, <currentBankIndex, <>randomAction, <>playTrigger, <>playToggle, <>getPreset, <>loadPreset, <>fileAction;

	*new { |argMetaData, argDefName|
		^super.newCopyArgs.init(argMetaData,argDefName);
	}

	init { |argMetaData, argDefName|
		metaData = argMetaData;
		defName = argDefName;
		currentBankIndex = 0;
		currentPresetIndex = 0;
		if (metaData[\bankListItems].isNil) { metaData[\bankListItems] = Array.new };
		if (metaData[\presets].isNil) { metaData[\presets] = MultiLevelIdentityDictionary.new };

	}

	defName_ { |argDefName|
		gui[\defName].string = argDefName;
		defName = argDefName;
	}

	currentPresetIndex_ { |argCurrentPresetIndex|
		currentPresetIndex = argCurrentPresetIndex;
		gui[\presetMenu].value = argCurrentPresetIndex;
	}

	currentBankIndex_ { |argcurrentBankIndex|
		currentBankIndex = argcurrentBankIndex;
		gui[metaData].value = argcurrentBankIndex;
	}

	randomize { if (randomAction.notNil) { randomAction.value; } }

	makeGui { |parent, bounds|
		bounds = bounds.asRect;
		gui = ();
		gui[\canvas] = CompositeView.new(parent, parent.bounds.width@28);
		gui[\canvas].background = Color.green;

		gui[\playToggle] = Button(gui[\canvas], Rect(4,3,24,22))
		.states_([
			["P", Color.grey, Color.black],
			["S", Color.black, Color.yellow]])
		.action_({ arg butt; if (playToggle.notNil) { playToggle.value(butt.value)}});

		gui[\playTrigger] = Button(gui[\canvas], Rect(32,3,24,22))
		.states_([["T", Color.grey, Color.black]])
		.action_({ arg butt; if (playTrigger.notNil) { playTrigger.value }});

		gui[\bankMenu] = PopUpMenu(gui[\canvas],Rect(60,3,80,22))
		.items_(metaData[\bankListItems])
		.background_(Color.grey)
		.value_(currentBankIndex)
		.action_({ arg v;
			currentBankIndex = v.value;
			gui[\presetMenu].items_(metaData[asSymbol(metaData[\bankListItems][currentBankIndex] ++ "presetNameArray")]);
			gui[\presetMenu].value = 0;
		});
		gui[\bankMenuAddButton] = Button(gui[\canvas], Rect(144,3,10,22))
		.states_([["A", Color.grey, Color.black]])
		.action_({
			TypePresetName({ |string|
				var aSlot = string.asSymbol;
				metaData[\bankListItems] = metaData[\bankListItems].insert(currentBankIndex,aSlot);
				gui[\bankMenu].items_(metaData[\bankListItems]);
				metaData[asSymbol(string ++ "presetNameArray")] = Array.new;
				gui[\presetMenu].items = metaData[asSymbol(string ++ "presetNameArray")];
				if (fileAction.notNil) { fileAction.value(metaData) };
			 })
		});
		gui[\bankMenuRemoveButton] = Button(gui[\canvas], Rect(158,3,10,22))
		.states_([["X", Color.grey, Color.black]])
		.action_({ var slotName = metaData[\bankListItems].removeAt(currentBankIndex);
			metaData[\presets].removeAt(slotName);
			metaData[slotName ++ "presetNameArray"] = nil;
			currentBankIndex = if (currentBankIndex >= metaData[\bankListItems].size.postln)
			{
				metaData[\bankListItems].size - 1;
			} {
				currentBankIndex };
			gui[\bankMenu].items_(metaData[\bankListItems]);
			gui[\presetMenu].items_(metaData[asSymbol(metaData[\bankListItems][currentBankIndex] ++ "presetNameArray")]);
			gui[\presetMenu].value = 0;
			if (fileAction.notNil) { fileAction.value(metaData) };
		});
		gui[\presetMenu] = PopUpMenu(gui[\canvas],Rect(170,3,80,22))
		.items_(metaData[asSymbol(metaData[\bankListItems][currentBankIndex] ++ "presetNameArray")])
		.value_(currentPresetIndex)
		.background_(Color.grey)
		.action_({arg index;
			var preset;
			var pArrayName = asSymbol(metaData[\bankListItems][currentBankIndex] ++ "presetNameArray");
			currentPresetIndex = index.value;
			preset =
			metaData[\presets][
				metaData[\bankListItems][currentBankIndex],
				metaData[pArrayName][currentPresetIndex]];
			preset.postln;
			loadPreset.value(preset.copy);

		});

		gui[\presetMenuAddButton] = Button(gui[\canvas], Rect(254,3,10,22))
		.states_([["A", Color.grey, Color.black]])
		.action_({
			TypePresetName({
				|string|
				var pArrayName = asSymbol(metaData[\bankListItems][currentBankIndex] ++ "presetNameArray");
				var aSlot = string.asSymbol;
				var bankName = metaData[\bankListItems][currentBankIndex];
				metaData[pArrayName] = metaData[pArrayName].insert(currentPresetIndex.postln, aSlot);
				gui[\presetMenu].items_(metaData[pArrayName]);
				metaData[\presets][bankName, aSlot] = if (getPreset.notNil) { getPreset.value; };
				if (fileAction.notNil) { fileAction.value(metaData) };
			});
		});

		gui[\presetMenuRemoveButton] = Button(gui[\canvas], Rect(268,3,10,22))
		.states_([["X", Color.grey, Color.black]])
		.action_({
			var pArrayName = asSymbol(metaData[\bankListItems][currentBankIndex] ++ "presetNameArray");
			var bankName = metaData[\bankListItems][currentBankIndex];

			var presetName = metaData[pArrayName].removeAt(currentPresetIndex);
			gui[\presetMenu].items_(metaData[pArrayName]);
			currentPresetIndex = if (currentPresetIndex >= metaData[pArrayName].size)
			{
				metaData[pArrayName].size - 1;
			} {
				currentPresetIndex };
			metaData[\presets].removeAt(bankName, presetName);
			if (fileAction.notNil) { fileAction.value(metaData) };
		});

		gui[\randomMenuAddButton] = Button(gui[\canvas], Rect(282,3,14,22))
		.states_([["R", Color.yellow, Color.black]])
		.action_({ this.randomize });

		gui[\defName] = StaticText(gui[\canvas], Rect(310,3,96,22))
		.string_(defName)
		.font_(Font("Monaco"));
	}
}


EmbedGui {

	// Control Elements is an array with only the control elements!
	var <controlElements, <synthDefName, synthDesc, <>controlPanel, canvas, tempNames, tempToggleSynth, <>index, <>widthOffset;

	// first inialization and creation of a new instance
	*new { |argSynthDefName|
		^super.newCopyArgs.init(argSynthDefName);
	}
	// init all nescessary value
	init { |argSynthDefName|
		synthDefName = argSynthDefName;
		synthDesc = SynthDesc.readDef(synthDefName);

		widthOffset = 0;

		controlPanel = MBControlPanel.new(synthDesc.metadata, synthDefName);
		controlPanel.randomAction = {
			controlElements do: { |element|
				if (element.isKindOf(MBFader)) { element.value_(1.0.rand) }
			}
		};
		controlPanel.playTrigger = { this.oneShotPlay };
		controlPanel.playToggle = { |value| this.togglePlay(value) };
		controlPanel.getPreset = { var preset = ();
			preset[\volgorde] = Array.new;
			controlElements do: { |element|
				preset[\volgorde] = preset[\volgorde].add(element.name);
				preset[preset[\volgorde].last] = element.value;
			};
			preset.copy;
		};
		controlPanel.loadPreset = { |preset|
			var volgorde = preset[\volgorde].copy;
			preset[\volgorde] = nil;

			volgorde do: { |key, i| controlElements[i].value_(preset[key]) }; // maybe a value action in elements
		};

		controlPanel.fileAction = { |toStoreMetaData| SynthDesc.storeMetaData(synthDefName, toStoreMetaData); };

		controlElements = List.new;
		tempNames = synthDesc.controlNames;

		synthDesc.metadata[\noGui] do: { |key| tempNames.removeAt(tempNames.indexOfEqual(key)) };

		tempNames do: { |key|
			var checkSpec = key.asSpec.isKindOf(ControlSpec) || synthDesc.metadata[\specs][key].isKindOf(ControlSpec);
			if (checkSpec) {
				controlElements.add(MBFader.new(key,if(key.asSpec.isKindOf(ControlSpec)){key.asSpec}{synthDesc.metadata[\specs][key]}));
			} {
				controlElements.add(MBNumberBox.new(key));
			};
			controlElements.last.name = key;
		};
	}

	makeGui { |argParent|
		var parent;
		if (argParent.isNil) { parent = Window.new.front; } { parent = argParent };

		canvas = CompositeView(parent,parent.bounds.width - widthOffset@(tempNames.size * 28 + 32));
		canvas.addFlowLayout(0@0, 0@2);
		canvas.background_(Color.grey);

		controlPanel.makeGui(canvas,canvas.bounds.width@30);

		controlElements do: { |element|
			element.makeGui(canvas, canvas.bounds.width @ 22);
			CompositeView(canvas,canvas.bounds.width@2).background_(Color.rand);
		};
	}

	closeGui { canvas.remove }

	getParamValuesArray {
		var paramValuesArray = Array.new;
		controlElements do: { |obj|
			paramValuesArray = paramValuesArray.add(obj.name.asSymbol);
			paramValuesArray = paramValuesArray.add(
				if(obj.isKindOf(MBFader)) {
					obj.spec.map(obj.value)
				} {
					obj.value;
				}
			)
		};
		^paramValuesArray
	}

	oneShotPlay {
		fork {
			var tempSynth, indexOf, paramsValues = this.getParamValuesArray;
			if (tempSynth.notNil) { tempSynth.release; tempSynth = nil };
			tempSynth = Synth(synthDefName, paramsValues);
			Server.default.sync;
			indexOf = paramsValues.indexOf(\atk);
			if (indexOf.notNil) {
				fork { (paramsValues[indexOf + 1]).wait; tempSynth.set(\gate, 0); tempSynth = nil }
			} {
				tempSynth.set(\gate, 0); tempSynth = nil
			};
		}
	}

	togglePlay { |toggle|
		var paramsValues = this.getParamValuesArray;

		if (toggle > 0) {
			controlElements do: { |element| element.action = { |value| tempToggleSynth.set(element.name, value) } };
			if (tempToggleSynth.notNil) { tempToggleSynth.release; tempToggleSynth = nil };
			tempToggleSynth = Synth(synthDefName, paramsValues);
		} {
			tempToggleSynth.set(\gate, 0); tempToggleSynth = nil;
			controlElements do: { |element| element.action = nil; };
		};
	}
}

