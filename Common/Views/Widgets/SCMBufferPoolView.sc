/*
FILENAME: SCMBufferPoolView

DESCRIPTION: SCMBufferPoolView to maintain a set of audio buffers.

AUTHOR: Marinus Klaassen (2022Q2)
*/

SCMBufferPoolView : SCMViewBase {
	var <buffers, <selectorViews;
	var mainLayout, layoutBufferPoolItemViews, scrollViewBufferPoolItemViews, <bufferPoolItemViews, buttonAdd, layoutFooter;
	var <>actionBuffersChanged, <>canUpdateDependants;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize().initializeView().initialized();
	}

	initialize {
		needsControlSpec = false;
		canUpdateDependants = true;
		bufferPoolItemViews = List();
		selectorViews = Set();
	}

	updateDependantViews {
		selectorViews do: { |view| view.update(); };
	}

	createBufferSelectorView {
		var newView = SCMBufferSelectorView();
		newView.bufferpool = this;
		selectorViews.add(newView);
		^newView;
	}

	initialized {
		this.addBufferPoolItem(Platform.resourceDir +/+ "sounds/a11wlk01.wav");
		this.addBufferPoolItem(SCMAudioHelper.fullpathAudiofile("amen-loop.wav"));
		this.updateDependantViews();
	}

	initializeView {
		this.name = "Bufferpool [mono]";
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
		var newItem = SCMBufferPoolItemView(path: path);
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
		if (canUpdateDependants, {
			this.updateDependantViews();
		});
		^newItem;
	}

	getState {
		var state = Dictionary();
		state[\bufferPoolItemStates] = bufferPoolItemViews collect: { |item| item.getState(); };
		^state;
	}

	loadState { |state|
		canUpdateDependants = false;
		if (state[\bufferPoolItemStates].size > 0, {
			bufferPoolItemViews.copy do: { |item| bufferPoolItemViews.remove(item).dispose(); };
		});
		state[\bufferPoolItemStates] do: { |itemState|
			this.addBufferPoolItem(itemState[\filepath]);
		};
		canUpdateDependants = true;
	}
}
