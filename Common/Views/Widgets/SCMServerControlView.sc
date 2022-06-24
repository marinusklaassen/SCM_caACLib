/*
FILENAME: SCMServerControlView

DESCRIPTION: SimpleSCMServerControlView

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
SCMServerControlView(bounds:400@50).front();
*/

SCMServerControlView : View {
	classvar cockpitStatePersistanceFile, cockpitStatePersistanceDir, currentDevice, instances;
	var <mainLayout, <>selectAudioDevices, buttonReboot, buttonShowServerNodeGraph, buttonShowMeter, buttonPanic, buttonRefreshMIDI;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	*initClass {
		// This method is automatically evaluated during startup.
		var cockpitStatePersistanceDir = Platform.userAppSupportDir ++ "/ExtensionsWorkdir/SCM_CaACLib/" ++ this.class.asString ++ "/";
		File.mkdir(cockpitStatePersistanceDir); // Create if not exists.
		cockpitStatePersistanceFile = cockpitStatePersistanceDir ++ this.class.asString ++ ".state";
		instances = Set();
	}

	initialize {
		this.loadState();
		this.initializeView();
		if (Server.default.serverRunning.not, {
			Server.default.boot;
		});
		this.onMove = { if(this.parent.isNil, { this.persistState(); })};
		instances.add(this);
	}

	initializeView {

		mainLayout = GridLayout();
		this.layout = mainLayout;

		this.deleteOnClose = false;
		this.name = "SC Server & Runtime Control";

		buttonPanic = ButtonFactory.createInstance(this, class: "btn-warning", buttonString1: "panic");
		buttonPanic.action = { this.onButtonAction_Panic(); };

		mainLayout.addSpanning(buttonPanic, 0, 0, columnSpan: 4);

		selectAudioDevices = PopUpMenuFactory.createInstance(this);
		selectAudioDevices.items = ServerOptions.devices;
		selectAudioDevices.action = { |sender| this.onPopupAction_DeviceSelection(sender); };
		ServerOptions.devices do: { |device, index| if (device == currentDevice, { selectAudioDevices.value = index; }); };

		mainLayout.addSpanning(selectAudioDevices, 1, columnSpan: 4);

		buttonReboot = ButtonFactory.createInstance(this, class: "btn-primary", buttonString1: "(re)boot)");
		buttonReboot.action = { this.onDeviceReboot(); };

		mainLayout.add(buttonReboot, 2, 0);

		buttonShowServerNodeGraph = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "node graph");
		buttonShowServerNodeGraph.action = { this.onButtonAction_ShowServerNodeGraph(); };

		mainLayout.add(buttonShowServerNodeGraph, 2, 1);

		buttonShowMeter = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "meter");
		buttonShowMeter.action = { this.onButtonAction_ShowMeter(); };

		mainLayout.add(buttonShowMeter, 2, 2);

		buttonRefreshMIDI = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "MIDI refresh");
		buttonRefreshMIDI.action = { this.onButtonAction_RefreshMIDI(); };

		mainLayout.add(buttonRefreshMIDI, 2, 3);
	}

	onButtonAction_RefreshMIDI {
		MIDIOutSelectorView.refresh();
		MIDIIn.connectAll();
    }

    onButtonAction_Panic {
		CmdPeriod.run();
        Server.freeAll(evenRemote: false);
    }

	onButtonAction_ShowMeter {
		Server.local.meter();
	}

	onButtonAction_ShowServerNodeGraph {
		Server.local.plotTree();
	}

	onPopupAction_DeviceSelection { |sender|
		Server.local.options.device = sender.item;
		this.onDeviceReboot();
		currentDevice = sender.item;
		this.persistState();
		instances do: { |instance| instance.selectAudioDevices.value = sender.value };
	}

	onDeviceReboot {
		CmdPeriod.run;
	    Server.local.reboot;
	}

	getState {
		var state = Dictionary();
		state[\type] = this.class.asString;
		state[\windowBounds] = this.bounds;
		^state[\selectedAudioDevice] = currentDevice;
	}

    persistState {
		this.getState().writeArchive(cockpitStatePersistanceFile);
	}

	loadState {
		if (File.exists(cockpitStatePersistanceFile), {
			var state = Object.readArchive(cockpitStatePersistanceFile);
			this.bounds = state[\windowBounds];
			currentDevice = state[\selectedAudioDevice];
		});
	}
}
