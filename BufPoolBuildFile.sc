

BPDragAndDropElement {
	var stringModel, stringValueFunction, stringActionDependant, oldString;
	var editModel, editValueFunction, editDependant, editModel;
	var >beginDragAction, >endDragAction, <>stringAction, >selectAction;
	var dragAndDropContainer, editView, dragAndDropView, textEdit, stringGuiDependant, editDependant, removeButton;
	var >removeAction;
	// object indentifiers
	var <>what, <>index, <>extra;

	*new { |argIndex, argWhat, argString|
		^super.newCopyArgs.init(argIndex, argWhat, argString);
	}

	stringModelViewController {

		stringModel = (string: "");

		stringValueFunction = { |argString|
			oldString = stringModel[\string];
			stringModel[\string] = argString;
			stringModel.changed(\string, argString);
		};

		stringActionDependant = { |theChanger, what, argString|
			if(stringAction.notNil) { stringAction.value(argString, oldString, what, index); }

		};

		stringModel.addDependant(stringActionDependant);
	}

	editModelViewController {

		editModel = (bool: false);

		editValueFunction = { |argBool|
			editModel[\bool] = argBool;
			editModel.changed(\bool, argBool);
		};
	}

	init { |argIndex, argWhat, argString|
		index = argIndex;
		what = argWhat;
		this.stringModelViewController;
		this.editModelViewController;
		stringValueFunction.value(argString);
	}

	makeGui { |argParent, argBounds|
		var bounds = argBounds.asRect;
		editView = CompositeView(argParent, bounds)
		.background_(Color.black.alpha_(0));

		textEdit = TextField(editView, bounds.extent)
		.background_(Color.black.alpha_(0.99))
		.string_(stringModel[\string])
		.stringColor_(Color.yellow)
		.action_({ |text| stringValueFunction.value(text.string) });


		removeButton = MButtonV(editView, Rect(bounds.bounds.width -20, 5, 15, 15));
		removeButton.action = { if (removeAction.notNil) { removeAction.value(what, index,  stringModel[\string], extra) } };

		dragAndDropView = CompositeView(argParent, bounds)
		.background_(Color.black.alpha_(0));

		dragAndDropContainer = DragBoth(dragAndDropView,bounds.extent).align_(\10)
		.background_(Color.black.alpha_(0.99))
		.object_(stringModel[\string])
		.stringColor_(Color.green)

		.beginDragAction_({
			if (beginDragAction.notNil) { beginDragAction.value(what, index, stringModel[\string], extra); };
		})
		.receiveDragHandler_({arg obj;
			if (endDragAction.notNil) { endDragAction.value(what,index,stringModel[\string], extra); }
		});

		stringGuiDependant = { |theChanger, what, argString|
			argString.postln;
			dragAndDropContainer.object = argString;
			textEdit.string = argString.asString;
		};
		stringModel.addDependant(stringGuiDependant);

		if (editModel[\bool]) {
			dragAndDropView.visible = false;
			editView.visible = true;
		} {
			dragAndDropView.visible = true;
			editView.visible = false;

		};

		editDependant = { |theChanger, what, argBool|
			if (argBool) {
				dragAndDropView.visible = false;
				editView.visible = true;
			} {
				dragAndDropView.visible = true;
				editView.visible = false;

			};

		};
		editModel.addDependant(editDependant);

	}

	closeGui {
		// Remove views
		editView.remove; textEdit.remove; removeButton.remove;
		dragAndDropView.remove; dragAndDropContainer.remove;
		// Remove GUI dependants from control models
		stringModel.removeDependant(stringGuiDependant);
		editModel.removeDependant(editDependant);
	}

	remove {
		stringModel.removeDependant(stringActionDependant);
	}

	string_ { |argString|
		stringValueFunction.value(argString);
	}

	string {
		^stringModel[\string];
	}

	edit {  |argEdit|
		editValueFunction.value(argEdit)
	}

}


BPdata {
	classvar <>bufferpool;

	*new {
		bufferpool = IdentityDictionary.new;
	}
}

BPBankView {
	var startDragArray;
	var view, <units;
	var <>bpData;
	var >addAction, <>selectAction, <>removeAction, <>stringAction;
	var bounds, parent;

	*new{ ^super.new.init }

	init {
		units = Array.new;
	}

	makeGui { |argParent, argBounds|
		parent = argParent; bounds = argBounds;
		view = ScrollView(argParent, Rect(5, 5, 180, 300));
		view.background = Color.grey;

		units do: { |bankUnit, i|
			bankUnit.makeGui(view, Rect(0, 35 * i, 175, 30));

			bankUnit.beginDragAction = { |...array| startDragArray = array; };

			bankUnit.endDragAction = { |what, index, string, extra|
				var currentDragObject = View.currentDrag;
				var dragString = if (currentDragObject.isArray) { currentDragObject[2] } { nil };

				if (string != dragString) {

					case { currentDragObject.isString } {
						"Append Soundfile to this bank: %\n".postf(bankUnit.string);
					} { currentDragObject.isArray } {
						if (currentDragObject.at(0) == "bank") {

							units[startDragArray[1].postln].string = units[bankUnit.index.postln].string.copy;
							units[bankUnit.index].string = startDragArray[2];
						};
						if (startDragArray[3] != bankUnit.string) {
							"Append soundfile to %\n".postf(bankUnit.string);
							"Peform remove action from previous arrays".postln;
							"Update GUI".postln;
						};
					} { currentDragObject == \add } {
						this.add(index);
					};
				} {
					if (selectAction.notNil) {
						selectAction.value(what, index, string, extra)
					}
				}
			}
		}
	}

	closeGui { view.remove; view = nil; units do: (_.closeGui) }

	update {
		units do: { |unit, i| unit.index = i };
		if (view.notNil) { this.closeGui; this.makeGui(parent.postln, bounds.postln); }
	}

	removeBank { |argIndex|
		var unit = units.removeAt(argIndex);
		unit.closeGui;
		unit.remove;
		this.update;
		if (units.size == 0) { this.add };
		if (removeAction.notNil) { removeAction.value(unit.what, unit.index, unit.string, unit.extra) }
	}

	remove { this.closeGui; units do: (_.remove); }

	add { |argIndex, argName|
		var index, unit;
		if (argName.isNil) { argName = "default bank " ++ units.size };
		argIndex.postln;
		if (argIndex.isNil) {
			index = units.size;
			unit = BPDragAndDropElement(index, "bank", argName);
			units = units.add(unit);
		} {
			index = argIndex;
			unit = BPDragAndDropElement(index, "bank", argName);
			units = units.insert(index, unit);
		};

		// Set action functions
		units[index].selectAction = selectAction;
		units[index].removeAction = { |what, index, string, extra|
			this.removeBank(index);
		};

		units[index].stringAction =  stringAction;

		this.update;

		if (addAction.notNil) { addAction.value(unit) };

	}
}


// Verder met deze unit!!

BPSoundfileView {
	var startDragArray;
	var view, <units;
	var <>bpData, <>bankName;
	var >addAction, <>addBufAction, <>selectAction, <>swapAction, >removeAction;
	var bounds, parent;

	*new{ ^super.new.init }

	init {
		units = Array.new;
		this.add;
	}

	makeGui { |argParent, argBounds|
		parent = argParent; bounds = argBounds;
		view = ScrollView(argParent, argBounds);
		view.background = Color.grey;

		// Set actions!!!
		units do: { |soudfileUnit, i|
			soudfileUnit.makeGui(view, Rect(0, 35 * i, argBounds.width - 4, 30));

			soudfileUnit.beginDragAction = { |...array| startDragArray = array; };

			soudfileUnit.endDragAction = { |what, index, string, extra|
				var currentDragObject = View.currentDrag;
				var dragString = if (currentDragObject.isArray) { currentDragObject[2] } { nil };
				var thisWhat = soudfileUnit.what;
				var thisIndex = soudfileUnit.index;


				if (string != dragString) {


					case { currentDragObject.isString } {
						this.addBuffer(currentDragObject, thisWhat, thisIndex, currentDragObject, extra);

					} { currentDragObject.isArray } {
						if (currentDragObject.at(0) == "soundfile") {

							units[startDragArray[1]].string = units[soudfileUnit.index.postln].string.copy;
							units[soudfileUnit.index].string = startDragArray[2];
							"is selection action nil".postln;
							if (swapAction.notNil.postln) {
								"selectaction boolean is oke".postln;
								swapAction.value(bankName, startDragArray[1], soudfileUnit.index) };
						};

					} { currentDragObject == \add } { this.addDialog(thisWhat, thisIndex) }
				} {
					if (selectAction.notNil) {
						selectAction.value(bankName, index, string)
					}
				}

			}
		}
	}

	closeGui { view.remove; view = nil; units do: (_.closeGui) }

	update {
		units do: { |unit, i| unit.index = i };
		if (view.notNil) { this.closeGui; this.makeGui(parent.postln, bounds.postln); }
	}

	removeSoundFile { |argIndex|
		var unit = units.removeAt(argIndex);
		unit.closeGui;
		unit.remove;
		this.update;
		if (units.size == 0) { this.add };
		if (removeAction.notNil) { removeAction.value(argIndex) };
	}

	remove { this.closeGui; units do: (_.remove); }

	add { |argIndex, argName|
		var index, unit;
		if (argName.isNil) { argName = "drop an audio file in here" };
		argIndex.postln;
		if (argIndex.isNil) {
			"append".postln;
			index = units.size;
			unit = BPDragAndDropElement(index, "soundfile", argName);
			units = units.add(unit);

		} {
			"insert".postln;
			index = argIndex;
			unit = BPDragAndDropElement(index, "soundfile", argName);
			units = units.insert(index + 1, unit);

		};

		// Set action functions
		units[index].selectAction = selectAction;
		units[index].removeAction = { |what, index, string, extra|
			this.removeSoundFile(index);
		};

		this.update;

		if (addAction.notNil) { addAction.value(argName) };

	}

	addBuffer { |path, what, index|
		if (path.pathExists != false) {
			Buffer.read(path: path, action: { |buf|
				if (addBufAction.notNil) {
				addBufAction.value(buf, path, what, index, bankName)
				}
			})
		}
	}

	addDialog { |what, index|
		Buffer.loadDialog(action: { |buf|
			if (addBufAction.notNil) {
				addBufAction.value(buf, buf.path, what, index, bankName)
			}
		})
	}
}




BPTransporter {
	var view, playFlag, recordFlag, editFlag, waveformFlag, >editAction;
	var addView, thrashView, addView, recordView, editView, playView, waveformView;
	var bufferToPlay, playSynth;

	*new { ^super.new.init; }

	init {
		playFlag = 0;
		recordFlag = 0;
		editFlag = 0;
		waveformFlag = 0;
	}

	changeBuffer { |argBuffer|
		bufferToPlay = argBuffer;
		if (playSynth.isPlaying) {
			playSynth.free;
			playSynth = { PlayBuf.ar(bufferToPlay.numChannels, bufferToPlay, loop: 1) }.play;
			playSynth.track;
		}
	}

	makeGui { |argParent, argBounds|
		view = CompositeView(argParent, argBounds);
		view.background = Color.grey;

		thrashView = DragBoth(view,Rect(5, 5, 50, 50))
		.align_(\10)
		.background_(Color.yellow(0.8))
		.object_(\trash)
		.align_(\center)
		.stringColor_(Color.black)
		.receiveDragHandler_{
			"remove this object".postln;
			View.currentDrag.postln
		};

		editView = Button(view, Rect(60, 5, 50, 50))
		.states_([
			["edit", Color.yellow, Color.black],
			["edit off", Color.yellow, Color.blue]
		])
		.action_({ |b|
			editFlag = b.value;
			if (editAction.notNil) { editAction.value(editFlag) }
		})
		.value_(editFlag);

		addView = DragBoth(view,Rect(115, 5, 50, 50)).align_(\10)
		.background_(Color.black.alpha_(0.99))
		.object_(\add)
		.align_(\center)
		.stringColor_(Color.yellow);

		recordView = Button(view, Rect(170, 5, 50, 50))
		.states_([
			["REC", Color.red, Color.black],
			["REC", Color.black, Color.red]
		])
		.action_({ |b|
			recordFlag = b.value;
			"record audio".postln;
			"getCurrentSelectBankName".postln;
			"append buffer to currentbank".postln;
			"update gui".postln;
		})
		.value_(recordFlag);

		playView = Button(view, Rect(280, 5, 50, 50))
		.states_([
			["PLAY", Color.yellow, Color.black],
			["PLAY", Color.black, Color.yellow]
		])
		.action_({ |b|
			playFlag = b.value;
			if (b.value > 0 && bufferToPlay.notNil) {
				playSynth = { PlayBuf.ar(bufferToPlay.numChannels, bufferToPlay, loop: 1) }.play;
				playSynth.track;
			} {
				playSynth.release(0.05)
			};
		})
		.value_(playFlag);

		waveformView = Button(view, Rect(225, 5, 50, 50))
		.states_([
			["WAV", Color.yellow, Color.black],
			["WAV", Color.yellow, Color.blue]
		])
		.action_({ |b| waveformFlag = b.value })
		.action_(waveformFlag);
	}

	closeGui {
		view.remove; addView.remove; thrashView.remove; addView.remove;
		recordView.remove; playView.remove; waveformView.remove;
	}
}

