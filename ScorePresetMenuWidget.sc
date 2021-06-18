// How to get all the preset stored in a score??
// How to load and update preset from disk in a score??
// Make a new model for this based on an object orientated way.

ScorePresetMenuWidget : ScoreWidgetBase {
	var <canvas, <>presets, <currentPresetIndex, <presetMenuItems, <>storeAction;

	currentPresetIndex_ { |argCurrentPresetIndex|
		currentPresetIndex = argCurrentPresetIndex;
		if (gui.notNil) { gui[\presetMenu].value = currentPresetIndex };
		if (action.notNil) { action.value(presets[presetMenuItems[currentPresetIndex]]) };
	}

	presetMenuItems_ { |argPresetMenuItems|
		presetMenuItems = argPresetMenuItems;
		if (gui.notNil) { gui[\presetMenu].items_(presetMenuItems).value_(currentPresetIndex); }
	}

	*new { ^super.new.init }

	init { presets = IdentityDictionary.new; gui = IdentityDictionary(); }

	makeGui { |parent, argBounds|
		var width, height, bounds = argBounds.asRect;
		width = bounds.width - 20; height = bounds.height - 8;
		canvas = CompositeView(parent, bounds)
		.background_(Color.new255(0,0,0,0));
		// .addFlowLayout(0@0, 0@2); // TODO alternative
		canvas.decorator = FlowLayout(canvas.bounds);

		gui[\gui] = ();
		gui[\presetMenu] = PopUpMenu(canvas, 37/160 * width @ height)
		.background_(Color.black.alpha_(0.8))
		.stringColor_(Color.red)
		.items_(presetMenuItems)
		.value_(currentPresetIndex)
		.action_({arg val; this.currentPresetIndex = val.value; });

		["+", "-", "recall", "store", "replace","delete"] collect: { |name, i|
			gui[(name ++ "Button").asSymbol] =
			Button(canvas, if(i < 2, { 10 / 160 * width }, { 25 / 160 * width }) @ height)
			.states_([[name,Color.red, if(i < 2, { Color.new255(189, 183, 107) }, { Color.black.alpha_(0.8) })]])
			.action_(
				[
					{ this.currentPresetIndex = currentPresetIndex + 1 % presetMenuItems.size; },
					{ this.currentPresetIndex = currentPresetIndex - 1 % presetMenuItems.size; },
					{ this.currentPresetIndex = currentPresetIndex }, {
						PresetNameDialog({ |name|
							presetMenuItems = presetMenuItems.insert(currentPresetIndex, name);
							gui[\presetMenu].items_(presetMenuItems);
							gui[\presetMenu].value_(currentPresetIndex);
							if (storeAction.notNil) { presets[name] = storeAction.value };
					}) }, {
						PresetNameDialog({ |name|
							presetMenuItems[currentPresetIndex] = name;
							gui[\presetMenu].items_(presetMenuItems);
							gui[\presetMenu].value_(currentPresetIndex);
							if (storeAction.notNil) { presets[name] = storeAction.value };
					}) }, {
						var presetName = presetMenuItems.removeAt(currentPresetIndex);
						gui[\presetMenu].items_(presetMenuItems);

						currentPresetIndex = if (currentPresetIndex >= presetMenuItems.size)
						{ presetMenuItems.size - 1; }
						{ currentPresetIndex };

						presets[presetName] = nil;
						gui[\presetMenu].value = currentPresetIndex;
			}][i])
		};
	}
}
