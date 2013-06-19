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
s.reboot
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
~lm.disconnect;
s.quit;



(
SynthDef(\FMpercGrain1, { arg freq = 200, hr = 4, md = 24, dur = 5,curve = -4, amp = 0.1, out, pan = 0;
	var env = EnvGen.kr(Env.perc(0.001,dur,amp,curve),1,doneAction: 2);
	var modFreq = (freq * hr.reciprocal);
	var sig = PMOsc.ar(freq, modFreq, md,0);
	OffsetOut.ar(out, PanAz.ar(4, sig, pan, env));
}, \ir!8).add;

SynthDef(\FMpercGrain1am, { arg freq = 200, hr = 4, md = 24, dur = 5,curve = -4, amp = 0.1, out, pan = 0, amFreq = 40;
	var env = EnvGen.kr(Env.perc(0.001,dur,amp,curve),1,doneAction: 2);
	var modFreq = (freq * hr.reciprocal);
	var sig = PMOsc.ar(freq, modFreq, md,0);
		sig = VarSaw.ar(amFreq,mul: sig,width: 0.5);
	OffsetOut.ar(out, PanAz.ar(4, sig, pan, env));
}, \ir!9).add;

SynthDef(\FMpercGrain1amVib, { arg freq = 200, hr = 4, md = 24, dur = 5,curve = -4, amp = 0.1, out, pan = 0, amFreq = 100, vibFreq = 4, vibPower = 20;
	var env = EnvGen.kr(Env.perc(0.001,dur,amp,curve),1,doneAction: 2);
	var car = freq + SinOsc.ar(vibFreq, 0, vibPower);
	var modFreq = (car * hr.reciprocal);
	var sig = PMOsc.ar(car, modFreq, md,0);
		sig = VarSaw.ar(amFreq,mul: sig,width: 0.5);
	OffsetOut.ar(out, PanAz.ar(4, sig, pan, env));
}, \ir!11).add;

SynthDef(\FMsineGrain1, { arg freq = 200, hr = 4, md = 24, dur = 5,curve = -4, amp = 0.1, out, pan = 0;
	var env = EnvGen.kr(Env.sine(dur,amp),doneAction: 2);
	var modFreq = (freq * hr.reciprocal);
	var sig = PMOsc.ar(freq, modFreq, md,0);
	OffsetOut.ar(out, PanAz.ar(4, sig, pan, env));
}, \ir!8).add;

SynthDef(\FMsineGrain1am, { arg freq = 200, hr = 4, md = 24, dur = 5,curve = -4, amp = 0.1, out, pan = 0, amFreq = 40;
	var env = EnvGen.kr(Env.sine(dur,amp),doneAction: 2);
	var modFreq = (freq * hr.reciprocal);
	var sig = PMOsc.ar(freq, modFreq, md,0);
	sig = VarSaw.ar(amFreq,mul: sig,width: 0.5);
	OffsetOut.ar(out, PanAz.ar(4, sig, pan, env));
}, \ir!9).add;

SynthDef(\FMpercGrain1amVib, { arg freq = 200, hr = 4, md = 24, dur = 5,curve = -4, amp = 0.1, out, pan = 0, amFreq = 100, vibFreq = 4, vibPower = 20;
	var env = EnvGen.kr(Env.perc(0.001,dur,amp,curve),1,doneAction: 2);
	var car = freq + SinOsc.ar(vibFreq, 0, vibPower);
	var modFreq = (car * hr.reciprocal);
	var sig = PMOsc.ar(car, modFreq, md,0);
		sig = VarSaw.ar(amFreq,mul: sig,width: 0.5);
	OffsetOut.ar(out, PanAz.ar(4, sig, pan, env));
}, \ir!11).add;

SynthDef(\FMsineGrain1amVib, { arg freq = 200, hr = 4, md = 24, dur = 5,curve = -4, amp = 0.1, out, pan = 0, amFreq = 40, vibFreq = 4, vibPower = 20;
	var env = EnvGen.kr(Env.sine(dur,amp),doneAction: 2);
	var car = freq + SinOsc.ar(vibFreq, 0, vibPower);
	var modFreq = (car * hr.reciprocal);
	var sig = PMOsc.ar(car, modFreq, md,0);
	sig = VarSaw.ar(amFreq,width: 0.5,mul: 2).distort.sqrt * sig;
	OffsetOut.ar(out, PanAz.ar(4, sig, pan, env));
}, \ir!9).add;
)