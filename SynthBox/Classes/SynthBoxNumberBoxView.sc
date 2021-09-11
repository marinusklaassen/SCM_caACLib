/*
FILENAME: SynthBoxNumberView

DESCRIPTION: Simple numberbox with a lable

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
SynthBoxNumberView(bounds:400@50).front().action_({ |sender| sender.postln });
*/

SynthBoxNumberView : View {
	var <name, <value;
	var <mainLayout, <labelName, <numberBox;

	*new { |name, parent, bounds|
		^super.new(parent,bounds).initialize(name);
	}

	initialize { |argName|
		this.initializeView();
		this.name = argName;
		value = 0;
	}

	initializeView {
		mainLayout = HLayout();
		this.layout = mainLayout;
		labelName = StaticText();
		labelName.string_(name);
		mainLayout.add(labelName);

		numberBox = NumberBox();
		numberBox.action_({ |num| if (action.notNil) { action.value(this); value = num.value;  } });
		mainLayout.add(numberBox, stretch: 1);
	}

	value_ {|argValue|
		numberBox.value = argValue;
		value = argValue;
		if (action.notNil) { action.value(this); };
	}

	name_ {|argName|
		name = argName;
		labelName.string_(name);
	}

	getState  {
		var state = Dictionary();
		state[\name] = this.name;
		state[\value] = this.value;
		^state;
	}

	loadState { |state|
		this.name = state[\name];
		this.value = state[\value];
	}
}