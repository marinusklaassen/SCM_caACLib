/*
 * FILENAME: ProjectStateManager
 *
 * DESCRIPTION:
 *         ProjectStateManager store and load SC objects as archive files
 *         - Constructs a usercontrols
 *         - Can be used without GUI.
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 *
 */

ProjectStateManager {
	var <userControl, >loadAction, <>storeAction;

	*new {
		^super.new.init();
	}

	init {
		userControl = HLayout();
		userControl.add(Button()
			.states_([["open project", Color.black, Color.red]])
			.action_({ this.load(); })
			.font_(Font("Menlo",12)));

		userControl.add(Button()
			.states_([["save project", Color.black, Color.red]])
			.action_({ this.store(); })
			.font_(Font("Menlo",12)));
	}

	store {
		Dialog.savePanel({ arg path; var temp;
			"The project state is saved to file.".postln;
			if (storeAction.notNil) {
				temp = storeAction.value;
				temp.writeArchive(path.postln);
		}},{ "cancelled".postln; });
	}

	load {
	    Dialog.getPaths({ arg paths;
		    paths do: { arg p;
			if (loadAction.notNil) {
				var temp = Object.readArchive(p.value.postln);
				loadAction.value(temp);
			}
		}},{ "cancelled".postln; });
	}
}
