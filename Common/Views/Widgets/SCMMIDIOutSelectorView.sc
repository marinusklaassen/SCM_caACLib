/*
FILENAME: PatternBoxProjectItemView

DESCRIPTION: THe PatternBoxProjectItemView the project item view which references to the projectview.

AUTHOR: Marinus Klaassen (2012, 2021Q4)

m = SCMMIDIOutSelectorView().front;
m.loadState(m.getState());
m.proxy.asStream.next;
*/

SCMMIDIOutSelectorView : SCMViewBase {
	var bufferpool, <proxy, <selectedBufferpoolItem, labelErrorMessage, popupMenuMIDIOuts, mainLayout, selectedMIDIOutName;
	classvar dependants, midiOuts;

	*initClass {
		midiOuts = Dictionary();
		dependants = Set();
		this.refresh();
	}

	*refresh {
		MIDIClient.init();
		midiOuts.clear();
		MIDIClient.destinations do: { |midiEndPoint|
			var endPointName = midiEndPoint.device + "-" + midiEndPoint.name;
			midiOuts[endPointName] = MIDIOut.newByName(midiEndPoint.device, midiEndPoint.name).latency_(Server.local.latency);
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
		selectedMIDIOutName = midiOuts.keys.asArray.sort.first;
		proxy.source = midiOuts[selectedMIDIOutName];
	}

	getProxies {
		var result = Dictionary();
		result[this.name.asSymbol] = proxy;
		^result;
	}

	update {
		var index;
		this.setErrorMessage(nil);
		popupMenuMIDIOuts.items = midiOuts.keys.asArray.sort;
		popupMenuMIDIOuts.items do: { |item, i| if( item == selectedMIDIOutName, { index = i; }); };
		if (index.isNil, {
			this.setErrorMessage("The required MIDIOut device % is not connected. Please connect the device and refresh MIDI state.".format(selectedMIDIOutName));
		}, {
			popupMenuMIDIOuts.valueAction = index;
		});

	}

	initializeView {
		mainLayout = VLayout();
		mainLayout.margins = 0!4;
		this.layout = mainLayout;

		popupMenuMIDIOuts = PopUpMenu();
		popupMenuMIDIOuts.items = midiOuts.keys.asArray.sort;
		popupMenuMIDIOuts.action = { |sender|
			selectedMIDIOutName = sender.item;
			proxy.source = midiOuts[selectedMIDIOutName]
		};
		mainLayout.add(popupMenuMIDIOuts);

		labelErrorMessage = StaticText();
		labelErrorMessage.visible = false;
		mainLayout.add(labelErrorMessage);
	}

	dispose {
		dependants.remove(this);
	}

	getState {
		var state = Dictionary();
		state[\selectedItem] = selectedMIDIOutName;
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
		selectedMIDIOutName = state[\selectedItem];
		popupMenuMIDIOuts.items do: { |item, i| if( item == state[\selectedItem] , { index = i; }); };
		if (index.notNil, {
			popupMenuMIDIOuts.valueAction =  index;
		},{
			this.setErrorMessage("The required MIDIOut device % is not connected. Please connect the device and refresh MIDI state.".format(selectedMIDIOutName));
		});
		^state;
	}
}
