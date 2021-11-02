/*
FILENAME: ServerControlView

DESCRIPTION: SimpleServerControlView

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
ServerControlView(bounds:400@50).front();
*/

ServerControlView : View {
	classvar cockpitStatePersistanceFile, cockpitStatePersistanceDir;
	var <mainLayout,<selectAudioDevices,<buttonReboot, currentDevice;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	*initClass {
		// This method is automatically evaluated during startup.
	    var cockpitStatePersistanceDir = Platform.userAppSupportDir ++ "/ExtensionsWorkdir/CaACLib/CockpitView/";
		File.mkdir(cockpitStatePersistanceDir); // Create if not exists.
		cockpitStatePersistanceFile = cockpitStatePersistanceDir ++ "scmcockpit.state";
	}

	initialize {
		this.loadState();
		this.initializeView();
		Server.default.reboot;
		this.onMove = { this.persistState(); };
	}

	initializeView {

		mainLayout = HLayout();
		this.deleteOnClose = false;
		this.layout = mainLayout;

		selectAudioDevices = PopUpMenu();
		selectAudioDevices.items = ServerOptions.devices;
		selectAudioDevices.action = { |sender| this.onDeviceSelected(sender); };
		ServerOptions.devices do: { |device, index| if (device == currentDevice, { selectAudioDevices.value = index; }); };

		mainLayout.add(selectAudioDevices);

		buttonReboot = Button();
		buttonReboot.string = "(re)boot";
		buttonReboot.action = { this.onDeviceReboot(); };

		mainLayout.add(buttonReboot);
	}

	onDeviceSelected { |sender|
		Server.local.options.device = sender.item;
		Server.local.reboot;
		currentDevice = sender.item;
		this.persistState();
	}

	onDeviceReboot {
		Server.default.reboot;
	}

	getState {
		var state = Dictionary();
		state[\type] = "CockpitView";
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
