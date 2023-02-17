/*
 * FILENAME: SCMRangeSliderView
 *
 * DESCRIPTION:
 *         Based on the sclang EZSlider, however it's a child of View and the child views are organised by a layout, so it can scale.
 *
 *         SCMRangeSliderView(nil, 400@100, \db.asSpec, 0.5, "test").front().action_({ |v| v.postln; }).labelText_("Label test").value_(1.0.rand).value.postln;
 *         SCMRangeSliderView()
 *
 * AUTHOR: Marinus Klaassen (2012, 2021Q3)
 */

SCMRangeSliderView : SCMViewBase {
	var <spec, <labelText, <range, <rangeMapped, valueLoProxy, valueLoMappedProxy, valueHiProxy, valueHiMappedProxy;
	var <mainLayoutView, <rangeSliderView, <lo, <hi, loMapped, hiMapped, proxyLo, proxyHi, controlSpecView; // spec in deze scope, print argument list.

	*new { | parent, bounds, spec, initVal, labelText, uiMode |
		^super.new(parent, bounds).init(spec, initVal, labelText, uiMode);
	}

	uiMode { |uiMode|
	}

	editMode_ { |mode|
	}

	randomize {
		this.lo = 1.0.rand;
		this.hi = 1.0.rand;
    }

	toLow {
		this.lo = 0;
		this.hi = 0;
	}

	toHigh {
		this.lo = 1;
		this.hi = 1;
	}

	toCenter {
		this.lo = 0.5;
		this.hi = 0.5;
	}

    spec_ { |argSpec|
		spec = argSpec;
		this.lo = lo;
		this.hi = hi;
	}

	init {
		spec = ControlSpec();
		needsControlSpec = true;
		valueLoProxy = PatternProxy();
		valueLoMappedProxy = PatternProxy();
		valueHiProxy = PatternProxy();
		valueHiMappedProxy = PatternProxy();
		mainLayoutView = GridLayout();
		this.layout = mainLayoutView;
		rangeSliderView = RangeSlider()
		.orientation_(\horizontal)
		.action_({ |sender|
			this.lo = sender.lo;
			this.hi = sender.hi;
		});
		mainLayoutView.addSpanning(rangeSliderView, row: 1, column: 0, rowSpan: 1, columnSpan: 4);
		mainLayoutView.margins = 0!4;
		// Set init values
		this.lo = 0;
		this.hi = 1;
	}

	lo_ { |argLo|
		lo = argLo;
		loMapped = spec.map(lo);
		rangeSliderView.lo = lo;
		valueLoProxy.source = lo;
		valueLoMappedProxy.source = loMapped;
	}

	hi_ { |argHi|
		hi = argHi;
		hiMapped = spec.map(hi);
		rangeSliderView.hi = hi;
		valueHiProxy.source = hi;
		valueHiMappedProxy.source = hiMapped;
	}

	getProxies {
		var result = Dictionary();
		result[asSymbol(this.name ++ "Lo")] = valueLoMappedProxy;
		result[asSymbol(this.name ++ "Hi")] = valueHiMappedProxy;
		^result;
	}

	getState {
		var state = Dictionary();
		state[\lo] = this.lo;
		state[\hi] = this.hi;
		^state;
    }

    loadState { |state|
		this.lo = state[\lo];
		this.hi = state[\hi];
    }
}

