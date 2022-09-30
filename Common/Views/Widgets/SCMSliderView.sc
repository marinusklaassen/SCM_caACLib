/*
* FILENAME: SCMSliderView
*
* DESCRIPTION:
*         Based on the sclang EZSlider, however it's a child of View and the child views are organised by a layout, so it can scale.
*
*
a = SCMSliderView(nil, 400@100, \db.asSpec, 0.5, "test").front().action_({ |v| v.postln; }).labelText_("Label test").value_(1.0.rand).value.postln;
a.value = 0;
*         SCMSliderView()
*
* AUTHOR: Marinus Klaassen (2012, 2021Q3)
*/

SCMSliderView : SCMViewBase {
	var <spec, <labelText, <value = 0, <valueMapped, valueProxy, valueMappedProxy;
	var <mainLayoutView, <sliderView, <labelView, <numberBoxView, unitView, controlSpecView, <>actionTextChanged;

	*new { | parent, bounds |
		^super.new(parent, bounds).init();
	}

	uiMode { |uiMode|
		if (uiMode == \brief, {
			labelView.visible = false;
			numberBoxView.visible = false;
			unitView.visible = false;
		});
	}

	editMode_ { |mode| }

	randomize {
		this.value = 1.0.rand;
	}

	spec_ { |argSpec|
		spec = argSpec;
		numberBoxView.clipLo_(this.spec.minval);
		numberBoxView.clipHi_(this.spec.maxval);
		unitView.string_(this.spec.units);
		this.value = value;
	}

	init {
		spec = ControlSpec();
		valueProxy = PatternProxy();
		valueProxy.source = 1;
		valueMappedProxy = PatternProxy();
		valueProxy.source = 1;

		mainLayoutView = GridLayout();
		this.layout = mainLayoutView;

		// Configure views
		labelView = TextFieldFactory.createInstance(this)
		.background_(Color.clear.alpha_(0))
		.string_(this.labelText)
		.action_({ |sender| if (this.actionTextChanged.notNil, { this.actionTextChanged.value(sender); }) });

		numberBoxView = NumberBoxFactory.createInstance(this)
		.action_({ |v|
			var unmappedValue = this.spec.unmap(v.value);
			this.value = unmappedValue;
		});

		sliderView = SliderFactory.createInstance(this)
		.orientation_(\horizontal)
		.action_({ |sender|
			this.value = sender.value;

		});
		unitView = StaticText();

		mainLayoutView.addSpanning(labelView, row: 0, column: 0, columnSpan: 2);
		mainLayoutView.add(numberBoxView, row: 0, column: 2);
		mainLayoutView.add(unitView, row: 0, column: 3, align: \right);
		mainLayoutView.addSpanning(sliderView, row: 1, column: 0, rowSpan: 1, columnSpan: 4);
		mainLayoutView.setColumnStretch(1, 1);
		mainLayoutView.margins = 0!4;
	}

	beginDragAction_ { |handler|
		super.beginDragAction = handler;
		labelView.beginDragAction = handler;
		numberBoxView.beginDragAction = handler;
		sliderView.beginDragAction = handler;
		unitView.beginDragAction = handler;
	}

	dragLabel_ { |label|
		super.dragLabel = label;
		labelView.dragLabel = label;
		numberBoxView.dragLabel = label;
		sliderView.dragLabel = label;
		unitView.dragLabel = label;
	}

	canReceiveDragHandler_ { |handler|
		super.canReceiveDragHandler = handler;
		labelView.canReceiveDragHandler = handler;
		numberBoxView.canReceiveDragHandler = handler;
		sliderView.canReceiveDragHandler = handler;
		unitView.canReceiveDragHandler = handler;
	}

	receiveDragHandler_ { |handler|
		super.receiveDragHandler = handler;
		labelView.receiveDragHandler = handler;
		numberBoxView.receiveDragHandler = handler;
		sliderView.receiveDragHandler = handler;
		unitView.receiveDragHandler = handler;
	}

	value_ { |argValue|
		value = argValue;
		valueMapped = spec.map(argValue);
		sliderView.value = value;
		numberBoxView.value = valueMapped;
		valueProxy.source = value;
		valueMappedProxy.source = valueMapped;
		if (this.action.notNil, {
			this.action.value(this); })
	}

	labelText_ { |text|
		labelText = text;
		labelView.string = text;
	}

	getProxies {
		var result = Dictionary();
		result[this.name.asSymbol] = valueMappedProxy;
		^result;
	}

	getState {
		var state = Dictionary();
		state[\value] = this.value;
		^state;
	}

	loadState { |state|
		this.value = state[\value];
	}
}
