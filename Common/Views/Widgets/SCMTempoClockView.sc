/*
FILENAME: SCMTempoClockView

DESCRIPTION: SCMTempoClockView

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
SCMTempoClockView(bounds:100@50).front();
*/

SCMTempoClockView : View {
	classvar persistanceFile, persistanceDir, <tempoInBeatsPerSeconds;
	var <mainLayout, labelTempoClock, <quant=0, <>actionQuant, numberboxTempoClock, buttonSetToDefault, labelQuant, numberboxQuant;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	*initClass {
		// This method is automatically evaluated during startup.
		var persistanceDir = Platform.userAppSupportDir ++ "/ExtensionsWorkdir/SCM_CaACLib/" ++ this.class.asString ++ "/";
		File.mkdir(persistanceDir); // Create if not exists.
		persistanceFile = persistanceDir ++ this.class.asString + ".state";
	}

	initialize {
		this.loadState();
		this.setDefaultTempoClock(tempoInBeatsPerSeconds);
		this.initializeView();
		this.onMove = { if (this.parent.isNil, { this.persistState(); }); };
	}

	initializeView {

		mainLayout = HLayout();
		this.layout = mainLayout;

		this.deleteOnClose = false;
		this.name = "BPM";

		labelTempoClock = StaticTextFactory.createInstance(this);
		labelTempoClock.string = "Tempo [BPM]:";

		this.layout.add(labelTempoClock, align: \left);

		numberboxTempoClock = NumberBoxFactory.createInstance(this, class: "numberbox-secondary");
		numberboxTempoClock.value = tempoInBeatsPerSeconds;
		numberboxTempoClock.maxWidth = 50;
		numberboxTempoClock.decimals = 0;
		numberboxTempoClock.action = { |sender| this.onClickAction_NumberBoxTempoClock(sender); };

		this.layout.add(numberboxTempoClock);

		labelQuant = StaticTextFactory.createInstance(this);
		labelQuant.string = "Quant:";

		this.layout.add(labelQuant, align: \left);

		numberboxQuant = NumberBoxFactory.createInstance(this, class: "numberbox-secondary");
		numberboxQuant.value = quant;
		numberboxQuant.maxWidth = 50;
		numberboxQuant.decimals = 2;
		numberboxQuant.action = { |sender|
			quant=sender.value;
			if(actionQuant.notNil, {  actionQuant.value(this); });
			if (this.parent.notNil, { this.persistState(); });
		};
		this.layout.add(numberboxQuant);

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
		state[\quant] = numberboxQuant.value;
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
			quant = if(state[\quant].isNil, { 0; }, { state[\quant]; });
			if(actionQuant.notNil, {  actionQuant.value(this); });
		});
		if (tempoInBeatsPerSeconds.isNil, { tempoInBeatsPerSeconds = 60;});
		this.setDefaultTempoClock(tempoInBeatsPerSeconds);


	}
}
