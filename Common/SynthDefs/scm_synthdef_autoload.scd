/*
FILENAME: scm_synthdef_autoload

DESCRIPTION: Autoloading synthdefs after startup

AUTHOR: Marinus Klaassen (2022)
*/

(
SynthDef(\SCM_PlayBuf_Perc, {
	| bufnum=0, decay=1, rate=1, pos=0, boost=0, amp=1, pan=0, out=0 |
	var sig = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * rate, startPos: pos * BufFrames.kr(bufnum));
	var env = EnvGen.kr(Env.perc(0.003, decay, curve: -3), doneAction: 2);
	sig = sig * env;
	sig = tanh(sig * (1 + boost));
	OffsetOut.ar(out, Pan2.ar(sig, pan, amp));
}).store();
);

(
SynthDef(\SCM_PlayStretch, { |bufnum=0, pos=0, rate=1, time=1, stretch=1, boost=01 atk=0.01, rel=0.05, amp=1, pan=0, out=0|
	var sig = PlayBuf.ar(1, bufnum, rate: rate * stretch.reciprocal, startPos: pos * BufFrames.kr(bufnum), doneAction:2);
	var env = EnvGen.kr(Env.linen(atk, time, rel), doneAction: 2);
	sig = PitchShift.ar(sig, pitchRatio: stretch);
	sig = sig * env;
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
Scale.choose

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
	var sig = SinOsc.ar(freqEnv, pi/2, ampEnv); // TODO select LFPulse.ar
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
	| rise=0.003, decay=0.2, freq=2000, rq=3, curve=(-7), boost=0, pan=0, amp=0.5, out=0 |
	var sig = PinkNoise.ar();
	var env = EnvGen.ar(Env.perc(rise, decay, curve: curve), doneAction: 2);
	sig = BPF.ar(sig, freq, rq, env);
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
// Based on https://sccode.org/1-57f
SynthDef(\SCM_DrumSynth_SN2, { |burstFreqLo=300, burstFreqHi=8000, burstDecay=0.02,
	freqStartClick=3000, freqEndClick=1500, clickDecay=0.01,
	rattleFreq=4000, rattleDecay=0.1,
	bodyFreqStart=500, bodyFreqEnd=196, bodyGlideDur=0.04, bodyDecay=0.2,

	snappy = 1, body = 1, click=1, rattle=1, boost=6, pan=0, amp=0.1, out=0 |
	var noise = WhiteNoise.ar();
	var noiseBurst = LPF.ar(HPF.ar(noise, burstFreqLo), burstFreqHi) * Env.linen(0.001, burstDecay, 0.001).ar();
	var snareBody = SinOsc.ar(XLine.ar(bodyFreqStart, bodyFreqEnd, bodyGlideDur), mul: Env.perc(0.003, bodyDecay).ar() * 6.dbamp).tanh;
    var sigRattle = (HPF.ar(BPeakEQ.ar(noise, rattleFreq, 0.5, 3), burstFreqLo) * Env.perc(0.05, rattleDecay).delay(0.01).ar(2) * -3.dbamp);
	var sigClick =  SinOsc.ar(XLine.ar(freqStartClick, freqEndClick, clickDecay)) * Env.perc(0.003, clickDecay).ar();
	var sig = (noiseBurst + sigRattle * snappy) + (snareBody * body) + (sigClick * click);
	sig = softclip(sig * (1 + boost));
    OffsetOut.ar(out, Pan2.ar(sig, pan, amp));
}).store();
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

// https://www.soundonsound.com/synthesizers/synth-secrets?f%5B0%5D=node%253Afield_section%3A6971
// https://www.soundonsound.com/techniques/synthesizing-bells
(
SynthDef(\SCM_DrumSynth_HH2, {
	| freq=40, width=0.5, rise=0.00001, decay=0.2, curve=(-50), bpff=10000, bpfrq=0.2, hpff=7000, boost=1, amp=0.2, pan=0, out=0|

	var env = EnvGen.ar(Env.perc(rise, decay, curve), doneAction: 2);
    var sig = Mix.ar(
		[1, 2, 3, 4.16, 5.43, 6.79, 8.21] collect: { |ratio|
			Pulse.ar(freq * ratio, width, 1/7);
	});
	sig = env * sig;
	sig = BPF.ar(sig, bpff, bpfrq);
	sig = HPF.ar(sig, hpff);
	sig = tanh((boost + 1) * sig);
	sig = Pan2.ar(sig, pan, amp);

	OffsetOut.ar(out, sig);
}).store;
);

(
SynthDef(\SCM_DrumSynth_Cymbal, {
	| freq=40, width=0.5, rise=0.00001, decay=0.2, dur=0.5, bpff=10000, bpfrq=0.2, hpff=7000, boost=1, amp=0.2, pan=0, out=0|

	var env = EnvGen.ar(Env.new([0.001, 1, 1, 0.001], [0.01, dur, 0.01], curve: \exp), doneAction: 2);
    var sig = Mix.ar(
		[1, 2, 3, 4.16, 5.43, 6.79, 8.21] collect: { |ratio|
			Pulse.ar(freq * ratio, width, 1/3);
	});
	sig = env * sig;
	sig = BPF.ar(sig, bpff, bpfrq);
	sig = HPF.ar(sig, hpff);
	sig = tanh((boost + 1) * sig);
	sig = Pan2.ar(sig, pan, amp);

	OffsetOut.ar(out, sig);
}).store;
);

(
SynthDef(\SCM_Pulse_ASR,
	{ | freq=440, pulseWidth=0.5, rise=0.1, rel=0.5, filterFreq=8000, filterReson=20, curve=(-7), boost=1, pan=0, amp=0.5, out=0, gate=1 |

		var ampEnv = EnvGen.ar(Env.asr(attackTime: rise, releaseTime: rel, curve: curve), gate: gate, doneAction: 2);
		var sig = Pulse.ar(freq: freq, width: pulseWidth, mul: ampEnv);
		sig = RLPF.ar(in: sig, freq: filterFreq, rq: reciprocal(filterReson));
		sig = tanh((boost + 1) * sig);
		sig = Pan2.ar(sig, pan, amp);
		OffsetOut.ar(out, sig);
	}, metadata: (
		specs: (
			rise: ControlSpec(0, 0.1, 'exp'),
			rel: ControlSpec(0, 5.0, 'exp'),
			filterReson: ControlSpec(0, 100),
			boost: ControlSpec(1, 50, 'exp')
))).store();
);

(
SynthDef(\SCM_Pulse_PERC,
	{ | freq=440, pulseWidth=0.5, rise=0.1, decay=0.5, filterFreq=8000, filterReson=20, curve=(-7), boost=1, pan=0, amp=0.5, out=0, gate=1 |

		var ampEnv = EnvGen.ar(Env.perc(attackTime: rise, releaseTime: decay, curve: curve), doneAction: 2);
		var sig = Pulse.ar(freq: freq, width: pulseWidth, mul: ampEnv);
		sig = RLPF.ar(in: sig, freq: filterFreq, rq: reciprocal(filterReson));
		sig = tanh((boost + 1) * sig);
		sig = Pan2.ar(sig, pan, amp);
		OffsetOut.ar(out, sig);
	}, metadata: (
		specs: (
			rise: ControlSpec(0, 0.1, 'exp'),
			rel: ControlSpec(0, 5.0, 'exp'),
			filterReson: ControlSpec(0, 100),
			boost: ControlSpec(1, 50, 'exp')
))).store();
);

(
SynthDef(\SCM_Sine_PERC_TG,{ | freq = 1000, gate=1, t_trig=1, boost=0, amp=0.1, pan=0, out=0 | // in pbind t_trig is just trig
	var env = Decay2.kr(t_trig;, 0.01, 0.1);
	var sig = SinOsc.ar(freq, 0, env);
	sig = tanh(sig * (1 + boost)) * amp;
	sig = sig * Linen.kr(gate, 0.01, 0.1, 0.1, doneAction: 2);
	OffsetOut.ar(0, Pan2.ar(sig, pan));
}).store();
);

(
SynthDef(\SCM_303 , {
	| freq=440, freqLag=0, waveform=0, ffreq=100, ffreqRange=1000, resonance=0.2, boost=1,
	  pulseWidth=0.5, hold=0, decay=1, delay=0.1, delayAmount=0.2, feedback=0.5, out=0, pan=0, gate=1, amp=0.1, t_trig=1 |

	var  envAmp, envGrainAmp, envGrainFilter, sig, sigFeedback;

	// Control stuf
    envAmp = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction: 2);
	envGrainAmp = EnvGen .ar(Env.new([10e-10, 1, 1, 10e-10], [0.01, hold, decay],  'exp' ), t_trig);
	envGrainFilter  = EnvGen .ar(Env.new([10e-10, 1, 10e-10], [0.01, decay],  'exp' ), t_trig);
	freq = Lag.ar(K2A.ar(freq), freqLag);

	// Synthesis
	sig = Select.ar(waveform, [Saw.ar(freq), Pulse.ar(freq, pulseWidth), LFTri.ar(freq)]) * envGrainAmp * envAmp;
	sig = RLPF.ar(sig, ffreq + (ffreqRange * envGrainFilter), resonance);
	// Boost
	sig = tanh(sig * (1 + boost));

	// Feedback delay (a bit non-standard, but sounds good)
	sigFeedback = LocalIn.ar(1);
	sigFeedback = DelayL.ar(sigFeedback, 0.2, Lag.ar(K2A.ar(delay), freqLag + 0.02));
	sig = sig + (delayAmount * sigFeedback);
	LocalOut.ar(sig * feedback);
	// End feedback delay

	Out.ar(out, Pan2.ar(sig, pan, amp));

}).store();
);

(
SynthDef(\SCM_404 , {  | freq=440, lag=0, waveform=0, ffreq=100, ffreqRange=1000, resonance=0.5, boost=1,
	pulseWidth=0.5, hold=0, decay=1, delay=0.1, mix=0.2, reverbAmount=0.2, feedback=0.5, out=0, pan=0, gate=1, amp=0.1, t_trig=1 |
	// TODO maak zonder grain amp.
	var  envAmp, envGrainAmp, envGrainFilter, sig, sigFeedback;
	// Control stuf
    envAmp = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction: 2);
    envGrainAmp = EnvGen.ar(Env.new([10e-10, 1, 1, 10e-10], [0.01, hold, decay],  'exp' ), t_trig);
	envGrainFilter  = EnvGen .ar(Env.new([10e-10, 1, 10e-10], [0.01, decay],  'exp' ), t_trig);
	ffreq = Lag.kr(freq, lag);
    // Synthesis
	sig = BPF.ar(LFNoise0.ar(freq, 0.2 * envGrainAmp), ffreq + (ffreqRange * envGrainFilter), resonance);
	// Boost
	sig = tanh(sig * (1 + boost)) * amp;
    // Feedback delay (a bit non-standard, but sounds good)
	sigFeedback = LocalIn.ar(1);
	sigFeedback = DelayL.ar(sigFeedback, 0.2, Lag.kr(delay, lag + 0.02));
	sig = sig + (mix * sigFeedback);
	sig = (Lag.kr(reverbAmount, lag) * RLPF.ar(JPverb.ar(sig, size: 20),  ffreq+ envGrainFilter));
	LocalOut.ar(sig * feedback);
	// End feedback delay
	Out.ar(out, Pan2.ar(sig, Lag.kr(pan, lag)));
}).store();
);

// https://www.soundonsound.com/techniques/practical-snare-drum-synthesis#para2



(
SynthDef(\SCM_Karplus, { |freq=110, burstDecay=0.05, delayTime=2, boost=0,  amp=0.1, pan=0, out|
    var burst, env, sig;
    burst = Decay.ar(Impulse.ar(0), burstDecay, WhiteNoise.ar);
	sig = CombL.ar(burst, 0.05, freq.reciprocal, delayTime);
    sig = LeakDC.ar(sig);
	DetectSilence.ar(sig, doneAction: 2);
	sig = tanh(sig * (1 + boost));
	OffsetOut.ar(out, Pan2.ar(sig, pan, amp));
}).store();
);

/* EOF */
