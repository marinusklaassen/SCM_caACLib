/*
 * Marinus Klaassen 2021
 *
 * Edit & interpret sclang score parameters.
 */

ScoreParamScriptWidget : ScoreWidgetBase {
	var <string, popupParent;

	string_ { |argString|
		setValueFunction.value(argString);
	}

	loadState { |preset| this.string = preset[\script].asString }

	init {  |argString = ""|
		string = argString;
		model = (script: string);
		setValueFunction = { |script|
			model[\script] = script;
			model.changed(\script, script);
			model.postln;
		};
		dependants = ();
		dependants[\action] = {|theChanger, what, argString|
			string = argString;
			if(action.notNil) { action.value(string) };
		};
		model.addDependant(dependants[\action]);

	}

	makeGui { |argParent, argBounds|
		parent = argParent;
		bounds = argBounds.asRect;
		canvas = CompositeView(parent,bounds);
		canvas.background = Color.white.alpha_(0.5);
		widgets = ();
		widgets[\scriptField] = TextView(canvas,canvas.bounds.extent)
		.background_(Color.white.alpha_(0.5))
		.string_(model[\script])
		.keyDownAction_({| ... args|
			var bool = args[2] == 524288;
			bool = args[1].ascii == 13 && bool;
			if (bool) { setValueFunction.value(widgets[\scriptField].string) };
		})
		.enterInterpretsSelection_(false)
		.hasVerticalScroller_(false);
		dependants[\scriptField] = {|theChanger, what, argString|
			widgets[\scriptField].string_(argString);

		};
		model.addDependant(dependants[\scriptField]);

	}

	makePopupGui {
		if (popupParent.isNil) {
			popupParent = Window.new("SCRIPTFIELD")
			.onClose_({
				model.removeDependant(dependants[\popupWindow]);
				popupParent= nil;
				widgets[\popupScriptField] = nil;
			})
			.front;
			widgets[\popupScriptField] = TextView(popupParent, popupParent.bounds.extent)
			.background_(Color.white.alpha_(0.5))
			.string_(model[\script])
			.keyDownAction_({| ... args|
				var bool = args[2] == 524288;
				bool = args[1].ascii == 13 && bool;
				if (bool) { setValueFunction.value(widgets[\popupScriptField].string) };
			})
			.enterInterpretsSelection_(false);
			dependants[\popupWindow] = {|theChanger, what, argString|
				widgets[\popupScriptField].string_(argString);
			}
		} {
			popupParent.front
		};
	}
}