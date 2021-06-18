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
