/*
FILENAME: SyntBoxControlPanelView

DESCRIPTION: SyntBoxControlPanelView toplevel synthbox control panel

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
SynthBoxControlPanelView(\default).front
*/

SynthBoxControlPanelView : View {
	var <>metaData, <gui, <defName, <>randomAction, <>playTrigger, <>playToggle;
	var <mainLayout, <togglePlay, <buttonOneshot, <buttonRandomize, <labelSynthName;

	*new { |argDefName, parent, bounds|
		^super.new(parent, bounds).initialize(argDefName,argDefName);
	}

	initialize { |argDefName|

		defName = argDefName;
		this.initializeView();
	}

	defName_ { |argDefName|
		gui[\defName].string = argDefName;
		defName = argDefName;
	}

	randomize { if (randomAction.notNil) { randomAction.value; } }

	initializeView {
		mainLayout = HLayout();
		this.layout = mainLayout;
		this.background = Color.green;

		togglePlay= Button()
		.states_([
			["PLAY", Color.grey, Color.black],
			["STOP", Color.black, Color.red]])
		.action_({ arg sender; if (playToggle.notNil) { playToggle.value(sender.value)}});

		mainLayout.add(togglePlay);

		buttonOneshot = Button()
		.states_([["SHOOT", Color.grey, Color.black]])
		.action_({ arg butt; if (playTrigger.notNil) { playTrigger.value }});
		mainLayout.add(buttonOneshot);

		buttonRandomize = Button()
		.states_([["RANDOMIZE", Color.green, Color.black]])
		.action_({ this.randomize(); });
		mainLayout.add(buttonRandomize);

		labelSynthName= StaticText()
		.align_(\center)
		.string_(defName)
		.font_(Font("Monaco"));
		mainLayout.add(labelSynthName);
	}
}
