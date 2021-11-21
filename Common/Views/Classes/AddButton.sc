/*
FILENAME: AddButton

DESCRIPTION: A tap control widget swith a plus sign.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
w = Window().front; a = AddButton(w,Rect(0,0,80,80)).action_({ "release function".postln; });
*/


AddButton : UserView {
	var pressed=false, strokeColor, highLightColor;

	*new { |parent, bounds|
		^super.new(parent, bounds).init();
	}

	init {
		strokeColor = Color.black;
		highLightColor = Color.black;
		this.drawFunc = {
			var radius = min(this.bounds.width, this.bounds.height) * 0.75;
			var widthLine = min(this.bounds.width, this.bounds.height) * 0.1;
			var cp = [
				(radius * 0.5).neg @ 0,
				0 @ (radius * 0.5),
				(radius * 0.5) @ 0,
				0 @(radius * 0.5).neg
			];

			Pen.strokeColor = strokeColor;
			Pen.smoothing = true;
			Pen.translate(this.bounds.width * 0.5, this.bounds.height * 0.5);
			Pen.width = widthLine;
			// draw plus
			Pen.line(cp[0],cp[2]);
			Pen.line(cp[1],cp[3]);
			Pen.fillStroke;

			if (pressed, {
				Pen.translate(neg(this.bounds.width * 0.5),neg(this.bounds.height * 0.5));
				Pen.width = widthLine * 0.5;
				Pen.strokeColor = highLightColor;
				Pen.moveTo(0@ 0);
				Pen.lineTo(0@ this.bounds.height);
				Pen.lineTo(this.bounds.width @ this.bounds.height);
				Pen.lineTo(this.bounds.width @ 0);
				Pen.lineTo(0@0);
				Pen.stroke;
			});
		};

		this.refresh();

		this.mouseDownAction = {
			pressed = true;
			this.refresh();
		};
		this.mouseUpAction = {
			pressed = false;
			this.refresh();
			if (this.action.notNil, { this.action.value(); })
		};
	}

	strokeColor_ {  |color|
		strokeColor = color;
		this.refresh();
	}

	strokeColors {
		^strokeColor;
	}

	highLightColor_ {  |color|
		highLightColor = color;
		this.refresh();
	}

	highLightColor {
		^highLightColor;
	}
}
