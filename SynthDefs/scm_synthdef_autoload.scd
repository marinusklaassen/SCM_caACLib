/*
FILENAME: scm_synthdef_autoload

DESCRIPTION: Autoloading synthdefs after startup

AUTHOR: Marinus Klaassen (2022)
*/

(
SynthDef(\SCM_PlayBuf_Perc, {
	| bufnum=0, decay=1, rate=1, pos=0, boost=0, amp=1, pan=0, out=0, dur=1 |
	var sig = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * rate, loop: 0, startPos: pos * BufFrames.kr(bufnum));
	var env = EnvGen.kr(Env.perc(0.003, decay, curve: -3), doneAction: 2);
	sig = sig * env;
	dur.poll;
	sig = tanh(sig * (1 + boost));
	OffsetOut.ar(out, Pan2.ar(sig, pan, amp));
}).store();
);

(
SynthDef(\SCM_FM_Perc, {
	| freq=200, hr=4, md=24, rise=0.001, decay=1, curve=(-4), boost=0, amp=0.1, out=0, pan=0 |
	var env = EnvGen.kr(Env.perc(rise, decay, curve: curve), 1, doneAction: 2);
	var mfreq = (freq * hr.reciprocal);
	var sig = PMOsc.ar(freq, mfreq, md, 0, env);
	sig = tanh(sig * (1 + boost));
	OffsetOut.ar(out, Pan2.ar(sig, pan, amp));
}).store();
);

(
SynthDef(\SCM_Saw_Perc, { |freq=50, cutoff=5000, decay=1, rise=0.005, combDecay=1, curve=2, amp=0.1, out=0, pan=0|
	var env = EnvGen.kr(Env.perc(rise, decay, curve: curve));
	var sig = RLPF.ar(Saw.ar(freq), cutoff, 0.3) * env;
	sig = CombC.ar(sig, 1, 0.125, combDecay);
	DetectSilence.ar(sig, 0.01, doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(sig * amp, pan));
}).store();
);

(
SynthDef(\SCM_DrumSynth_BD, {
	| freqBase=56, freqClick=500, freqRise=0.005, freqDecay=0.05, freqCurve=(-7), rise=0.005, decay=0.5, curve=(-4), boost=0, pan=0, amp=0.5, out=0 |
	var ampEnv = EnvGen.ar(Env.perc(rise, decay, curve: curve), doneAction: 2);
	// linexp freq results on a nice steep slope. Perc curve with a large minus value didn't do the job.
	var freqEnv = EnvGen.ar(Env.perc(freqRise, freqDecay, curve: freqCurve), doneAction: 0).linexp(0, 1.0, freqBase, freqClick);
	var sig = SinOsc.ar(freqEnv, pi/2, mul: ampEnv);
	sig = Limiter.ar(sig, 0.5);
	sig = tanh((boost + 1) * sig);
	sig = Pan2.ar(sig, pan, amp);
	OffsetOut.ar(out, sig);
}, metadata: (
	specs: (
		freqBase: ControlSpec(20, 500, 'exp'),
		freqClick: \freq.asSpec,
		rise: ControlSpec(0.001, 0.1, 'exp'),
		decay: ControlSpec(0.001, 5.0, 'exp'),
		freqRise: ControlSpec(0.001, 0.1, 'exp'),
		freqDecay: ControlSpec(0.001, 1, 'exp')
))).store();
);

(
SynthDef(\SCM_DrumSynth_SN, {
	| rise=0.003, decay=0.2, curve=(-7), boost=0, pan=0, amp=0.5, out=0 |
	var sig = PinkNoise.ar(EnvGen.ar(Env.perc(rise, decay, curve: curve), doneAction: 2));
	sig = tanh((boost + 1) * sig);
	sig = Pan2.ar(sig, pan, amp);
	OffsetOut.ar(out, sig);
}, metadata: (
	specs: (
		rise: ControlSpec(0.001, 0.1, 'exp'),
		decay: ControlSpec(0.003, 5.0, 'exp')
))).store();
);

(
SynthDef(\SCM_DrumSynth_HH, {
	| freq=10000, rise=0.005, decay=0.1, curve=(-7), boost=1, pan=0, amp=0.5, out=0 |

	var sig = WhiteNoise.ar(EnvGen.ar(Env.perc(rise, decay, curve: curve), doneAction: 2));
	sig = HPF.ar(sig, freq);
	sig = tanh((boost + 1) * sig);
	sig = Pan2.ar(sig, pan, amp);
	OffsetOut.ar(out, sig);
}, metadata: (
	specs: (
		rise: ControlSpec(0, 0.1, 'exp'),
		decay: ControlSpec(0, 5.0, 'exp'),
		amp: ControlSpec(0.3, 2, 'exp'),
		boost: ControlSpec(1, 50, 'exp')
))).store();
);

/* EOF */
