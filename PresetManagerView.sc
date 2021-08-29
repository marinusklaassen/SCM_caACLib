/*
 * FILENAME: PresetManagerView
 *
 * DESCRIPTION:
 *         Reusable PresetManagerView.
 *         - store, read presets with file IO
 *         - multiple layout possibilities
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 *

PresetManagerView(bounds: 600@50).front(); // Geef een bug qua dubbele layout. Later een bug fix indienen.
*/

PresetManagerView : View {
	var mainLayout, <popupPresetSelector, <buttonPresetNext, <buttonPresetPrevious, buttonPresetLoad, buttonPresetStore, buttonPresetNew, buttonPresetDelete, textPresetName;
	var <presets, <selectedPresetIndex, <presetMenuItems, <>storeAction, <>loadAction;

	*new { | parent, bounds |
			^super.new(parent, bounds).initialize();
		}

	initialize {
		presets = List();
		presets.add("FM Weird");
		presets.add("Grain Turbo");
		presets.add("Stochastic Megadrive Noise!");
		selectedPresetIndex = 0;
	 	this.initializeView();
	}

	initializeView{
		mainLayout = HLayout();
		mainLayout.margins = 0!4;
		this.layout = mainLayout;

		popupPresetSelector = PopUpMenu();
		popupPresetSelector.background = Color.black.alpha_(0.8);
		popupPresetSelector.stringColor = Color.red;
		popupPresetSelector.items = presets.asArray();
		popupPresetSelector.value = selectedPresetIndex;
		popupPresetSelector.action = { |menu| [menu.value, menu.item].postln;  textPresetName.string = menu.item; };
		popupPresetSelector.minWidth = 150;
		popupPresetSelector.maxWidth = 150;
		mainLayout.add(popupPresetSelector);

		buttonPresetNext = Button();
		buttonPresetNext.states = [["-", Color.red, Color.new255(189, 183, 107)]];
		buttonPresetNext.action = { "previous preset".postln; textPresetName.value.postln; };
		buttonPresetNext.maxWidth = 20;

		mainLayout.add(buttonPresetNext);

		buttonPresetPrevious = Button();
		buttonPresetPrevious.states = [["+", Color.red, Color.new255(189, 183, 107)]];
		buttonPresetPrevious.action = { "next preset".postln; textPresetName.value.postln;  };
        buttonPresetPrevious.maxWidth = 20;

		mainLayout.add(buttonPresetPrevious);

	    textPresetName = TextField();
		textPresetName.action = { | val | val.value.postln; };

	    mainLayout.add(textPresetName);

		buttonPresetLoad = Button();
		buttonPresetLoad.states = [["load", Color.red, Color.black.alpha_(0.8)]];
		buttonPresetLoad.action = { "load preset".postln; textPresetName.value.postln;  };
		buttonPresetLoad.maxWidth = 50;
		buttonPresetLoad.minWidth = 50;

		mainLayout.add(buttonPresetLoad);

		buttonPresetStore = Button();
		buttonPresetStore.states = [["store", Color.red, Color.black.alpha_(0.8)]];
		buttonPresetStore.action = { "store preset".postln; };
		buttonPresetStore.maxWidth = 50;
		buttonPresetStore.minWidth = 50;

	    mainLayout.add(buttonPresetStore);

		buttonPresetNew = Button();
		buttonPresetNew.states = [["new", Color.red, Color.black.alpha_(0.8)]];
		buttonPresetNew.action = { "new preset".postln; textPresetName.value.postln; };
		buttonPresetNew.maxWidth = 50;
			buttonPresetNew.minWidth = 50;

	    mainLayout.add(buttonPresetNew);

		buttonPresetDelete = Button();
		buttonPresetDelete.states = [["delete", Color.red, Color.black.alpha_(0.8)]];
		buttonPresetDelete.action = { "delete preset".postln; textPresetName.value.postln;  };
		buttonPresetDelete.maxWidth = 50;
		buttonPresetDelete.minWidth = 50;


	    mainLayout.add(buttonPresetDelete);
	}
}
