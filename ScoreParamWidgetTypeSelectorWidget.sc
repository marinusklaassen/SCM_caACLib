/*
 * Marinus Klaassen 2021
 *
 * Select param controller widgets types.
 */

ScoreParamWidgetTypeSelectorWidget : ScoreWidgetBase {
	var >rangeAction, >faderAction, <>spec, <lemur, <>ezLemurInstance, tempDependant, tempLemurDependant, <name;
	var <pagename, <objectReferenceName, <setValueFunction;

	loadState { |preset|
		setValueFunction[\fader].value(preset[\fader]);
		setValueFunction[\range].value(preset[\range]);
	}

	init {
		spec = ControlSpec();

		model = (fader: 0, range: [0, 1]);

		setValueFunction = ();
		setValueFunction[\fader] = { |value|
			model[\fader] = value;
			model.changed(\fader, value);
		};
		setValueFunction[\range] = { |value|
			model[\range] = value;
			model.changed(\range, value);
		};

		dependants = ();

		dependants[\faderAction] = {|theChanger, what, val|
			if (what == \fader) {
				if (faderAction.notNil) { faderAction.value(spec.map(val)) };
			};
		};

		dependants[\rangeAction] = {|theChanger, what, val|
			if (what == \range) {
				if (rangeAction.notNil) { rangeAction.value(spec.map(val)) };
			};
		};


		model.addDependant(dependants[\faderAction]);
		model.addDependant(dependants[\rangeAction]);
	}

	selectElement { |objectType|

		gui do: { |widget| widget.remove; gui[widget] = nil };
		if (tempDependant.notNil) { model.removeDependant(tempDependant); tempDependant = nil; };
		if (tempLemurDependant.notNil) { model.removeDependant(tempLemurDependant) };
		if (ezLemurInstance.notNil) { ezLemurInstance.remove };

		case { objectType == "Fader" } {
			if (gui.notNil) {
				gui[\fader] = Slider(canvas, bounds.extent)
				.value_(model[\fader])
				.action_({|val|setValueFunction[\fader].value(val.value)});
				tempDependant = {|theChanger, what, val|
					if (what == \fader) { { gui[\fader].value = val }.defer;  };
				};
				model.addDependant(tempDependant);
			};
			if (ezLemurInstance.notNil) {

				ezLemurInstance.makeGui(objectType, model[\fader]);
				ezLemurInstance.action = {|val| setValueFunction[\fader].value(val.first) };
				tempLemurDependant = {|theChanger, what, val|

					if (what == \fader) { ezLemurInstance.value = val; };
				};

				model.addDependant(tempLemurDependant);
			};
		} { objectType =="Range" } {
			if (gui.notNil) {
				gui[\range] = RangeSlider(canvas, bounds.extent)
				.lo_(model[\range][0]).hi_(model[\range][1])
				.action_({|val|setValueFunction[\range].value([val.lo,val.hi])});
				tempDependant = {|theChanger, what, val|
					if (what == \range) { { gui[\range].lo_(val[0]).hi_(model[\range][1]) }.defer; };
				};
				model.addDependant(tempDependant);
			};
			if (ezLemurInstance.notNil) {
				ezLemurInstance.makeGui(objectType, model[\range]);
				ezLemurInstance.action_({|val|setValueFunction[\range].value(val)});
				tempLemurDependant = {|theChanger, what, val|
					if (what == \range) { ezLemurInstance.value = val };
				};
				model.addDependant(tempLemurDependant);
			};
		};
	}

	makeGui {  |argParent, argBounds |
		parent = argParent;
		bounds = argBounds.asRect;
		gui = ();
		canvas = CompositeView(parent,bounds)
		.background_(Color.blue.alpha_(0.5));
	}

	close {
		this.closeLemur;
		this.closeGui;
		model.dependants do: { |i| model.removeDependant(i) };
	}

	closeGui {
		canvas.remove;
		gui do: (_.remove); gui = nil;
		if (tempDependant.notNil) { model.removeDependant(tempDependant); tempDependant = nil; };
	}

	restoreGui {  |argParent, argBounds, objectType |
		parent = argParent;
		bounds = argBounds.asRect;
		gui = ();
		canvas = CompositeView(parent,bounds)
		.background_(Color.blue.alpha_(0.5));

		case { objectType == "Fader" } {
			if (gui.notNil) {
				gui[\fader] = Slider(canvas, bounds.extent)
				.value_(model[\fader])
				.action_({|val|setValueFunction[\fader].value(val.value)});
				tempDependant = {|theChanger, what, val|
					if (what.postln == \fader) { { gui[\fader].value = val }.defer;  };
				};
				model.addDependant(tempDependant);
			};

		} { objectType =="Range" } {
			if (gui.notNil) {
				gui[\range] = RangeSlider(canvas, bounds.extent)
				.lo_(model[\range][0]).hi_(model[\range][1])
				.action_({|val|setValueFunction[\range].value([val.lo,val.hi])});
				tempDependant = {|theChanger, what, val|
					if (what == \range) { { gui[\range].lo_(val[0]).hi_(model[\range][1]) }.defer; };
				};
				model.addDependant(tempDependant);
			};

		};
	}

	moveLemur { |argxOffset| ezLemurInstance.move(argxOffset); }

	pagename_ { |argPagename|
		pagename = argPagename;
		if (ezLemurInstance.notNil) { ezLemurInstance.pagename = argPagename; };
	}

	objectReferenceName_ { |argObjectReferenceName|
		objectReferenceName = argObjectReferenceName;
		if (ezLemurInstance.notNil) { ezLemurInstance.objectReferenceName = objectReferenceName; };
	}

	name_ { |argName|
		name = argName;
		if (ezLemurInstance.notNil) { ezLemurInstance.name = name; };
	}

	initLemur {  |argLemur, xoffset|
		if (argLemur.notNil) { lemur = argLemur;
		ezLemurInstance = EZLemurGui.new;
		ezLemurInstance.lemur = lemur;
		ezLemurInstance.pagename = pagename;
		ezLemurInstance.name = name;
		ezLemurInstance.objectReferenceName = objectReferenceName;
		ezLemurInstance.xOffset = xoffset;
    };


	}

	closeLemur { ezLemurInstance.remove }

	lemur_ { |arglemur|
		lemur = arglemur;
		if (ezLemurInstance.isNil) { this.initLemur };
	}


}
