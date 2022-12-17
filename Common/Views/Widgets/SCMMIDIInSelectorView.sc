/*
FILENAME: SCMMIDIInSelectorView

DESCRIPTION: Select a MIDIIn source

AUTHOR: Marinus Klaassen (2012, 2021Q4)
*/

SCMMIDIInSelectorView : SCMViewBase {
	var <proxy, labelErrorMessage, popupMenuMIDIIns, mainLayout, selectedMIDIInName, <midiIn;
	classvar dependants, midiIns;

	*initClass {
		midiIns = Dictionary();
		dependants = Set();
		this.refresh();
	}

	*refresh {
		MIDIIn.connectAll();
		MIDIClient.init();
		midiIns.clear();
		MIDIClient.sources do: { |midiEndPoint|
			var endPointName = midiEndPoint.device + "-" + midiEndPoint.name;
			midiIns[endPointName] = midiEndPoint;
			midiEndPoint.uid.postln;
		};
		dependants do: { |dependant| dependant.update(); };
	}

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize().initializeView();
	}

	initialize {
		this.needsControlSpec = false;
		proxy = PatternProxy();
		dependants.add(this);
		selectedMIDIInName = midiIns.keys.asArray.sort.first;
		proxy.source = midiIns[selectedMIDIInName];
        midiIn = midiIns[selectedMIDIInName];
	}

	getProxies {
		var result = Dictionary();
		result[this.name.asSymbol] = proxy;
		^result;
	}

	update {
		var index;
		this.setErrorMessage(nil);
		popupMenuMIDIIns.items = midiIns.keys.asArray.sort;
		popupMenuMIDIIns.items do: { |item, i| if( item == selectedMIDIInName, { index = i; }); };
		if (index.isNil, {
			this.setErrorMessage("The required MIDIIn device % is not connected. Please connect the device and refresh MIDI state.".format(selectedMIDIInName));
		}, {
			popupMenuMIDIIns.valueAction = index;
		});
	}

	initializeView {
		mainLayout = VLayout();
		mainLayout.margins = 0!4;
		this.layout = mainLayout;

		popupMenuMIDIIns = PopUpMenu();
		popupMenuMIDIIns.items = midiIns.keys.asArray.sort;
		popupMenuMIDIIns.action = { |sender|
			selectedMIDIInName = sender.item;
			proxy.source = midiIns[selectedMIDIInName];
			midiIn = midiIns[selectedMIDIInName];
            if (this.action.notNil, { action.value(this); });
		};
		mainLayout.add(popupMenuMIDIIns);

		labelErrorMessage = StaticText();
		labelErrorMessage.visible = false;
		mainLayout.add(labelErrorMessage);
	}

	dispose {
		dependants.remove(this);
	}

	getState {
		var state = Dictionary();
		state[\selectedItem] = selectedMIDIInName;
		^state;
	}

	setErrorMessage { |msg|
		if (msg.notNil, {
			postln(msg);
			labelErrorMessage.visible = true;
			labelErrorMessage.string = msg;
		},{
			labelErrorMessage.visible = false;
		});
	}

	loadState { |state|
		var index;
		this.setErrorMessage(nil);
		selectedMIDIInName = state[\selectedItem];
		popupMenuMIDIIns.items do: { |item, i| if( item == state[\selectedItem] , { index = i; }); };
		if (index.notNil, {
			popupMenuMIDIIns.valueAction =  index;
		},{
			this.setErrorMessage("The required MIDIIn device % is not connected. Please connect the device and refresh MIDI state.".format(selectedMIDIInName));
		});
		^state;
	}
}
