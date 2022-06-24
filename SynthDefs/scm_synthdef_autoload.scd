/*
FILENAME: scm_synthdef_autoload

DESCRIPTION: Autoloading synthdefs after startup

AUTHOR: Marinus Klaassen (2022)
*/

(
SynthDef(\SCM_Sampler_Oneshot_Percussive, {
	| out=0, bufnum=0, dur=1, rate=1, legato=1, pos=0, boost=0, amp=1, pan=0 |
	var sig = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * rate, loop: 0, startPos: pos * BufFrames.kr(bufnum));
	var env = EnvGen.kr(Env.perc(0.003, legato * dur, curve: -3), doneAction: 2);
	sig = sig * env;
	sig = tanh(sig * (1 + boost));
	OffsetOut.ar(out, Pan2.ar(sig, pan, amp));
}).add;
);

(
SynthDef(\SCM_FM_Basic, {
	| freq=200, hr=4, md=24, dur=1, curve=(-4), boost=0, amp=0.1, out=0, pan=0 |
	var env = EnvGen.kr(Env.perc(0.001, dur, curve: curve), 1, doneAction: 2);
	var mfreq = (freq * hr.reciprocal);
	var sig = PMOsc.ar(freq, mfreq, md, 0, env);
	sig = tanh(sig * (1 + boost));
	OffsetOut.ar(out, Pan2.ar(sig, pan, amp));
}).add;
);

