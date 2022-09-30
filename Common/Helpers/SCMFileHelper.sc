/*
FILENAME: SCMFileHelper

DESCRIPTION: Extra file helpers.

AUTHOR: Marinus Klaassen (2022Q2)

EXAMPLE:
SCMFileHelper.evaluateSourcesInDirectory("/somedir/", "abc*\.scd");
*/

SCMFileHelper {
	*evaluateSourcesInDirectory { |directory, pathMatchPattern="*\.scd", recursive=true|
		var pathName = PathName(directory);
		pathName.files do: {  |path|
			try {
				if (pathMatchPattern.matchRegexp(path.fileName), {
					"Loading %".format(path.fullPath).postln();
					load(path.fullPath);
				});
			};
		};
		pathName.folders do: {  |folder|
			this.evaluateSourcesInDirectory(folder.pathOnly, pathMatchPattern, recursive);
		};
	}
}
