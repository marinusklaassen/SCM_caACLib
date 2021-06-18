MBNumberBox {
	var gui, <name, <>action, <value;

	*new { ^super.newCopyArgs.init; }

	init {
		name = "default";
		value = 0;
	}

	makeGui { |parent,bounds|
		var boxWidth = bounds.asRect.width - 180;
		bounds = bounds.asRect;
		gui = Dictionary.new;
		gui[\canvas] = CompositeView(parent,bounds);
		gui[\numberBox] = NumberBox.new(gui[\canvas],Rect(60,0,boxWidth,bounds.height));
		gui[\numberBox].action_({ |num| if (action.notNil) { action.value(num.value); value = num.value } });
		gui[\numberBox].value = value;
		gui[\nameView] = StaticText.new(gui[\canvas],Rect(4,0,54,bounds.height));
		gui[\nameView].string_(name);
	}

	value_ {|argValue|
		gui[\numberBox].value = argValue;
	}

	name_ {|argName|
		name = argName;
		if (gui.notNil) { gui[\nameView].string_(name); };
	}
}