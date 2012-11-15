/*
Marinus Klaassen 2012
Version 0.1 of the MLemurGui Class.
This version in the current simple form allows
It is has a similar usage to the Ndef and SynthDef's auto gui functionallity.
Together with the auto gui classes I am writing now it has a two way Model View Control
To embed the lemur app into the SuperCollider code written program's
I am not trying to remake the Lemur Editor but I am writing it as a extension
If you have suggestion contact me via GitHub.

a = MLemurGui.new;
a.connect("192.10.1.2");
a.resetAll;
a.addPage;
a.addPage("test page 2");
a.disconnect;
a.removePage;
a.removePage("test page 2")
a.addFader("thing");
a.removeFader;

a.buildDemo(
	[\Fader, \Fader, \Range, \Range, \Fader, \Fader],
	["tempo", "distortion", "frequency", "mask", "reverb", "distortion"],
	"synth setup 1", 2);

*/

MLemurGui {
	classvar connections, buildPort = 8002, oscPort = 8000;
	var <current_ip, <buildInfo;

	*initClass {
		connections = IdentityDictionary.new;
	}

	*new { ^super.new; }

	connect { |ip = "192.10.1.2"|
		if (connections[ip] == nil, {
			connections[ip] = [
				NetAddr(ip,buildPort).connect,
				NetAddr(ip,oscPort)
			];
		});

		current_ip = ip; // current ip is stored in an instance variable to
		current_ip.postln;
		connections.postln;
		buildInfo = IdentityDictionary.new;
	}

	disconnect {
		connections[current_ip] do: (_.disconnect);
		connections[current_ip] = nil;
	}

	sendPacket { |message|
		if (connections[current_ip].notNil, {
		connections[current_ip][0].sendMsg("/jzml",message.ascii.add(0).as(Int8Array));
			},{
				"evaluate method .connect".postln;
		});
	}

	resetCode { ^"<RESET/>" }

	resetAll {
		// reset lemur and set the scheme name inside lemur-app
		var string = '<RESET/><OSC request="1"/><SYNCHRO mode="0"/><PROJECT title="more beer" version="3030" width="1024" height="724" osc_target="-2" midi_target="-2" kbmouse_target="-2"/>'.asString;

		this.sendPacket(string);
	}

	addPageCode { |pagename = "default name", x = 0, y = 0, width = 1024, height = 724|
		// name is also used when selecting a page name here and it is also the id name
		var string =
		'<WINDOW class="JAZZINTERFACE" text="%" x="%" y="%" width="%" height="%" state="1" group="0" font="tahoma,11,0" >'.asString.format(pagename, x, y, width, height);
		^string;
	}

	removePageCode { |pagename = "default name"|
		// this xml code removes a page of "name"
		var string =
		'<DELETE> <WINDOW class="JAZZINTERFACE" text="%" id="1" state="1" group="0"> </WINDOW> </DELETE>'.asString.format(pagename);
		^string;
	}

	addPage { |pagename = "default name", x = 0, y = 0, width = 1024, height = 724|
		this.sendPacket("<JZML>" ++ this.addPageCode(pagename,x,y,width,height) ++ "<JZML>");
	}

	// remove page doen't work..
	removePage { |pagename = "default name"|
		this.sendPacket(postln("<JZML>" ++ this.removePageCode(pagename) ++ "<JZML>"));
	}

	addFaderCode { | idname = "Fader1", x = 6, y = 15, width = 100, height = 678 |
		// this will add a green slider.
		var string =
		'<WINDOW class="Fader" text="%" x="%" y="%" width="%" height="%" id="1" state="1" group="0" font="tahoma,10,0" send="1" osc_target="-2" midi_target="-2" kbmouse_target="-2" capture="1" color="32768" cursor="0" grid="0" grid_steps="1" label="0" physic="1" precision="3" unit="" value="0" zoom="0.000000"> <PARAM name="x=" value="0.000000" send="17" osc_target="0" osc_trigger="1" osc_message="/%/x" midi_target="-1" midi_trigger="1" midi_message="0x90,0x90,0,0" midi_scale="0,16383" osc_scale="0.000000,1.000000" kbmouse_target="-1" kbmouse_trigger="1" kbmouse_message="0,0,0" kbmouse_scale="0,1,0,1"/> <PARAM name="z=" value="0.000000" send="17" osc_target="0" osc_trigger="1" osc_message="/%/z" midi_target="-1" midi_trigger="1" midi_message="0x90,0x90,0,0" midi_scale="0,16383" osc_scale="0.000000,1.000000" kbmouse_target="-1" kbmouse_trigger="1" kbmouse_message="0,0,0" kbmouse_scale="0,1,0,1"/> </WINDOW>'.asString.format(idname, x, y, width, height, idname, idname);
		^string;
	}

	removeFaderCode { |idname = "Fader1"|
		var string = '<DELETE> <WINDOW class="Fader" text="%" id="1" <DELETE>'.asString.format(idname);
		^string;
	}

	addFader { | idname = "Fader1", x = 6, y = 15, width = 100, height = 678 |
		this.sendPacket("<JZML>" ++ this.addFaderCode(idname,x,y,width,height) ++ "<JZML>");
	}

	removeFader { |idname = "Fader1"|
		this.sendPacket("<JZML>" ++ this.removeFaderCode(idname) ++ "<JZML>");
	}

	addRangeCode { |idname = "Range1", x = 6, y = 15, width = 100, height = 678|
		// this will add a range slider
		^'<WINDOW class="Range" text="%" x="%" y="%" width="%" height="%" id="1" state="1" group="0" font="tahoma,10,0" send="1" osc_target="-2" midi_target="-2" kbmouse_target="-2" capture="1" color="32768" grid="0" grid_steps="1" horizontal="0" label="0" physic="0"> <PARAM name="x=" value="0.250000,0.750000" send="17" osc_target="0" osc_trigger="1" osc_message="/%/x" midi_target="-1" midi_trigger="1" midi_message="0x90,0x90,0,0" midi_scale="0,16383" osc_scale="0.000000,1.000000" kbmouse_target="-1" kbmouse_trigger="1" kbmouse_message="0,0,0" kbmouse_scale="0,1,0,1"/>'.asString.format(idname, x, y, width, height, idname);

	}

	removeRangeCode { |idname = "Range1"|
		^'<DELETE> <WINDOW class="Range" text="%" id="1" <DELETE>'.asString.format(idname);
	}

	addRange { | idname = "Range1", x = 6, y = 15, width = 100, height = 678 |
	this.sendPacket("<JZML>" ++ this.addRangeCode(idname,x,y,width,height) ++ "<JZML>");
	}

	removeRange { |idname = "Range1"|
		this.sendPacket("<JZML>" ++ this.removeRangeCode(idname) ++ "<JZML>");
	}

	addTextCode { |idname = "Text1", content = "parname", x = 6, y = 129, width = 100, height = 48|
	// this will add a text gui
		^'<WINDOW class="Text" text="%" x="%" y="%" width="%" height="%" id="1" state="245" group="0" font="tahoma,11,0" send="1" osc_target="-2" midi_target="-2" kbmouse_target="-2" color="32768" content="%"> <VARIABLE name="light=0" send="0" osc_target="0" osc_trigger="1" osc_message="/Text1/light" midi_target="-1" midi_trigger="1" midi_message="0x90,0x90,0,0" midi_scale="0,16383" kbmouse_target="-1" kbmouse_trigger="1" kbmouse_message="0,0,0" kbmouse_scale="0,1,0,1"/> </WINDOW>'.asString.format(idname, x,y,width,height,content);
	}

	addText { | idname = "Text1", content = "parname", x = 6, y = 129, width = 100, height = 48|
	this.sendPacket("<JZML>" ++ this.addTextCode(idname,content,x,y,width,height) ++ "<JZML>");
	}

	removeText { |idname = "Range1"|
		this.sendPacket("<JZML>" ++ this.removeTextCode(idname) ++ "<JZML>");
	}

	set_osctarget { |target_number=0,ip_host="192.10.1.16",port=57120|
	this.sendPacket('<OSC target=\"%\" ip=\"%\" port=\"%\"/>'.asString.format(target_number,ip_host,port));
	}

	buildBind { | bindGui, pageName, id = 1|
		var type,snippets,idNames,typeArray,parNames;
		typeArray = bindGui.typeArray; parNames = bindGui.nameArray;

		if ( typeArray.size == parNames.size, {
			snippets = snippets ++ this.addPageCode(pageName);

			parNames do: { |name, i|
			var type = typeArray[i];

			case
			{ type == \Fader }
			{       idNames = idNames ++ ["p" ++ id ++ "Fader" ++ i];
				snippets = snippets ++ this.addFaderCode(idNames.last,i * 100 + 6,15,100, 678);
			}
			{ type == \Range }
			{       idNames = idNames ++ ["p" ++ id ++ "Range" ++ i];
				snippets = snippets ++ this.addRangeCode(idNames.last,i * 100 + 6,15,100, 678);
			};

			snippets = snippets ++ this.addTextCode("p" ++ id ++ "Text" ++ i,name,i * 100 + 6,129,100,48);
		};
		buildInfo[id, \pageName] = pageName;
		buildInfo[id, \idNames] = idNames;

		this.sendPacket("<JZML>" ++ snippets ++ "<JZML>");

			r { 0.1.wait; connections[current_ip][1].sendMsg("/interface", pageName); bindGui.randomize;}.play;
		}, { "input typeArray and oscTagArray don't have the same sizes".postln; });
	}

	buildDemo { | typeArray, parNames, pageName = "default page", id = 1|
		var type,snippets,idNames;

		if ( typeArray.size == parNames.size, {
			snippets = snippets ++ this.addPageCode(pageName);

			parNames do: { |name, i|
				var type = typeArray[i];

				case
				{ type == \Fader }
				{       idNames = idNames ++ ["p" ++ id ++ "Fader" ++ i];
					snippets = snippets ++ this.addFaderCode(idNames.last,i * 100 + 6,15,100, 678);
				}
				{ type == \Range }
				{       idNames = idNames ++ ["p" ++ id ++ "Range" ++ i];
					snippets = snippets ++ this.addRangeCode(idNames.last,i * 100 + 6,15,100, 678);
				};

				snippets = snippets ++ this.addTextCode("p" ++ id ++ "Text" ++ i,name,i * 100 + 6,129,100,48);
			};
			buildInfo[id, \pageName] = pageName;
			buildInfo[id, \idNames] = idNames;

			this.sendPacket("<JZML>" ++ snippets ++ "<JZML>");

			r { 0.1.wait; connections[current_ip][1].sendMsg("/interface", pageName); nil }.play;
		}, { "input typeArray and oscTagArray don't have the same sizes".postln; });
	}
}