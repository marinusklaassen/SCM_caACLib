/*
FILENAME: StaticTextFactory

DESCRIPTION: Factory class

AUTHOR: Marinus Klaassen (2021Q4)
*/

StaticTextFactory {

	*createInstance { |caller, class, labelText|
		var newInstance = StaticText();
		class = class.asString();

		if (labelText.notNil, {
			newInstance.string = labelText;
		});
		if (class.contains("label-form"), {
			newInstance.align_(\right);
		});
		if (class.contains("columnlabel-patternbox"), {
			newInstance.font = Font("Menlo", 12, true, true, true);

			if (labelText == "NAME", {
				newInstance.maxWidth = 88;
				newInstance.minWidth = 88;
			});
			if (labelText == "PTID", {
				newInstance.maxWidth = 53;
				newInstance.minWidth = 53;
			});
			if (labelText == "SELECTORS", {
				newInstance.maxWidth = 145;
				newInstance.minWidth = 145;
			});
		});
		if (class.contains("columnlabel-patternbox-move"), {
				newInstance.maxWidth = 50;
				newInstance.minWidth = 50;
		});
		^newInstance;
	}
}
