/*
 * FILENAME: ScoreControl -> TODO rename ScoreEditorView
 *
 * DESCRIPTION:
 *         - Score Editor View usercontrol
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 *
 * ScoreParamWidget().front();
 * Script action get string and compile etcc..
 * Add validation errors.
 */


ScoreParamWidget : View {
	var buttonDelete,scorePatternScriptEditingView, <pScript, controlSpec, model, setValueFunction, dependants, <paramController, <pSpec, ez4Buttons, <name, <>currentLayerIndex, <>currentWidgetType, <>currentWidgetIndex, previousLayer;
	var <>actionNameChanged, <>removeAction, <>index, <>paramProxy, <>controllerProxies, <>scriptFunc, actionbuttonDelete, rangeAction, >faderAction;

	*new { |parent, bounds, argName, argIndex, argLemur, argLemurXoffset, argPageName, argObjectReferenceName|
		^super.new(parent, bounds).initialize(argName, argIndex, argLemur, argLemurXoffset, argPageName, argObjectReferenceName)
	}

	initialize { |argName, argIndex, argLemur, argLemurXoffset, argPageName, argObjectReferenceName|

		controlSpec = ControlSpec();
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
				if (faderAction.notNil) { faderAction.value(controlSpec.map(val)) };
			};
		};

		dependants[\rangeAction] = {|theChanger, what, val|
			if (what == \range) {
				if (rangeAction.notNil) { rangeAction.value(controlSpec.map(val)) };
			};
		};

		model.addDependant(dependants[\faderAction]);
		model.addDependant(dependants[\rangeAction]);

		name = argName;
		scriptFunc = {};
		currentLayerIndex = 0;
		currentWidgetType = "Empty";
		currentWidgetIndex = argIndex;
		index = argIndex;

		ez4Buttons = ScoreParamButtonStripWidget.new; // Niet meer op deze manier. Is niet nodig. Dus TODO: button trip in deze view verwerken.

		ez4Buttons.action[0] = {
			scorePatternScriptEditingView.showSeparateEditingView();

		};

		ez4Buttons.action[1] = {
			var showLayer, objectType;
			currentWidgetIndex = currentWidgetIndex + 1 % 3;
			currentLayerIndex = 1;
			if (currentWidgetIndex == 0) {

				// Doe dit via stacklayouts
			} {
				showLayer = paramController.canvas;
			};
			previousLayer.visible = false;
			showLayer.visible = true;
			currentWidgetType = ["Empty", "Fader", "Range"][currentWidgetIndex];
			paramController.selectElement(currentWidgetType);
			previousLayer = showLayer;
		};

		ez4Buttons.action[2] = {
			var showLayer;
			currentLayerIndex = currentLayerIndex + 1 % 3;
			// if (currentLayerIndex == 1) { currentLayerIndex = currentLayerIndex + 1 };
			// showLayer = [pScript.canvas, paramController.canvas, pSpec.canvas][currentLayerIndex];
			// Doe dit via stacklayout
			previousLayer.visible = false;
			showLayer.visible = true;
			previousLayer = showLayer;
		};

		ez4Buttons.action[3] = { "button action to be implemented".postln; };

		this.initializeView();
	}

	initializeView {
		var layerBounds;

		var mainLayout, textPatternKeyname, layoutStackEditingSection, scorePatternScriptEditorView;

		mainLayout = HLayout();
		mainLayout.background_(Color.red.alpha_(0.4));

		this.layout = mainLayout;

		textPatternKeyname = TextField();
		textPatternKeyname.minWidth = 80;
		textPatternKeyname.string = name.asString;
		textPatternKeyname.action = { | sender |
			name = sender.string.asString;
			if (actionNameChanged.notNil) { actionNameChanged.value(name) };
			paramController.name = name;
		};

        mainLayout.add(textPatternKeyname);

        layoutStackEditingSection = StackLayout();
        mainLayout.add(layoutStackEditingSection);

        scorePatternScriptEditingView = ScorePatternScriptEditingView();
		layoutStackEditingSection.add(scorePatternScriptEditingView);

	    pSpec = ScoreControlSpecView();
		pSpec.action = { |argSpec|
			"a new controlSpec:".postln;
			controlSpec = argSpec.postln;
			"is added".postln;
		};


		// TODO ga verder met dit
	/* Case mag weg. Doe dit gewoon binnen de stack
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
			*/
	    // TODO.
		//paramController.makeGui(canvas, layerBounds);


			/*
			[Color.red, Color.blue,Color.yellow, Color.black] do: { |color, i|
			gui = gui.(
				Button(parent,
					Rect(i * jumpWidth + bounds.left, bounds.top, jumpWidth - gaps, bounds.height)
				)
				.states_([[""] ++ color.dup(2)])
				.action_({ if (action[i].notNil) { action[i].value }})
			);
		};
			*/

		// TODO
		//pSpec.makeGui(canvas, layerBounds);
		//pSpec.canvas.visible_(false);
        /* Maak hier aparte widgets van. ez4 is niet nodig!
		ez4Buttons.makeGui(canvas, Rect(
			ScoreWidgetSettings.settings[\xButtons],
			ScoreWidgetSettings.settings[\chGap],
			ScoreWidgetSettings.settings[\widthButtons],
			ScoreWidgetSettings.settings[\chHeight]), 5);
		*/

		buttonDelete = DeleteButton();
		buttonDelete.fixedSize = 15;
		buttonDelete.action = { if (actionbuttonDelete.notNil, { actionbuttonDelete.value(this) }) };
		mainLayout.add(buttonDelete, \topRight);

	}

// 	moveLemur { | argxOffset| paramController.moveLemur(argxOffset); } // TODO lemur logica anders toepassen. Bv een redraw op basis van positie veranderingen }

		/* TODO should be scoreId
	name_  { | name |
		this.name = name.asString;
		if (actionNameChanged.notNil) { actionNameChanged.value(name) };
		paramController.name = name;
	}
*/
	loadState { |aPreset|
		this.name = aPreset[\name];
		pSpec.loadState(aPreset[\controlSpec]);
		pScript.loadState(aPreset[\script]);
		paramController.loadState(aPreset[\paramController]);
		paramController.selectElement(aPreset[\paramControllerCurrentWidget]);
	}

	getState {
		var preset = Dictionary.new;
		preset[\name] = name.copy;
		preset[\script] = pScript.getState;
		preset[\paramController] = paramController.getState.copy;
		preset[\paramControllerCurrentWidget] = currentWidgetType.copy;
		preset[\controlSpec] = pSpec.controlSpec.copy;
		^preset;
	}
}