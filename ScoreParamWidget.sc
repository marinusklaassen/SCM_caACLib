/*
 * Marinus Klaassen 2021
 *
 * Ties all the pieces together inorder to conttrol ScoreParameters which inorder control patterns are possible other constructs.
 */


ScoreParamWidget : ScoreWidgetBase {
	var <pScript, <paramController, <pSpec, ez4Buttons, <name, <>currentLayerIndex, <>currentWidgetType, <>currentWidgetIndex, previousLayer;
	var <>nameAction, <>removeAction, <>index, <>paramProxy, <>controllerProxies, <>scriptFunc;

	*new { |argName, argIndex, argLemur, argLemurXoffset, argPageName, argObjectReferenceName|
		^super.newCopyArgs.init(argName, argIndex, argLemur, argLemurXoffset, argPageName, argObjectReferenceName)
	}

	init { |argName, argIndex, argLemur, argLemurXoffset, argPageName, argObjectReferenceName|

		name = argName;
		scriptFunc = {};
		currentLayerIndex = 0;
		currentWidgetType = "Empty";
		currentWidgetIndex = argIndex;
		index = argIndex;
		pScript = ScoreParamScriptWidget.new;
		paramController = ScoreParamWidgetTypeSelectorWidget.new;
		paramController.lemur = argLemur;
		paramController.pagename = argPageName;
		paramController.objectReferenceName = argObjectReferenceName;
		paramController.name = argName.asString;
		paramController.initLemur(argLemur, argLemurXoffset);
		paramController.spec = ControlSpec(); // to be initialize

		pSpec = ScoreParamControlSpecWidget.new;
		pSpec.action = { |argSpec|
			"a new spec:".postln;
			paramController.spec = argSpec.postln;
			"is added".postln;
		};
		pSpec.spec = ControlSpec();

		ez4Buttons = ScoreParamButtonStripWidget.new;

		ez4Buttons.action[0] = { pScript.makePopupGui };

		ez4Buttons.action[1] = {
			var showLayer, objectType;
			currentWidgetIndex = currentWidgetIndex + 1 % 3;
			currentLayerIndex = 1;
			if (currentWidgetIndex == 0) {

				showLayer = pScript.canvas;
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
			showLayer = [pScript.canvas, paramController.canvas, pSpec.canvas][currentLayerIndex];
			previousLayer.visible = false;
			showLayer.visible = true;
			previousLayer = showLayer;
		};

		ez4Buttons.action[3] = { "button action to be implemented".postln; };
	}

	moveBounds { |x = 0, y = 0| canvas.moveTo(x,y) }

	moveLemur { |argxOffset| paramController.moveLemur(argxOffset); }

	name_  { |argName|
		name = argName.asString;
		if (nameAction.notNil) { nameAction.value(name) };
		paramController.name = name;
		"what is this??".postln;
		if (gui.notNil) { gui[\name].string = if (name.isKindOf(String)) { name } { name.asCompileString }; };
	}

	makeGui { |parent, bounds|
		var layerBounds;

		canvas = CompositeView(parent, bounds)
		.background_(Color.red.alpha_(0.4));

		gui = ();
		gui[\name] = TextField(canvas, Rect(
			ScoreWidgetSettings.settings[\xName],
			ScoreWidgetSettings.settings[\chGap],
			ScoreWidgetSettings.settings[\widthName],
			ScoreWidgetSettings.settings[\chHeight]
			)
		).string_(name.asString)
		.action_({|argName|
			name = argName.string.asString;
			// name = interpret(argName.string);
			if (nameAction.notNil) { nameAction.value(name) };
			paramController.name = name;
		});

		layerBounds = Rect(
			ScoreWidgetSettings.settings[\xLayers],
			ScoreWidgetSettings.settings[\chGap],
			ScoreWidgetSettings.settings[\widthLayers],
			ScoreWidgetSettings.settings[\chHeight]);

		pScript.makeGui(canvas, layerBounds);

		previousLayer = pScript.canvas;

		paramController.makeGui(canvas, layerBounds);

		if (currentWidgetType == "Fader" || (currentWidgetType == "Range")) {
			previousLayer.visible_(false);
			previousLayer = paramController.canvas.visible_(true);
			paramController.restoreGui(canvas, layerBounds, currentWidgetType);
		} { paramController.canvas.visible_(false); };

		pSpec.makeGui(canvas, layerBounds);
		pSpec.canvas.visible_(false);

		ez4Buttons.makeGui(canvas, Rect(
			ScoreWidgetSettings.settings[\xButtons],
			ScoreWidgetSettings.settings[\chGap],
			ScoreWidgetSettings.settings[\widthButtons],
			ScoreWidgetSettings.settings[\chHeight]), 5);

		gui[\remove] = MButtonV(canvas, Rect(
			ScoreWidgetSettings.settings[\xRemove],
			ScoreWidgetSettings.settings[\chGap],
			ScoreWidgetSettings.settings[\widthRemove],
			ScoreWidgetSettings.settings[\widthRemove]))
		.action_({ if (removeAction.notNil) { removeAction.value(index) } });
	}

	closeGui {
		canvas.remove; gui do: (_.remove); gui = nil;
		pScript.closeGui;
		paramController.closeGui;
		pSpec.closeGui;
	}

	closeLemur { paramController.closeLemur; }

	loadState { |aPreset|
		this.name = aPreset[\name];
		pSpec.loadState(aPreset[\spec]);
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
		preset[\spec] = pSpec.spec.copy;
		^preset;
	}
}