

BufPool {
	classvar bufDict;

	*start {
		bufDict = IdentityDictionary.new;
		if (Server.default.serverRunning) { BufPool.init; } {
			Server.default.boot;
			Server.default.waitForBoot {
				BufPool.init;
			}
		}
	}

	add { |argBank, argBuf|
	 // add a buffer
	}

	remove { |argBank, argIndex|

	}
}

// Create a drag and drog interface!!

OneElement {
	var <index, dragBothGui, <>endDragAction, <>beginDragAction, textGui;

	*new { |argIndex, argParent, argBounds|
		^super.newCopyArgs.init(argIndex, argParent, argBounds);
	}

	init { |argIndex, argParent, argBounds|
		var bounds = argBounds.bounds;
		index = argIndex;
		dragBothGui = DragBoth(argParent,argBounds).align_(\left).background_(Color.grey.alpha_(0.8));
		dragBothGui.object = index;
		dragBothGui.stringColor_(Color.green);

		/*textGui = StaticText(argParent,
			Rect(bounds.left,bounds.top + 5, bounds.width - 30, bounds.height - 6)
		).background_(Color.black);
		textGui.stringColor_(Color.green);*/

		dragBothGui.beginDragAction = {
			if (beginDragAction.notNil) { beginDragAction.value(index); };
		};

		dragBothGui.receiveDragHandler = { arg obj;
			if (endDragAction.notNil) { endDragAction.value(index); }
		};
	}

	object_ { |argString|
		dragBothGui.object = argString;
	}

	object { ^dragBothGui.object }
}

SimpleDragInterface {
	var parent, elements;
	*new { ^super.new.init; }

	init {
		var beginDragIndex;
		parent = Window.new("", Rect(200,200,200,400)).front;
		parent.background = Color.black;
		elements = Array.fill(8, { |i|
			var temp = OneElement(i, parent, Rect(0, i * 50 + 5, 200, 40));
			temp.beginDragAction = { beginDragIndex = temp.index };
			temp.endDragAction = { |endDragIndex|
				// swap objects when drag action is ended
				var tempObject = elements[beginDragIndex].object.copy;
				elements[beginDragIndex].object = elements[endDragIndex].object.copy;
				elements[endDragIndex].object = tempObject;
				}
			}
		)
	}
}






/*
SimpleDragInterface()

// record to disk

//rec
(
// this will record to the disk
SynthDef("rec-ins", {arg bufnum;
    DiskOut.ar(bufnum, In.ar(NumOutputBuses.ir,32) );
}).send(s);
)

(
~recServer = s;
~recSynth = Synth.basicNew("rec-ins", ~recServer);
~recBuf = Buffer.new(~recServer, 65536, 32);
~recBuf.alloc(
	~recBuf.writeMsg("/tmp/test1.aiff", "aiff", "int24", 0, 0, true,
		completionMessage:
		~recSynth.newMsg(~recServer, ["bufnum", ~recBuf], 'addToTail')
	)
)
)

(
~recServer = s;
~recServer.bind{
	~recSynth.free;
	~recBuf.close(completionMessage:~recBuf.freeMsg);
}
)
*/














