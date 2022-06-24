/*
FILENAME: SCMMessageLabelView

DESCRIPTION: Show and hide messages. Short timed notifications are also possible.

AUTHOR: Marinus Klaassen (2012, 2021Q3)

EXAMPLE:
a = SCMMessageLabelView(bounds:400@50).string_("test").front
a.clear
*/

SCMMessageLabelView : StaticText {

	*new { | parent, bounds |
		^super.new(parent, bounds).visible_(false);
	}

	string_ { |string|
		super.string_(string);
		this.visible = true;
	}

	notify { | message |
		this.string = message;
		fork { 2.wait; { this.clear(); }.defer; }
	}

	clear {
		this.string = "";
		this.visible = false;
	}
}