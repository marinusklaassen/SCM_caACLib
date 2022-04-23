/*
FILENAME: PresetView

DESCRIPTION: Preset management view, CRUD with immediate automatic file storage.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
PresetView(bounds:500@50, contextId: "mycontext").front().actionFetchPreset_({ 1000.rand.postln; }).actionLoadPreset_({ |preset| preset.postln });
*/

PresetView : View {
	classvar eventPresetsChanged, <>nameToPositionMap, presets, presetNamesSorted, presetPersistanceDir;
	var mainLayout, layoutControls, notificationMessageLabel, <popupPresetSelector, <buttonPresetNext, <buttonPresetPrevious, buttonPresetLoad, buttonPresetUpdate, buttonPresetCreate, buttonPresetDelete, textPresetName;
	var <>actionFetchPreset, <>actionLoadPreset, <>contextId, <>name, presetPersistanceFile, eventHandlerPresetsChanged;

	*new { | contextId, parent, bounds  |
		if (contextId.isNil) {
			Error("Constructor argument contextId is mandatory").throw();
		};
		^super.new(parent, bounds).initialize(contextId);
	}

	*initClass {
		// This method is automatically evaluated during startup.
		presetPersistanceDir = Platform.userAppSupportDir ++ "/ExtensionsWorkdir/SCM_CaACLib/" ++ this.class.asString ++ "/";
		File.mkdir(presetPersistanceDir); // Create if not exists.
		presets = Dictionary();
		nameToPositionMap = Dictionary();
		presetNamesSorted = Dictionary();
		eventPresetsChanged = Dictionary();
	}

	initialize { |contextId|
		this.contextId = contextId;
		if (presets[this.contextId].isNil) {
			this.initalizeContext();
		};
		eventHandlerPresetsChanged = { | event, sender |
			this.onPresetsChanged(sender);
		};
		// Register handler to event.
		eventPresetsChanged[this.contextId].addDependant(eventHandlerPresetsChanged);
		// Compose preset file for context ID once. Context ID should only be provided during instance construction.
		presetPersistanceFile = presetPersistanceDir ++ this.contextId ++ ".presets";
		this.initializeView();
		this.loadData();
	}

	initalizeContext{
		presets[this.contextId] = Dictionary();
		nameToPositionMap[this.contextId] = Dictionary();
		presetNamesSorted[this.contextId] = [];
		eventPresetsChanged[this.contextId] = ();
	}

	initializeView{
		mainLayout = VLayout();
		mainLayout.margins = 0!4;
		this.layout = mainLayout;

		layoutControls = HLayout();
		layoutControls.margins = 0!4;
		mainLayout.add(layoutControls);

		notificationMessageLabel = MessageLabelViewFactory.createInstance(this, class: "message-info");
		mainLayout.add(notificationMessageLabel);

		popupPresetSelector = PopUpMenuFactory.createInstance(this);
		popupPresetSelector.action = { |menu|
			name = menu.item;
			textPresetName.string = menu.item;
		};

		popupPresetSelector.minWidth = 150;
		popupPresetSelector.maxWidth = 150;
		layoutControls.add(popupPresetSelector);

		buttonPresetNext = ButtonFactory.createInstance(this, class: "btn-next");

		buttonPresetNext.action = {
			if (popupPresetSelector.items.size > 0, {
				popupPresetSelector.valueAction = popupPresetSelector.value - 1 %  popupPresetSelector.items.size;
			});
		};

		layoutControls.add(buttonPresetNext);

		buttonPresetPrevious = buttonPresetNext = ButtonFactory.createInstance(this, class: "btn-previous");
		buttonPresetPrevious.action = {
			if (popupPresetSelector.items.size > 0, {
				popupPresetSelector.valueAction = popupPresetSelector.value - 1 %  popupPresetSelector.items.size;
			});
		};

		layoutControls.add(buttonPresetPrevious);

		textPresetName = TextFieldFactory.createInstance(this);

		layoutControls.add(textPresetName);

		buttonPresetLoad = ButtonFactory.createInstance(this, class: "btn-success", buttonString1: "load");
		buttonPresetLoad.action = { |sender|
			if (popupPresetSelector.item.notNil, {
				actionLoadPreset.value(presets[contextId][popupPresetSelector.item]);
				name = popupPresetSelector.item;
				textPresetName.string = name;
				notificationMessageLabel.notify("Preset is loaded.");
			});
		};

		layoutControls.add(buttonPresetLoad);

		buttonPresetCreate = ButtonFactory.createInstance(this, class: "btn-success", buttonString1: "create");
		buttonPresetCreate.action = {
			this.createPreset(textPresetName.string);
			notificationMessageLabel.notify("Preset is created.");
		};
		layoutControls.add(buttonPresetCreate);

		buttonPresetUpdate = ButtonFactory.createInstance(this, class: "btn-warning", buttonString1: "update");
		buttonPresetUpdate.action = {
			this.updatePreset(textPresetName.string);
			notificationMessageLabel.notify("Preset is updated.");
		};

		layoutControls.add(buttonPresetUpdate);

		buttonPresetDelete =  ButtonFactory.createInstance(this, class: "btn-danger", buttonString1: "delete");
		buttonPresetDelete.action = {
			this.deletePreset(popupPresetSelector.item);
			notificationMessageLabel.notify("Preset is deleted.");
		};

		layoutControls.add(buttonPresetDelete);
	}

	sortPresetContext {
		var tempSortList = List();
		nameToPositionMap[this.contextId].clear();
		presets[this.contextId].asSortedArray() do: { |keyValuePair, position|
			tempSortList.add(keyValuePair[0]);
			nameToPositionMap[this.contextId][keyValuePair[0]] = position;
		};
		presetNamesSorted[this.contextId] = tempSortList.asArray();
	}

	onPresetsChanged {
		var position = nameToPositionMap[contextId][this.name];
		popupPresetSelector.items = presetNamesSorted[this.contextId];
		if (position.notNil, {
			popupPresetSelector.value = position;
		},{
			this.name = nil;
			textPresetName.string = "";
		});
	}

	persistData {
		presets[this.contextId].writeArchive(presetPersistanceFile);
	}

	loadData {
		if (File.exists(presetPersistanceFile), {
			presets[this.contextId] = Object.readArchive(presetPersistanceFile);
			this.sortPresetContext();
			popupPresetSelector.items = presetNamesSorted[this.contextId];
		});
	}

	createPreset { |name|
		if (name.size > 0, {
			// name must be entered.
			if (presets[this.contextId][name].isNil, {
				presets[this.contextId][name] = actionFetchPreset.value();
				this.name = name;
				this.persistData(this.contextId);
				this.sortPresetContext();
				eventPresetsChanged[contextId].changed();
			}, {
				notificationMessageLabel.notify("Preset name is already taken.");
			});
		});
	}

	updatePreset { |newName |
		if (newName.size > 0, {
			if (newName == this.name || presets[this.contextId][newName].isNil, {
				presets[contextId][newName] = actionFetchPreset.value();
				if (newName != this.name, {
					presets[contextId][this.name] = nil;
				});
				this.sortPresetContext();
				this.persistData(this.contextId);
				this.name = newName;
				eventPresetsChanged[contextId].changed();
			}, {
				notificationMessageLabel.notify("Preset name is already taken.");
			});
		});
	}

	deletePreset { |name|
		presets[contextId][name] = nil;
		this.sortPresetContext();
		popupPresetSelector.items = presetNamesSorted[this.contextId];
		this.persistData(this.contextId);
		eventPresetsChanged[contextId].changed();
	}

	dispose {
		eventPresetsChanged[this.contextId].removeDependant(eventHandlerPresetsChanged);
	}

	getState {
		var state = Dictionary();
		state[\type] = this.class.asString;
		state[\name] = name;
	}

	loadState { |state|
		name = state[\selectedName];
		this.onPresetsChanged();
	}

}
