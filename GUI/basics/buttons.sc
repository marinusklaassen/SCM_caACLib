MButtonV {
	var <>view, <>action, state;

	*new { |parent, bounds|
		^super.newCopyArgs.init(parent,bounds.asRect);
	}

	init { |parent, bounds|
		state = false;
		view = UserView(parent,bounds);
		view.background = Color.white;
		view.drawFunc = {
			var radius = min(bounds.width, bounds.height) * 0.7;
			var widthLine = min(bounds.width, bounds.height) * 0.1;
			var cp = [
				(radius * 0.5).neg @ (radius * 0.5).neg,
				(radius * 0.5).neg @ (radius * 0.5),
				(radius * 0.5) @ (radius * 0.5),
				(radius * 0.5) @ (radius * 0.5).neg
			];


			Pen.smoothing = true;
			Pen.translate(bounds.width * 0.5, bounds.height * 0.5);
			Pen.width = widthLine;
			// draw cross.
			Pen.line(cp[0],cp[2]);
			Pen.line(cp[1],cp[3]);
			Pen.stroke;

			if (state, {
				Pen.translate(neg(bounds.width * 0.5),neg(bounds.height * 0.5));
				Pen.width = widthLine * 0.5;
				Pen.strokeColor = Color.red;
				Pen.moveTo(0@ 0);
				Pen.lineTo(0@ bounds.height);
				Pen.lineTo(bounds.width @ bounds.height);
				Pen.lineTo(bounds.width @ 0);
				Pen.lineTo(0@0);
				Pen.stroke;
			});
		};

		view.refresh;
		view.mouseDownAction = { state = true; view.refresh; };
		view.mouseUpAction = { state = false; view.refresh; if (action.notNil, { action.value }) };
	}

	resize { |param = 0| "here".postln; view.resize = param }

}

/*
w = Window.new.front; a = MButtonV(w,Rect(0,0,50,50)).action_({ "release function".postln; }); a.resize = 3
*/

MButtonP {
	var <>view, <>action, state;

	*new { |parent, bounds|
		^super.newCopyArgs.init(parent,bounds.asRect);
	}

	init { |parent, bounds|
		state = false;
		view = UserView(parent,bounds);
		view.background = Color.new(0.8,0.8,0.8);
		view.drawFunc = {
			var radius = min(bounds.width, bounds.height) * 0.75;
			var widthLine = min(bounds.width, bounds.height) * 0.1;
			var cp = [
				(radius * 0.5).neg @ 0,
				0 @ (radius * 0.5),
				(radius * 0.5) @ 0,
				0 @(radius * 0.5).neg
			];

			Pen.strokeColor = Color.new(0.2,0.2,0.2);
			Pen.smoothing = true;
			Pen.translate(bounds.width * 0.5, bounds.height * 0.5);
			Pen.width = widthLine;
			// draw plus
			Pen.line(cp[0],cp[2]);
			Pen.line(cp[1],cp[3]);
			Pen.fillStroke;

			if (state, {
				Pen.translate(neg(bounds.width * 0.5),neg(bounds.height * 0.5));
				Pen.width = widthLine * 0.5;
				Pen.strokeColor = Color.red;
				Pen.moveTo(0@ 0);
				Pen.lineTo(0@ bounds.height);
				Pen.lineTo(bounds.width @ bounds.height);
				Pen.lineTo(bounds.width @ 0);
				Pen.lineTo(0@0);
				Pen.stroke;
			});
		};

		view.refresh;
		view.mouseDownAction = { state = true; view.refresh; };
		view.mouseUpAction = { state = false; view.refresh; if (action.notNil, { action.value }) };
	}

	remove { view.remove }

}

/*
w = Window.new.front; a = MButtonP(w,Rect(0,0,80,80)).action_({ "release function".postln; });
a.remove; w.front;
*/
