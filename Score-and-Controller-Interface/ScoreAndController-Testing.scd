// The page ID plus name ID are not combined to create
// an osc-message slot.
// This results in problems with the bidirectional osc
// message flow with multiple scores
// Ding opgelost.
// Parameter linkin is opgelost.







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
s.boot;

(
// MAKE A NEW MIXER INSTANCE
~scoreAndMixer.newInstance;
// Set lemur flag on true
~scoreAndMixer.lemurFlag = true;
// MAKE A NEW MIXER VIEW
~scoreAndMixer.makeView;
)

// View to front
~scoreAndMixer.makeView;




// CLEANUP STUF
~lm.disconnect; s.quit;

s.quit
// Laad FM
Pbind(
	\instrument, \FM1,
	\addAction, 0,
	\buf, Prand(~segBuf,inf),
	\dur, Pexprand(1,13),
	\note,Pn(Pshuf(Pxrand(Scale.choose,inf).asStream.nextN(5),Pexprand(1,7)),inf),
	\amm, Pexprand(0.01,0.1),
	\hr,  1.21,
	\md,  12,
	\pan, Pwhite(-1,1.0),
	\amp, Pexprand(0.1,1.0) * dbamp(Pstutter(Pexprand(10,70), Prand([0,-18],inf))) * 0.1,
/*	\type,Pwrand([\note,\rest], [0.7,0.3],inf),*/
	\legato, 8
).play
(
// Good with fm. make short burst!
b = Ppar({ |i|
	Pbind(
		\instrument, \oneShot1,
		\freq,Pwrand([40,4],[0.99,0.01],inf),
		\dur, reciprocal(Pkey(\freq)),
		\amp, Pseg(Pwhite(0,0.1), Pexprand(0.01,exprand(0.5,2)), \sqr,inf),
		\buf, Pstutter(Pbeta(1,20,0.1,0.1), Purno(~segBuf,inf)),
		\envbuf, ~noEnv,
		\pan, ~spatSpeakerAngles.choose,
		\type, Pstutter(16, Prand([\r, \note],inf))
	)
}!2).play;
)
s.boot;
(
c.stop; c = Pbind(
	\instrument, \KICK,
	/*\dur, Pseq([3/4,1/4,1/8,1/8,1],inf),*/
	\dur, 1/4,
	\plow, 30,
	\phigh, 110,
	\dtime, Pkey(\dur),
	\atime, Pkey(\dur),
	\subtime, Pkey(\dur),
	\curveFactor, -18,
	\distort, 10,
	\amp, 0.2
).play
)


