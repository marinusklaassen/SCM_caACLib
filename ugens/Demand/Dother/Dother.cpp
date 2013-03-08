/*
Marinus Klaassen 2012 
Contains a really simple demand rate if else expression for as a demand rate ugen.
*/

#include "SC_PlugIn.h"
#include <cstdio>
#include <cmath>

static InterfaceTable *ft;

struct Dif : public Unit
{
	double m_repeats;
	int32 m_repeatCount;
	};

extern "C" 
{
	void Dif_Ctor(Dif* unit);
	void Dif_next(Dif *unit, int inNumSamples);
};

void Dif_Ctor(Dif* unit)
{
	SETCALC(Dif_next);
	Dif_next(unit, 0);
	OUT0(0) = 0.f;
}

void Dif_next(Dif *unit, int inNumSamples)
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
		
		float input = DEMANDINPUT_A(1, inNumSamples);
		int boolean = DEMANDINPUT_A(2, inNumSamples);
		float test = DEMANDINPUT_A(3, inNumSamples);
		float value_if_true = DEMANDINPUT_A(4, inNumSamples);
		float value_if_false = DEMANDINPUT_A(5, inNumSamples);
		
//		printf ("%f \n", input);
//		printf ("%i \n", boolean);
//		printf ("%f \n", test);
//		printf ("%f \n", value_if_true);
//		printf ("%f \n", value_if_false);
		
		switch (boolean) {
			case 0:
				input = (input == test) ? value_if_true : value_if_false; 
				break;
			case 1:
				input = (input <  test) ? value_if_true : value_if_false; 
				break;
			case 2:
				input = (input <= test) ? value_if_true : value_if_false; 
				break;
			case 3:
				input = (input >  test) ? value_if_true : value_if_false; 
				break;
			case 4:
				input = (input >= test) ? value_if_true : value_if_false; 
				break;
		
			default:
				input = (input == test) ? value_if_true : value_if_false; 
				break;
		}

		OUT0(0) = input;	

	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}


PluginLoad(Demand) {
	ft = inTable;
	DefineSimpleUnit(Dif);
}






