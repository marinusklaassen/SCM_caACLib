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

PresetView(bounds:500@50, contextId: "mycontext").front().actionFetchPreset_({ 1000.rand.postln; }).actionLoadPreset_({ |preset| preset.postln });
*/


PresetView : View {
    classvar eventPresetsChanged, <>nameToPositionMap, presets, presetNamesSorted, presetPersistanceDir;
    var mainLayout, layoutControls, textNotification, <popupPresetSelector, <buttonPresetNext, <buttonPresetPrevious, buttonPresetLoad, buttonPresetStore, buttonPresetCreate, buttonPresetDelete, textPresetName;
    var <>actionFetchPreset, <>actionLoadPreset, <>contextId, <>name, presetPersistanceFile, eventHandlerPresetsChanged;

    *new { | parent, bounds, contextId |
        ^super.new(parent, bounds).initialize(contextId);
    }

    *initClass {
        // This method is automatically evaluated during startup.
        presetPersistanceDir = Platform.userAppSupportDir ++ "/ExtensionsWorkdir/CaACLib/PresetView/";
        File.mkdir(presetPersistanceDir); // Create if not exists.
        presets = Dictionary();
        nameToPositionMap = Dictionary();
        presetNamesSorted = Dictionary();
        eventPresetsChanged = Dictionary();
    }

    initialize { |contextId|

        if (contextId.isNil) {
            Error("Constructor argument contextId is mandatory").throw();
        };
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

    setNotification { | message |
        textNotification.string = message;
        textNotification.visible = true;
        fork {
            2.wait;
            {
                textNotification.string = "";
                textNotification.visible = false;
            }.defer;
        }
    }

    initializeView{
        mainLayout = VLayout();
        mainLayout.margins = 0!4;
        this.layout = mainLayout;

        layoutControls = HLayout();
        layoutControls.margins = 0!4;
        mainLayout.add(layoutControls);

        textNotification = StaticText();
        textNotification.visible = false;
        mainLayout.add(textNotification);

        popupPresetSelector = PopUpMenu();
        popupPresetSelector.background = Color.black.alpha_(0.8);
        popupPresetSelector.stringColor = Color.red;
        popupPresetSelector.action = { |menu|
            name = menu.item;
            textPresetName.string = menu.item;
        };
        popupPresetSelector.minWidth = 150;
        popupPresetSelector.maxWidth = 150;
        layoutControls.add(popupPresetSelector);

        buttonPresetNext = Button();
        buttonPresetNext.states = [["-", Color.red, Color.new255(189, 183, 107)]];
        buttonPresetNext.action = {
            popupPresetSelector.valueAction = popupPresetSelector.value - 1 %  popupPresetSelector.items.size;
        };
        buttonPresetNext.maxWidth = 20;

        layoutControls.add(buttonPresetNext);

        buttonPresetPrevious = Button();
        buttonPresetPrevious.states = [["+", Color.red, Color.new255(189, 183, 107)]];
        buttonPresetPrevious.action = {
            popupPresetSelector.valueAction = popupPresetSelector.value - 1 %  popupPresetSelector.items.size;
        };
        buttonPresetPrevious.maxWidth = 20;

        layoutControls.add(buttonPresetPrevious);

        textPresetName = TextField();

        layoutControls.add(textPresetName);

        buttonPresetLoad = Button();
        buttonPresetLoad.states = [["load", Color.red, Color.black.alpha_(0.8)]];
        buttonPresetLoad.action = { |sender|
            if (popupPresetSelector.item.notNil, {
                actionLoadPreset.value(presets[contextId][popupPresetSelector.item]);
                name = popupPresetSelector.item;
                textPresetName.string = name;
            });
        };
        buttonPresetLoad.maxWidth = 50;
        buttonPresetLoad.minWidth = 50;

        layoutControls.add(buttonPresetLoad);

        buttonPresetStore = Button();
        buttonPresetStore.states = [["update", Color.red, Color.black.alpha_(0.8)]];
        buttonPresetStore.action = {
            this.updatePreset(textPresetName.string);
        };
        buttonPresetStore.maxWidth = 50;
        buttonPresetStore.minWidth = 50;

        layoutControls.add(buttonPresetStore);

        buttonPresetCreate = Button();
        buttonPresetCreate.states = [["create", Color.red, Color.black.alpha_(0.8)]];
        buttonPresetCreate.action = {
            this.createPreset(textPresetName.string);
        };
        buttonPresetCreate.maxWidth = 50;
        buttonPresetCreate.minWidth = 50;

        layoutControls.add(buttonPresetCreate);

        buttonPresetDelete = Button();
        buttonPresetDelete.states = [["delete", Color.red, Color.black.alpha_(0.8)]];
        buttonPresetDelete.action = { this.deletePreset(popupPresetSelector.item);  };
        buttonPresetDelete.maxWidth = 50;
        buttonPresetDelete.minWidth = 50;

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
                this.setNotification("Preset name is already taken.");
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
                this.setNotification("Preset name is already taken.");
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
        state[\type] = "PresetView";
        state[\name] = name;
    }

    loadState { |state|
        name = state[\selectedName];
        this.onPresetsChanged();
    }

}
