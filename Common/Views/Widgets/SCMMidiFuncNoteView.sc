/*
* FILENAME: SliderSequencerView
*
* DESCRIPTION:
*         Based on the sclang EZSlider, however it's a child of View and the child views are organised by a layout, so it can scale.
*
*         SliderSequencerView(nil, 400@100, \db.asSpec, 0.5, "test").front().action_({ |v| v.postln; }).labelText_("Label test").value_(1.0.rand).value.postln;
*         SliderSequencerView()
*
* AUTHOR: Marinus Klaassen (2012, 2021Q3)
*
* EXAMPLES:
m = SCMMIDIFuncNoteView(bounds: 400@50).front();
m.actionNoteOn = { "on".postln; };
m.actionNoteOff = { "off".postln; };
m.update();
*/


SCMMIDIFuncNoteView : SCMViewBase {
    var numberBoxChan, numberBoxMsgNum, numberBoxSrcID, mainLayout;
    var chan, msgNum, srcID, hasMapping;
    var midiFuncNoteOff, <midifuncNoteOn, actionLearn;
    var <>actionNoteOn, <>actionNoteOff, learnMode, buttonLearn;

    *new { | parent, bounds |
        ^super.new(parent, bounds).initialize().initializeView();
    }

    initialize {
    }

    initializeView {
        mainLayout = HLayout();
        mainLayout.margins = 0!4;
        this.layout = mainLayout;
        numberBoxChan = NumberBox();
        numberBoxChan.background = Color.white.alpha_(0.5);
        numberBoxChan.toolTip = "MIDI chan - note message mapping";
        numberBoxChan.decimals = 0;
        numberBoxChan.action = {|sender|
            chan = sender.value;
            this.update();
        };
        mainLayout.add(numberBoxChan);
        numberBoxMsgNum = NumberBox();
        numberBoxMsgNum.background = Color.white.alpha_(0.5);
        numberBoxMsgNum.toolTip = "MIDI msg num - note message mapping";
        numberBoxMsgNum.decimals = 0;
        numberBoxMsgNum.action = {|sender|
            msgNum = sender.value;
            this.update();
        };
        mainLayout.add(numberBoxMsgNum);

        numberBoxSrcID = NumberBox();
        numberBoxSrcID.background = Color.white.alpha_(0.5);
        numberBoxSrcID.toolTip = "MIDI src ID - note message mapping";
        numberBoxSrcID.decimals = 0;
        numberBoxSrcID.action = {|sender|
            srcID = sender.value;
            this.update();
        };

        mainLayout.add(numberBoxSrcID);

        buttonLearn = Button();
		buttonLearn.states = [["learn", Color.black, Color.white.alpha_(0.25) ]];
        buttonLearn.toolTip = "Note message mapping";
        buttonLearn.action = { this.learn(); };
        mainLayout.add(buttonLearn);
    }

    learn {
        if (midifuncNoteOn.notNil, { midifuncNoteOn.disable(); });
        midifuncNoteOn = MIDIFunc.noteOn({
            chan = midifuncNoteOn.chan;
            msgNum = midifuncNoteOn.msgNum;
            srcID = midifuncNoteOn.srcID;
            if (chan.notNil, { this.update(); });
        }).learn();
    }

    update {
        if (midifuncNoteOn.notNil, {
            midifuncNoteOn.free;


        });
        if (     midiFuncNoteOff.notNil, {
            midiFuncNoteOff.free;
        });
        defer({
            numberBoxMsgNum.value = msgNum;
            numberBoxChan.value = chan;
            numberBoxSrcID.value = srcID;
        });
        midifuncNoteOn = MIDIFunc.noteOn(actionNoteOn, msgNum, chan, srcID).permanent_(true);
        midiFuncNoteOff = MIDIFunc.noteOff(actionNoteOff, msgNum, chan, srcID).permanent_(true);
        hasMapping = true;
    }

    getState {
        var state = Dictionary();
        state[\msgNum] = msgNum;
        state[\chan] = chan;
        state[\srcID] = srcID;
        state[\hasMapping] = hasMapping;
        ^state;
    }

    loadState { |state|
        if (state[\hasMapping] == true, {
            msgNum = state[\msgNum];
            chan = state[\chan];
            srcID = state[\srcID];
            this.update();
        });
    }

    dispose {
        midiFuncNoteOff.free;
        midifuncNoteOn.free;
    }
}