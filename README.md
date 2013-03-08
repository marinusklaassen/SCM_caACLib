
caAC-MLib 0.42
===========
**Computer Aided Algorithmic Composition Library 0.42**

 
This library I am currently developing contains various SuperCollider classes and UGens extensions to perform algorithmic compostion and digital sound synthesis within concepts and approaches towards flexible models and user interfaces.   
An important aspect is the development of a flexible UI frontend that I am programming with the SuperCollider language, to work with patterns and tactile controllers in a model-view-controller paradigm, to have a bi-directional control between SuperCollider and the Lemur applications to perform realtime CAAC and digital sound synthesis.    


**Content:**
 
- Various GUI elements  
- Various (demand) UGens  
- Various Patterns   
- MLemurGui  
- Score & Controller interface  
- MPreset  

**Example code:**  
Adding a bunch of faders to the Lemur app. 
 
`~lm = MLemurGui.new;`  

`~lm.connect("192.10.1.2",8002,8000);`    
`~lm.set_osctarget(0,"192.10.1.16", NetAddr.langPort);`    
`~lm.resetAll;`    
  
`30 do: {|i|~lm.fader("klang",\sine++i,i%15*65+25,asInt(i/15%2,1)*350,65,350,Color.rand)};`  
`30 do: {|i|~lm.oscaddr.sendMsg('/sine'++i++'/x',1.0.linrand)};` 
 
`OSCFunc.trace(true);`   

`30 do: {|i|~lm.removeFader("klang",\sine++i)};`  

`~lm.disconnect;`  


![](http://https://github.com/marinusklaassen/caAC-MLib/blob/Update/Lemur/HelpSource/Classes/lpict.png)


Status
======
This library is currently under active development. 



Contact
=======

**Marinus Klaassen**  
marinus_klaassen@hotmail.com  
[www.soundcloud.com/marinusklaassen](www.soundcloud.com/marinusklaassen)



