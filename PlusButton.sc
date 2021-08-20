/*
 * FILENAME: PlusButton
 *
 * DESCRIPTION:
 *         A tap control widget swith a plus sign.
 *
 *         w = Window().front; a = PlusButton(w,Rect(0,0,80,80)).action_({ "release function".postln; });
 *
 * NICE TO HAVE:
 *         set strokocolor an redraw.
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 *
 */

PlusButton : UserView {
	var pressed=false;

	*new { |parent, bounds|
		^super.new(parent, bounds).init();
	}

	init {
		this.background = Color.new(0.8,0.8,0.8);
		this.drawFunc = {
			var radius = min(this.bounds.width, this.bounds.height) * 0.75;
			var widthLine = min(this.bounds.width, this.bounds.height) * 0.1;
			var cp = [
				(radius * 0.5).neg @ 0,
				0 @ (radius * 0.5),
				(radius * 0.5) @ 0,
				0 @(radius * 0.5).neg
			];

			Pen.strokeColor = Color.new(0.2,0.2,0.2);
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
				Pen.strokeColor = Color.red;
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
}
