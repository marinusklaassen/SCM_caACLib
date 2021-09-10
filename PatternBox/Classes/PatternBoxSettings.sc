/* Marinus Klaassen 2021
 *
 * General Score Widget settings in a static class variable.
 *
 * Override settings can be done via de startup file.
 */

ScoreWidgetSettings {
	classvar settings;

	*settings {
		^(
			font: Font("Menlo", 12, true, true, true),
			backgroundColor: Color.new255(* ({ 150 }!3 ++ 230)),
			specSettingColor: nil,
			controlSettinColor: nil,
			chOffset: 240,
			chHeight: 30,
			chGap: 5,
			xName: 10,
			widthName: 70,
			xLayers: 80,
			widthLayers: 415,
			xButtons: 500,
			widthButtons: 140,
			xRemove: 640,
			widthRemove: 15,
		)
	}
}