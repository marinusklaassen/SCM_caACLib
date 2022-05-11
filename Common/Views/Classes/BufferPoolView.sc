/*
FILENAME: PatternBoxProjectItemView

DESCRIPTION: THe PatternBoxProjectItemView the project item view which references to the projectview.

AUTHOR: Marinus Klaassen (2012, 2021Q4)

TODO:
Protection: Only remove when not used!!!!!

b = BufferPoolView();
b.front;
c = b.createBufferSelectorView().front;
c.loadState(c.getState());
b.createBufferSelectorView().front;
b.createBufferSelectorView().front;

*/

BufferPoolItemView : View {
	var <buffer, <filepath, mainLayout, <labelName, dragBothPanel, <labelBufferNumber, soundfile, pathName, filename, buttonPlay, buttonPlot, buttonInspec, buttonDelete;
	var <>actionRemove, <>actionMove, <>actionNewItem;
	var prBeginDragAction, prCanReceiveDragHandler, prReceiveDragHandler;

	*new { |path, parent, bounds|
		^super.new(parent, bounds).initialize(path).initializeView();
	}

	initialize { |path|
		pathName = PathName(path);
		filepath = path;
		buffer = Buffer.read(Server.local, path);
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

		buttonDelete = DeleteButton();
		buttonDelete.fixedSize = 20;
		buttonDelete.background = Color.clear.alpha_(0);
		buttonDelete.action = { if(actionRemove.notNil, { actionRemove.value(this); }); };
		mainLayout.add(buttonDelete, align: \right);

		prBeginDragAction =  { |view, x, y|
			this; // Current instance is the object to drag.
		};

		prCanReceiveDragHandler = {  |view, x, y|
			View.currentDrag.isKindOf(BufferPoolItemView) || View.currentDrag.isKindOf(String);
		};

		prReceiveDragHandler = { |view, x, y|
			if (View.currentDrag.isKindOf(BufferPoolItemView), {
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
		// bufferPoolItemViews
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

BufferSelectorView : SCMViewBase {
	var bufferpool, <proxy, <selectedBufferpoolItem, popupMenuBuffers, mainLayout;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize().initializeView();
	}

	initialize {
		proxy = PatternProxy();
	}

	spec_ { |spec|

	}

	uiMode { |mode|

	}

	editMode_ { |mode|

	}

	getProxies {
		var result = Dictionary();
		result[this.name.asSymbol] = proxy;
		^result;
	}

	bufferpool_ { |pool|
		bufferpool = pool;
		this.update();
	}

	update {
		var tmp;
		if (popupMenuBuffers.items.size > 0, {
			tmp = popupMenuBuffers.items[popupMenuBuffers.value];
		});
		popupMenuBuffers.items = bufferpool.bufferPoolItemViews collect: { |view| format("% - %", view.labelBufferNumber.string, view.labelName.string);  };
		if (tmp.notNil, {
			var index;
			popupMenuBuffers.items do: { |item, i| if( item == tmp, { index = i; }); };
			popupMenuBuffers.valueAction = index;
		},{
			popupMenuBuffers.valueAction  = 0;
		});
	}

	initializeView {
		mainLayout = HLayout();
		mainLayout.margins = 0!4;
		this.layout = mainLayout;
		popupMenuBuffers = PopUpMenu();
		popupMenuBuffers.action = { |sender|
			selectedBufferpoolItem = bufferpool.bufferPoolItemViews[sender.value];
			proxy.source = bufferpool.bufferPoolItemViews[sender.value].buffer;
		};
		mainLayout.add(popupMenuBuffers);
	}

	dispose {
		bufferpool.selectorViews.remove(this);
	}

	getState {
		var state = Dictionary();
		state[\selectedItem] = popupMenuBuffers.item;
		^state;
	}

	loadState { |state|
		var index;
		popupMenuBuffers.items do: { |item, i| if( item == state[\selectedItem] , { index = i; }); };
		popupMenuBuffers.valueAction = if (index.notNil, index, 0);
		^state;
	}
}

BufferPoolView : SCMViewBase {
	var <buffers, <selectorViews;
	var mainLayout, layoutBufferPoolItemViews, scrollViewBufferPoolItemViews, <bufferPoolItemViews, buttonAdd, layoutFooter;
	var <>actionBuffersChanged;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize().initializeView().initialized();
	}

	initialize {
		needsControlSpec = false;
		bufferPoolItemViews = List();
		selectorViews = Set();
	}

	updateDependantViews {
		selectorViews do: { |view| view.update(); };
	}

	createBufferSelectorView {
		var newView = BufferSelectorView();
		newView.bufferpool = this;
		selectorViews.add(newView);
		^newView;
	}

	initialized {
		this.addBufferPoolItem(Platform.resourceDir +/+ "sounds/a11wlk01.wav");
    	this.addBufferPoolItem(PathName(BufferPoolView.filenameSymbol.asString).pathOnly ++ "amen-demo.wav");
		this.updateDependantViews();
	}

	initializeView {
		this.name = "Bufferpool";
		this.deleteOnClose = false;
		mainLayout = VLayout();
		this.layout = mainLayout;

		layoutBufferPoolItemViews = VLayout([nil, stretch:1, align: \bottom]); // workaround. insert before stretchable space.
		layoutBufferPoolItemViews.margins_([0,0,0,0]);
		layoutBufferPoolItemViews.spacing_(5);

		scrollViewBufferPoolItemViews = ScrollViewFactory.createInstance(this);
		scrollViewBufferPoolItemViews.canvas.layout = layoutBufferPoolItemViews;
		scrollViewBufferPoolItemViews.canvas.canReceiveDragHandler = {
			var canRecieve = false;
			if (View.currentDrag.isKindOf(String), {
				canRecieve = PathName(View.currentDrag).isFile;
			});
		};
		scrollViewBufferPoolItemViews.canvas.receiveDragHandler = { |view, x, y|
			this.addBufferPoolItem(View.currentDrag);
		};
		mainLayout.add(scrollViewBufferPoolItemViews);

		layoutFooter = HLayout();
		mainLayout.add(layoutFooter, align: \bottom);

		buttonAdd = ButtonFactory.createInstance(this, class: "btn-add");
		buttonAdd.toolTip = "Add a new soundfile";
		buttonAdd.action = { this.addNewItemDialog(); };

		layoutFooter.add(buttonAdd, align: \right);
	}

	addNewItemDialog {
		Dialog.openPanel({ |filepath|
			var pathName = PathName(filepath.value);
			this.addBufferPoolItem(pathName.fullPath);
		},{ "cancelled".postln; });
	}

	addBufferPoolItem { |path|
		var newItem = BufferPoolItemView(path: path);
		var newPosition = bufferPoolItemViews.size;

		newItem.actionRemove = { |sender|
			bufferPoolItemViews.remove(sender).dispose();
			this.updateDependantViews();
		};

		newItem.actionNewItem = { |dragDestinationObject, dragObject|
			this.addBufferPoolItem(dragObject);
		};

		newItem.actionMove = { |dragDestinationObject, dragObject|
			var targetPosition;
			if (dragDestinationObject !==  dragObject, {
				targetPosition = bufferPoolItemViews.indexOf(dragDestinationObject);
				bufferPoolItemViews.remove(dragObject);
				bufferPoolItemViews.insert(targetPosition, dragObject);
				layoutBufferPoolItemViews.insert(dragObject, targetPosition);
				this.updateDependantViews();
			});
		};
		bufferPoolItemViews.add(newItem);
		layoutBufferPoolItemViews.insert(newItem, newPosition);
		this.updateDependantViews();
		^newItem;
	}

	getState {
		var state = Dictionary();
		state[\bufferPoolItemStates] = bufferPoolItemViews collect: { |item| item.getState(); };
		^state;
	}

	loadState { |state|
		bufferPoolItemViews.copy do: { |item| bufferPoolItemViews.remove(item).dispose(); };
		state[\bufferPoolItemStates] do: { |itemState| this.addBufferPoolItem(itemState[\filepath]); };
	}
}
