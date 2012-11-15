Gosc1 : UGen {
	*ar { arg mass = -400000, vx = 0.0, ax = 0.0, sx = 1, mul = 1, add = 0;
		^this.multiNew('audio', mass,vx, ax, sx).madd(mul,add)

	}
}

/*
s.boot;

{ Gosc1.ar(-1000000.0, 1, 0, 1) }.scope
 */