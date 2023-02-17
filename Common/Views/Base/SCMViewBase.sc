/*
keyFILENAME: SCMViewBase

DESCRIPTION: SCMViewBase

AUTHOR: Marinus Klaassen (2022Q2)

EXAMPLE:

SCMViewBase().front;
*/

SCMViewBase : View {
	var <>needsControlSpec = true;

	dispose { }

	randomize { }

	toLow { }

	toHigh { }

	toCenter { }

	spec_ { |spec| }

	uiMode { |mode| }

	editMode_ { |mode| }

	setMainSequencerPosition { |position| }

	setMainSequencerMode { |mode| }
}
