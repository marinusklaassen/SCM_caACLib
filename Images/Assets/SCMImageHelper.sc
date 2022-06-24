/*
FILENAME: SCMImageHelper

DESCRIPTION: SCM Image Helper

AUTHOR: Marinus Klaassen (2022Q2)
*/

SCMImageHelper {
	classvar <imagesDirectory;

	*initClass {
		imagesDirectory = PathName.new(this.filenameSymbol.asString).pathOnly;
	}

	*mixerFaderSprite {
		^Image.openSVG(imagesDirectory ++ "MixerFadersSprite.svg", 50@50);
	}
}
