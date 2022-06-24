/*
 * FILENAME: Class
 *
 * DESCRIPTION:
 *         Class extension methods.
 *
 * AUTHOR: Marinus Klaassen (2021Q2)
 *
 */

+Class {

	classVarValueByName  { | name |

		var result = nil;
		var index = this.classVarNames.indexOf(name);
		if (index.notNil, {
			result = this.classVars[index];
		});
		^result;
	}
}