/*
 * FILENAME: SliderSequencerView
 *
 * DESCRIPTION:
 *         Based on the sclang EZSlider, however it's a child of View and the child views are organised by a layout, so it can scale.
 *
 *         SliderSequencerView(nil, 400@100, \db.asSpec, 0.5, "test").front().action_({ |v| v.postln; }).labelText_("Label test").value_(1.0.rand).value.postln;
 *         SliderSequencerView()
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 */

SliderSequencerView : View {
	var <spec, <labelText, <value, valueMapped;
	var <mainLayoutView, <sliderView, <labelView, <numberBoxView, unitView, controlSpecView, proxy, proxyMapped, sliders;

	*new { | parent, bounds |
		^super.new(parent, bounds).init();
	}

	uiMode { |uiMode|
		if (uiMode == \brief, {
		});
	}

	editMode_ { |mode| }

	randomize {
		sliders do: { |slider| slider.value = 1.0.rand };
		sliders.size do: { |i|
			proxyMapped[i] = spec.map(proxy[i]);
		};
	}

    spec_ { |argSpec|
		spec = argSpec;
		sliders.size do: { |i|
			proxyMapped[i] = spec.map(proxy[i]);
		};
	}

	init {
		mainLayoutView = HLayout();
		this.layout = mainLayoutView;
		this.layout.margins_(0!4).spacing_(0);

		proxy = List.newUsing(0!8);
		proxyMapped = List.newUsing(0!8);
		sliders = List();
		// newCopyArgs didn't work.. weird. any workaround.
		this.spec = spec;

        // defaults
		if (this.spec.isNil, { this.spec = ControlSpec(); });

		proxy.size do: { |i|
			var newSlider = Slider().orientation_(\vertical).minHeight_(100).value_().action_({
				|sender|
				proxy[i] = sender.value;
				proxyMapped[i] = spec.map(sender.value);
			});
			sliders.add(newSlider);
			this.layout.add(newSlider);
		};

		mainLayoutView.margins = 0!4;

	    this.labelText = labelText;
	}

	labelText_ { |text|

	}

	getProxies {
		var result = Dictionary();
		result[this.name.asSymbol] = proxyMapped;
		^result;
	}
}
