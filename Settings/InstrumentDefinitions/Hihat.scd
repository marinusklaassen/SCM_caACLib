// SynthDef Definition
(
SynthDef(\Hihat, { | out, freq = 10000, gate = 1, atk = 0.003, dec = 0.4, curveParam = -4, pan = 0, amp = 0.1|
	var env = EnvGen.kr(Env.asr(atk, 1, dec, curveParam), gate, doneAction: 2);
	var sig = HPF.ar(WhiteNoise.ar(1), freq);
	sig = Pan2.ar(sig, pan, amp);
	Out.ar(out, sig);
}).store;
)

// Call and process SynthDef metadata

(
m = SynthDesc.readDef(\Hihat); if (m.metadata.isNil) { m.metadata = (); };
m.metadata[\specs] = (
	atk: ControlSpec(0.001, 2, \exp),
	dec: ControlSpec(0.01, 2, \exp),
	curveParam: ControlSpec(-20, 0),
	freq: ControlSpec(2000, 16000, \exp),
);

// Ignore key list
m.metadata[\noGui] = [\gate];

// Store processed SynthDef metadata
SynthDesc.storeMetaData(\Hihat, m.metadata);
);

// Test SynthDef
EmbedGui(\Hihat).makeGui;