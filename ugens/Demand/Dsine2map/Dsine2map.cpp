/*
Marinus Klaassen 2012 
Contains a really simple demand rate if else expression for as a demand rate ugen.
*/

#include "SC_PlugIn.h"
#include <cstdio>
#include <cmath>

static InterfaceTable *ft;

struct Dsine2map : public Unit
{
	double m_repeats;
	int32 m_repeatCount;
	float x,y;
	};

struct Dsinemap : public Unit
{
	double m_repeats;
	int32 m_repeatCount;
	float x;
};


extern "C" 
{
	void Dsinemap_Ctor(Dsinemap* unit);
	void Dsinemap_next(Dsinemap *unit, int inNumSamples);
	void Dsine2map_Ctor(Dsine2map* unit);
	void Dsine2map_next(Dsine2map *unit, int inNumSamples);
};

void Dsinemap_Ctor(Dsinemap* unit)
{
	unit->x = 1;
	SETCALC(Dsinemap_next);
	Dsinemap_next(unit, 0);
	OUT0(0) = 0.f;
}

void Dsine2map_Ctor(Dsine2map* unit)
{
	unit->x = 1;
	unit->y = 1; 
	SETCALC(Dsine2map_next);
	Dsine2map_next(unit, 0);
	OUT0(0) = 0.f;
}

void Dsine2map_next(Dsine2map *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		
		unit->m_repeatCount++;
		
		float r1 = DEMANDINPUT_A(1, inNumSamples);
		float r2 = DEMANDINPUT_A(1, inNumSamples);
		
		float x,y,xprev,yprev; 
	
		xprev = unit->x;
		yprev = unit->y; 
		
//		printf ("%f \n", input);
//		printf ("%i \n", boolean);
//		printf ("%f \n", test);
//		printf ("%f \n", value_if_true);
//		printf ("%f \n", value_if_false);
		
		x = sin(r1 * yprev); 
		y = sin(r2 * xprev); 
				
		OUT0(0) = x;
		
		unit->x = x;
		unit->y = y;

	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}

void Dsinemap_next(Dsinemap *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		
		unit->m_repeatCount++;
		
		float r = DEMANDINPUT_A(1, inNumSamples);
		
		float x,xprev; 
		
		xprev = unit->x;
				
		//		printf ("%f \n", input);
		//		printf ("%i \n", boolean);
		//		printf ("%f \n", test);
		//		printf ("%f \n", value_if_true);
		//		printf ("%f \n", value_if_false);
		
		x = sin(r * xprev); 

		
		OUT0(0) = x;
		
		unit->x = x;
		
	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}


PluginLoad(Demand) {
	ft = inTable;
	DefineSimpleUnit(Dsinemap);
	DefineSimpleUnit(Dsine2map);
}






