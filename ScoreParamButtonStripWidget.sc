/*
 * Marinus Klaassen 2021
 */


ScoreParamButtonStripWidget  {

	var <action, <gui;

	*new { ^super.new.init; }

	init { action = IdentityDictionary.new; }

	makeGui { |parent, argBounds, gaps|
		var bounds = argBounds.asRect;
		var jumpWidth = bounds.width * 0.25;
		var height = bounds.height;
		gui = Array.new;
		[Color.red, Color.blue,Color.yellow, Color.black] do: { |color, i|
			gui = gui.(
				Button(parent,
					Rect(i * jumpWidth + bounds.left, bounds.top, jumpWidth - gaps, bounds.height)
				)
				.states_([[""] ++ color.dup(2)])
				.action_({ if (action[i].notNil) { action[i].value }})
			);
		};
	}
}
