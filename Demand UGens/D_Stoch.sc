


Dcoin : DUGen {
	*new { arg weight = 0.5, length = inf;
		^this.multiNew('demand', length, weight)
	}
}

Dcoin2 : DUGen {
	*new { arg weight = 0.5, length = inf;
		^this.multiNew('demand', length, weight)
	}
}

Dexpon : DUGen {
	*new { arg lo = 0.0, hi = 1.0, length = inf;
		^this.multiNew('demand', length, lo, hi)
	}
}

Dsumnrand : DUGen {
	*new { arg lo = 0.0, hi = 1.0, n = 1, length = inf;
		^this.multiNew('demand', length, lo, hi, n)
	}
}

Dlinear : DUGen {
	*new { arg lo = 0.0, hi = 1.0, favor = 0, length = inf;
		^this.multiNew('demand', length, lo, hi, favor)
	}
}

Dbeta : DUGen {
	*new { arg lo = 0.0, hi = 1.0, a = 0.2, b = 0.2, length = inf;
		^this.multiNew('demand', length, lo, hi, a, b)
	}
}

Dlogist : DUGen {
	*new { arg lo = 0.0, hi = 1.0, lambda = 3.449449, x = 0.001, length = inf;
		^this.multiNew('demand', length, lo, hi, lambda, x)
	}
}

Dsine : DUGen {
	*new { arg lo = 0.0, hi = 1.0, length = inf;
		^this.multiNew('demand', length, lo, hi)
	}
}


