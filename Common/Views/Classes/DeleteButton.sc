/*
 * FILENAME: DeleteButton
 *
 * DESCRIPTION:
*         A tap control widget swith a delete (remove) sign.
 *
 *         w = Window().front; a = DeleteButton(w).action_({ "release function".postln; });
 *
 * NICE TO HAVE:
 *         set strokocolor an redraw.
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 *
 * TODO make een single press base SCMUserView voor delete and plus button
 */

DeleteButton : UserView {
	var pressed=false, strokeColor, highLightColor;

	*new { |parent, bounds|
		^super.new(parent, bounds).init();
	}

	init {
		this.background = Color.white;
		strokeColor = Color.black;
		highLightColor = Color.black;

		this.drawFunc = {
			var radius = min(this.bounds.width, this.bounds.height) * 0.7;
			var widthLine = min(this.bounds.width, this.bounds.height) * 0.1;
			var cp = [
				(radius * 0.5).neg @ (radius * 0.5).neg,
				(radius * 0.5).neg @ (radius * 0.5),
				(radius * 0.5) @ (radius * 0.5),
				(radius * 0.5) @ (radius * 0.5).neg
			];

			Pen.smoothing = true;
			Pen.strokeColor = strokeColor;
			Pen.translate(this.bounds.width * 0.5, this.bounds.height * 0.5);
			Pen.width = widthLine;
			// draw cross.
			Pen.line(cp[0],cp[2]);
			Pen.line(cp[1],cp[3]);
			Pen.stroke;

			if (pressed, {
				Pen.translate(neg(this.bounds.width * 0.5),neg(this.bounds.height * 0.5));
				Pen.width = widthLine * 0.5;
				Pen.strokeColor = highLightColor;
				Pen.moveTo(0@0);
				Pen.lineTo(0@this.bounds.height);
				Pen.lineTo(this.bounds.width@this.bounds.height);
				Pen.lineTo(this.bounds.width@0);
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
			if (this.action.notNil(), { this.action.value(); })
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
