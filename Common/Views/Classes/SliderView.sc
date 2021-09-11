/*
 * FILENAME: SliderView
 *
 * DESCRIPTION:
 *         Based on the sclang EZSlider, however it's a child of View and the child views are organised by a layout, so it can scale.
 *
 *         SliderView(nil, 400@100, \db.asSpec, 0.5, "test").front().action_({ |v| v.postln; }).labelText_("Label test").value_(1.0.rand).value.postln;
 *         SliderView()
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 */

SliderView : View {
	var <>controlSpec, <labelText, <value;
	var <mainLayoutView, <sliderView, <labelView, <numberBoxView, unitView;

	*new { | parent, bounds, controlSpec, initVal, labelText |
		^super.new(parent, bounds).init(controlSpec, initVal, labelText);
	}

	init { | controlSpec, initVal, labelText |

		// newCopyArgs didn't work.. weird. any workaround.
		this.controlSpec = controlSpec;

        // defaults
		if (this.controlSpec.isNil, { this.controlSpec = ControlSpec(); });

		// Configure views
		labelView = StaticText()
		.string_(this.labelText);

		numberBoxView = NumberBox()
		.clipLo_(this.controlSpec.minval)
		.clipHi_(this.controlSpec.maxval)
		.value_(this.controlSpec.map(initVal))
		.action_({ |v|
			var unmappedValue = this.controlSpec.unmap(v.value);
			value = unmappedValue;
			this.action.value(unmappedValue);
			sliderView.value = unmappedValue;
		});

		sliderView = Slider()
		.value_(initVal)
		.orientation_(\horizontal)
		.action_({ |v|
			var mappedValue = this.controlSpec.map(v.value);
			numberBoxView.value = mappedValue;
		    value = v.value;
			this.action.value(v.value); });
		unitView = StaticText()
		.string_(this.controlSpec.units);

		// Configure layout
		mainLayoutView = GridLayout();
		mainLayoutView.add(labelView, row: 0, column: 0, align: \left);
		mainLayoutView.add(numberBoxView, row: 0, column: 2);
		mainLayoutView.add(unitView, row: 0, column: 3, align: \right);
		mainLayoutView.addSpanning(sliderView, row: 1, column: 0, rowSpan: 1, columnSpan: 4);
		mainLayoutView.setColumnStretch(1, 1);
		mainLayoutView.margins = 0!4;

		this.layout = mainLayoutView;
		// Set init values
		this.value = initVal;
	    this.labelText = labelText;
	}

	value_ { |v|
		value = v;
		sliderView.value = value;
		numberBoxView.value = this.controlSpec.map(value);
	}

	labelText_ { |text|
		labelText = text;
		labelView.string = text;
	}
}
