/*
FILENAME: SCMBufferSelectorView

DESCRIPTION: SCMBufferSelectorView to select a buffer from a BufferPool context

AUTHOR: Marinus Klaassen (2022Q2)
*/

SCMBufferSelectorView : SCMViewBase {
	var bufferpool, <proxy, <selectedBufferpoolItem, popupMenuBuffers, mainLayout;

	*new { |parent, bounds|
		^super.new(parent, bounds).initialize().initializeView();
	}

	initialize {
		proxy = PatternProxy();
		needsControlSpec = false;
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

		try {
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
		} { |error|
			postln(error);
		}
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
