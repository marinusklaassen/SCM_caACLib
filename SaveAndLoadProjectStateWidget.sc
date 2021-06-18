/* Marinus Klaassen 2021 */

SaveAndLoadProjectStateWidget {
	var <canvas, <gui, >readAction, <>storeAction;

	*new { ^super.new }

	makeGui { |argParent, argBounds|
		var width = argBounds.width, height = argBounds.height;
		canvas = CompositeView(argParent, argBounds)
		.background_(Color.clear);

		gui = { |i|
			Button(canvas, Rect(i * 0.5 * width, 0, width * 0.5, height))
			.states_([[["all: open file", "all: save to file"][i], Color.black, Color.red]])
			.action_({ var func = [{this.read}, {this.store}][i]; func.value; })
			.font_(Font("Menlo",12)) } ! 2;
	}

	store {
		Dialog.savePanel({ arg path; var temp;
			"The project state is saved to file.".postln;
			if (storeAction.notNil) {
				temp = storeAction.value;
				temp.writeArchive(path.postln);
		}},{ "cancelled".postln; });
	}

	read {  Dialog.getPaths({ arg paths;
		paths do: { arg p;
			if (readAction.notNil) {
				var temp = Object.readArchive(p.value.postln);
				readAction.value(temp);
			}

		}},{ "cancelled".postln; });
	}
}


