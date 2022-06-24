/*
FILENAME: SCMAudioHelper

DESCRIPTION: SCM Audio Helper

AUTHOR: Marinus Klaassen (2022Q2)

b = Buffer.readChannel(s, SCMAudioHelper.fullpathAudiofile("amen-loop.wav").postln, channels: 1);
b.plot;
b.play;
*/

SCMAudioHelper {
	classvar <audioDirectory;

	*initClass {
		audioDirectory = PathName.new(this.filenameSymbol.asString).pathOnly;
	}

	*fullpathAudiofile { |file|
		^audioDirectory ++ file;
	}
}
