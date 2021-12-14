/*
 * FILENAME: MultiStepView
 *
 * DESCRIPTION:
 *         Based on the sclang EZSlider, however it's a child of View and the child views are organised by a layout, so it can scale.
 *
 *         MultiStepView(nil, 400@100, \db.asSpec, 0.5, "test").front().action_({ |v| v.postln; }).labelText_("Label test").value_(1.0.rand).value.postln;
 *         MultiStepView()
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 */

MultiStepView : View {
	var <>spec, <labelText, <value, valueMapped, <>name;
	var <mainLayoutView, <sliderView, <labelView, <numberBoxView, unitView, controlSpecView, proxy, sliders;

	*new { | spec, parent, bounds |
		^super.new(parent, bounds).init(spec); // use new copyargs
	}

	editMode_ { |mode|
		// Set editMode. For example: set the amount of steps.
		// Off value // On value
		// for \rest is off and \note is on
	}

	uiMode { |mode|

	}

	randomize {
		sliders do: { |slider| slider.value = 1.0.rand };
	}

	registerProxy { |context|
		//
		// name
	}

	init { | spec, initVal, labelText, uiMode |
		mainLayoutView = HLayout();
		this.layout = mainLayoutView;
		this.layout.margins_(0!4).spacing_(0);

		proxy = List.newUsing(0!8);
		sliders = List();
		this.spec = spec;

        // defaults
		if (this.spec.isNil, { this.spec = ControlSpec(); });

		proxy.size do: { |i|
			var newStep = Button().states_([["off"], ["on"]]).action_({ |sender| proxy[i] = sender.value; });
			sliders.add(newStep);
			this.layout.add(newStep);
		};

		mainLayoutView.margins = 0!4;

	    this.labelText = labelText;
	}

	getProxies {
		var result = Dictionary();
		result[this.name.asSymbol] = proxy;
		^result;
	}

	value_ { |v|
		value = v;
		sliderView.value = value;
		numberBoxView.value = this.spec.map(value);
	}

	labelText_ { |text|

	}
}

