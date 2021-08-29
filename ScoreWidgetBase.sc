/*
 * Marinus Klaassen 2021 -> Rename ScoreWidgetViewBase -> Is base view wel nodig?
 *
 * Base class for all the score and controller widget objects.
 */

ScoreWidgetBase {
	var parent, <model, <setValueFunction, dependants, <gui, <canvas, widgets, <bounds, <>action;

	*new { ^super.new.init }

	init { }

	makeGui { }

	getState { ^model.copy }

	loadState { |preset| setValueFunction.value(preset) }

	closeGui { canvas.remove; gui do: (_.remove); gui = nil; }

	close {
		this.closeGui;
		model.dependants do: { |i| model.removeDependant(i) };
	}

	bounds_ { canvas.bounds = bounds }
}