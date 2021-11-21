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
				newInstance.maxWidth = 93;
				newInstance.minWidth = 93;
			});
			if (labelText == "SELECTORS", {
				newInstance.maxWidth = 145;
				newInstance.minWidth = 145;
			});
		});
		^newInstance;
	}
}
