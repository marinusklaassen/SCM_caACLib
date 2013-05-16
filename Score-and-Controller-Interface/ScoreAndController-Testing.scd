
// FIRST CONNECT LEMUR
~lm = MLemurGui.new;
// Connect with Lemur client by setting IP of the ipad or iphone
~lm.connect("192.10.1.2",8002,8000);
// Sets target on the Lemur to send OSC to the IP of this computer
// Change address if nescessary
~lm.set_osctarget(0,"192.10.1.16", NetAddr.langPort);

// RESET LEMUR
~lm.resetAll;

// BOOT SERVER
s.boot

// MAKE A NEW MIXER INSTANCE
~scoreAndMixer.newInstance;

// MAKE A NEW MIXER VIEW
~scoreAndMixer.makeView;


(
~scoreAndMixer.scoreArray do: { |score|
	score.channelArray do: { |channel|
		channel.selectWidget.lemurFlag = false;
	}
}
)

// CLEANUP STUF
~lm.disconnect; s.quit;
