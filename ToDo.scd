// To add

// Waveform view and Signal Feature Extraction Tools.

Buffer().as(SoundFile)

s.boot;
SoundFile.asBuffer

s.boot;

// Show a soundfile!!
(
w = Window().front;
b = SoundFileView(w, w.bounds.extent);
b.gridOn = false;
c = Buffer.read(s,"/Users/MarinusKlaassen/Desktop/Novation Heritage Soundpack novationmusic.com/Bass Station/Loops/NOVHTG 120 A - BassStation I - Epic Saw.wav"
);
c.loadToFloatArray(action: { arg array; a = array; {b.data = array; }.defer; "done".postln;});
)


r = CompositeView(w, Rect(5, 310, 594, 105));
r.background = Color.grey;
t = SoundFileView(r, r.bounds.extent);
t.gridOn = false;
u = Buffer.read(s, "/Users/MarinusKlaassen/Desktop/Novation Heritage Soundpack novationmusic.com/UltraNova/Loops/NOVHTG 110 F - Ultranova - Evil Clown.wav"

);
u.loadToFloatArray(action: { arg array; {t.data = array; }.defer; "done".postln;});
