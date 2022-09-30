/*
FILENAME: SCMBufferPoolItemView

DESCRIPTION: SCMBufferPoolItemView of a BufferPoolView to maintain a set of audio buffers.

AUTHOR: Marinus Klaassen (2022Q2)
*/

SCMBufferPoolItemView : View {
	var <buffer, <filepath, mainLayout, <labelName, dragBothPanel, <labelBufferNumber, soundfile, pathName, filename, buttonPlay, buttonPlot, buttonInspec, buttonDelete;
	var <>actionRemove, <>actionMove, <>actionNewItem;
	var prBeginDragAction, prCanReceiveDragHandler, prReceiveDragHandler;

	*new { |path, parent, bounds|
		^super.new(parent, bounds).initialize(path).initializeView();
	}

	initialize { |path|
		pathName = PathName(path);
		filepath = path;
		buffer = Buffer.readChannel(Server.local, path, channels: 0);
	}

	initializeView {

		mainLayout = HLayout();
		this.toolTip = pathName.fullPath;
		this.layout = mainLayout;
		mainLayout.margins = [5, 5, 20, 5];
		this.background = Color(0.45490196078431, 0.55686274509804, 0.87843137254902);

		dragBothPanel = DragBoth();
		dragBothPanel.fixedWidth = 24;
		dragBothPanel.background = Color.black.alpha_(0.5);
		dragBothPanel.toolTip = this.toolTip;
		mainLayout.add(dragBothPanel, align: \top);

		mainLayout.add(StaticText().string_("-"));

		labelBufferNumber = StaticText();
		labelBufferNumber.fixedWidth = 40;
		labelBufferNumber.string = buffer.bufnum;
		mainLayout.add(labelBufferNumber);

		labelName = StaticText();
		labelName.string = pathName.fileName;
		mainLayout.add(labelName, stretch: 1.0);

		buttonPlot = Button();
		buttonPlot.string = "plot";
		buttonPlot.action = { buffer.plot(Server.local); };
		mainLayout.add(buttonPlot, align: \right);

		buttonInspec = Button();
		buttonInspec.string = "inspect";
		buttonInspec.action = { buffer.inspect; };
		mainLayout.add(buttonInspec, align: \right);

		buttonPlay = Button();
		buttonPlay.string = "play";
		buttonPlay.action = { buffer.play; };
		mainLayout.add(buttonPlay, align: \right);

		buttonDelete = SCMButtonDelete();
		buttonDelete.fixedSize = 20;
		buttonDelete.background = Color.clear.alpha_(0);
		buttonDelete.action = { if(actionRemove.notNil, { actionRemove.value(this); }); };
		mainLayout.add(buttonDelete, align: \right);

		prBeginDragAction =  { |view, x, y|
			this; // Current instance is the object to drag.
		};

		prCanReceiveDragHandler = {  |view, x, y|
			View.currentDrag.isKindOf(SCMBufferPoolItemView) || View.currentDrag.isKindOf(String);
		};

		prReceiveDragHandler = { |view, x, y|
			if (View.currentDrag.isKindOf(SCMBufferPoolItemView), {
				if (actionMove.notNil, { actionMove.value(this, View.currentDrag); });
			});
			if(View.currentDrag.isKindOf(String), {
				if (actionNewItem.notNil, { actionNewItem.value(this, View.currentDrag); });
			});
		};

		// Start drag & drop workaround
		this.setDragAndDropBehavior(this);
		this.setDragAndDropBehavior(dragBothPanel);
		this.setDragAndDropBehavior(labelName);
		this.setDragAndDropBehavior(labelBufferNumber);
		this.setDragAndDropBehavior(buttonPlay);
		this.setDragAndDropBehavior(buttonPlot);
		this.setDragAndDropBehavior(buttonInspec);
		this.setDragAndDropBehavior(buttonDelete);
	}

	initialized {
	}

	setDragAndDropBehavior { |object|
		object.dragLabel = buffer.bufnum + "-" + labelName.string;
		object.beginDragAction = prBeginDragAction;
		object.canReceiveDragHandler = prCanReceiveDragHandler;
		object.receiveDragHandler = prReceiveDragHandler;
	}

	addBufferPoolItem { |path|
		// SCMBufferPoolItemViews
	}

	dispose {
		this.remove;
		buffer.free;
	}

	getState {
		var state = Dictionary();
		state[\filepath] = filepath;
		^state;
	}

	loadState { |state| }
}
