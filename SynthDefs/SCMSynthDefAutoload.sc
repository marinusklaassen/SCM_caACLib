/*
FILENAME: SCMSynthDefAutoload

DESCRIPTION: SCMSynthDefAutoload

AUTHOR: Marinus Klaassen (2022Q2)
*/

SCMSynthDefAutoload {
	*initClass {
		StartUp.add({
			SCMFileHelper.evaluateSourcesInDirectory(PathName(this.filenameSymbol.asString).pathOnly, "scm_synthdef_autoload.scd", false);
		});
	}
}
