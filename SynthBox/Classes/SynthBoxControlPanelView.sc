/*
FILENAME: SyntBoxControlPanelView

DESCRIPTION: SyntBoxControlPanelView toplevel synthbox control panel

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
SynthBoxControlPanelView(\default).front
*/

SynthBoxControlPanelView : View {
	var <>metaData, <gui, <defName, <>randomAction, <>playTrigger, <>playToggle;
	var <mainLayout, <togglePlay, <buttonOneshot, <buttonRandomize;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize();
	}

	initialize {
		this.initializeView();
	}

	randomize { if (randomAction.notNil) { randomAction.value; } }

	initializeView {
		mainLayout = HLayout();
		this.layout = mainLayout;

		togglePlay= ButtonFactory.createInstance(this, class: "btn-toggle", buttonString1: "PLAY", buttonString2: "STOP")
		.action_({ arg sender; if (playToggle.notNil) { playToggle.value(sender.value)}});

		mainLayout.add(togglePlay);

		buttonOneshot = ButtonFactory.createInstance(this, class: "btn-normal", buttonString1: "ONESHOT")
		.action_({ arg butt; if (playTrigger.notNil) { playTrigger.value }});

		mainLayout.add(buttonOneshot);

		buttonRandomize = ButtonFactory.createInstance(this, class: "btn-secondary", buttonString1: "RANDOMIZE")
		.action_({ this.randomize(); });

		mainLayout.add(buttonRandomize);
	}
}
