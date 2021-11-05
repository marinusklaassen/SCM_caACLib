/*
FILENAME: TempoClockView

DESCRIPTION: TempoClockView

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
TempoClockView(bounds:200@50).front();
*/

TempoClockView : View {
	classvar persistanceFile, persistanceDir, <tempoInBeatsPerSeconds;
	var <mainLayout, labelTempoClock, numberboxTempoClock, buttonSetToDefault;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	*initClass {
		// This method is automatically evaluated during startup.
		var persistanceDir = Platform.userAppSupportDir ++ "/ExtensionsWorkdir/SCMcaACLib/";
		File.mkdir(persistanceDir); // Create if not exists.
		persistanceFile = persistanceDir ++ "TempoClockView.state";
	}

	initialize {
		this.loadState();
		this.setDefaultTempoClock(tempoInBeatsPerSeconds);
		this.initializeView();
		this.onMove = { if (this.parent.notNil, { this.persistState(); }); };
	}

	initializeView {

		mainLayout = HLayout();
		this.layout = mainLayout;

		this.deleteOnClose = false;
		this.name = "Default Tempo Clock";

		labelTempoClock = StaticText();
		labelTempoClock.string = "BPM";

		this.layout.add(labelTempoClock);

		numberboxTempoClock = NumberBox();
		numberboxTempoClock.value = tempoInBeatsPerSeconds;
		numberboxTempoClock.decimals = 0;
		numberboxTempoClock.action = { |sender| this.onClickAction_NumberBoxTempoClock(sender); };

		this.layout.add(numberboxTempoClock);

		buttonSetToDefault = Button();
		buttonSetToDefault.string = "Reset";
		buttonSetToDefault.action = { |sender| this.onClickAction_ButtonSetToDefault(sender); };

		this.layout.add(buttonSetToDefault);
	}

    onClickAction_ButtonSetToDefault { |sender|
		tempoInBeatsPerSeconds = 60;
		this.setDefaultTempoClock(60);
		numberboxTempoClock.value = 60;
		if (this.parent.notNil, { this.persistState(); });
	}

	onClickAction_NumberBoxTempoClock { |sender|
		tempoInBeatsPerSeconds = sender.value;
		this.setDefaultTempoClock(tempoInBeatsPerSeconds);
		if (this.parent.notNil, { this.persistState(); });
	}

	setDefaultTempoClock { |tempoInBeatsPerSeconds|
		TempoClock.default.tempo = tempoInBeatsPerSeconds / 60;
	}

	getState {
		var state = Dictionary();
		state[\type] = "CockpitView";
		state[\windowBounds] = this.bounds;
		^state[\tempoInBeatsPerSeconds] = tempoInBeatsPerSeconds;
	}

	persistState {
		this.getState().writeArchive(persistanceFile);
	}

	loadState {
		if (File.exists(persistanceFile), {
			var state = Object.readArchive(persistanceFile);
			this.bounds = state[\windowBounds];
			tempoInBeatsPerSeconds = state[\tempoInBeatsPerSeconds];
		});
		if (tempoInBeatsPerSeconds.isNil, { tempoInBeatsPerSeconds = 60;});
		this.setDefaultTempoClock(tempoInBeatsPerSeconds);
	}
}
