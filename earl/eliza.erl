### JFRED Eliza Clone
### converted from Charlie Hayden's freeware Eliza 
### script by Robby Garner

action: TOPIC
	Please tell me your problem.
	Please tell me what's been bothering you. 
	Is something troubling you?
	I'm not sure I understand you fully.
	Please go on.
	That is interesting.  Please continue.
	Tell me more about that.
	Could you be more specific?
	Does talking about this bother you ?

regex:	HELLO
	invokes: TOPIC
	hello
	^hi$
	^hi there
	greetings
	
action: FINAL
	Goodbye.  It was nice talking to you.
	Goodbye.  I hope you found this session helpful.
	I think you should talk to a real analyst.  Ciao! 
	Life is tough.  Hang in there!

regex: QUIT
	invokes: FINAL
	bye
	goodbye
	done
	exit
	quit

### NB Some notes on synonymns taken from the original script
#synon: belief feel think believe wish
#synon: family mother mom father dad sister brother wife children child
#synon: desire want need
#synon: sad unhappy depressed sick
#synon: happy elated glad better
#synon: cannot can't
#synon: everyone everybody nobody noone
#synon: be am is are was
	
regex:	SORRY
	invokes: SAYSORRY
	sorry

action:	SAYSORRY
	Please don't apologise.
	Apologies are not necessary.
	I've told you that apologies are not required.
	It did not bother me.  Please continue.

regex: 	APOLOGIZE
	invokes: SAYSORRY
	apologize
	
#regex: remember 5
  #decomp: * i remember *
 
regex:	REMEMBER
 	invokes: SAYREMEMBER
 	i remember
   
action: SAYREMEMBER
	priority: 7
	bind: remember
	Do you often think of [] ?
	Does thinking of [] bring anything else to mind ?
	What else do you recollect ?
	Why do you remember [] just now ?
	What in the present situation reminds you of [] ?
	What is the connection between me and [] ?
	What else does [] remind you of ?
  
#  decomp: * do you remember *

regex:	DOYOUREMEMBER
	invokes: SAYREMEMBER
	do you remember
	
action: SAYDOYOUREM
	priority: 7
	bind: remember
	Did you think I would forget [] ?
	Why do you think I should recall [] now ?
	What about [] ?
	You mentioned [] ?
  
  
#  decomp: * you remember *

regex:	YOUREM
	invokes: DOYOUREM
	you remember
	
action: DOYOUREM
	priority: 7
	bind: remember
	How could I forget [] ?
	What about [] should I remember ?

#fuzzy:	REMEMBER
#	20	YOU

regex: FORGET5
	invokes: FORGET5DO
	i forget
	
action:	FORGET5DO
	priority: 7
	bind: forget
	Can you think of why you might forget [] ?
	Why can't you remember [] ?
	How often do you think of [] ?
	Does it bother you to forget that ?
	Could it be a mental block ?
	Are you generally forgetful ?
	Do you think you are suppressing [] ?

regex: DIDYOUFORGET
	invokes: DIDYOUFORG
	did you forget
	
action: DIDYOUFORG
	priority: 7
	bind: forget
	Why do you ask ?
	Are you sure you told me ?
	Would it bother you if I forgot [] ?
	Why should I recall [] just now ?
	Tell me more about [].

#fuzzy:	FORGET
#	20	WHAT


regex:	IF3
	invokes: IF
	/bif/b
	
action: IF
	priority: 7
	bind: if
	Do you think its likely that [] ?
	Do you wish that [] ?
	What do you know about [] ?
	Really, if [] ?
	What would you do if [] ?
	But what are the chances that [] ?
	What does this speculation lead to ?

regex: DREAMED4
	invokes DREAMED
	i dreamed
	
action:	DREAMED
	bind: dreamed
	Really, [] ?
	Have you ever fantasized [] while you were awake ?
	Have you ever dreamed [] before ?
    
    
###    reasmb: goto dream
regex: DREAM3
	invokes: DREAM
	/bdream/b
	What does that dream suggest to you ?
	Do you dream often ?
	What persons appear in your dreams ?
	Do you believe that dreams have something to do with your problem ?

regex: PERHAPS0
	invokes: PERHAPS
	perhaps
	
action:	PERHAPS
	priority: 7
	You don't seem quite certain.
	Why the uncertain tone ?
	Can't you be more positive ?
	You aren't sure ?
	Don't you know ?
	How likely, would you estimate ?

regex: NAME15
	invokes: NAME
	name

action:	NAME	
	priority: 7
	I am not interested in names.
	I've told you before, I don't care about names -- please continue.

regex: DEUTCH0
	invokes: DEUTCH
	deutch
	german
	
action:	DEUTCH
	priority: 7
	I speak only English.
	I told you before, I don't understand German.

regex: FRANCAIS0
	invokes: FRANCAIS
	francais
	french

action:	FRANCAIS
	priority: 7
	I speak only English.
	I told you before, I don't understand French.

regex: ITALIANO0
	invokes: ITALIAN
	italiano
	italian

action:	ITALIAN
	priority: 7
	I speak only English.
	I told you before, I don't understand Italian.

regex: ESPANOL0
	invokes: ESPANOL
	espanol
	spanish

action:	ESPANOL
	priority: 7
	I speak only English.
	I told you before, I don't understand Spanish.


regex: HELLO0
	invokes: HELLO
	hello
	/bhi/b
	
action:	HELLO
	How do you do.  Please state your problem.
	Hi.  What seems to be your problem ?

regex: COMPUTER50
	invokes: COMPUTER
	computer
	
action: COMPUTER
	priority: 7
	Do computers worry you ?
	Why do you mention computers ?
	What do you think machines have to do with your problem ?
	Don't you think computers can help people ?
	What about machines worries you ?
	What do you think about machines ?

regex: AM0
	invokes: AM
	/bam i/b
	
action: AM
	priority: 7
	bind: am
	Do you believe you are [] ?
    Would you want to be [] ?
    Do you wish I would tell you you are [] ?
    What would it mean if you were [] ?


#  decomp: * i am *
#    reasmb: goto i

regex:	IAM0
	invokes: IAM
	^i am$
	
action:	IAM
	Why do you say 'am' ?
	I don't understand that.
    
regex: ARE0
	invokes: AREYOU WHAT
	are you
	
action:	AREYOU
	priority: 7
	bind: you
	Why are you interested in whether I am [] or not ?
	Would you prefer if I weren't [] ?
	Perhaps I am [] in your fantasies.
	Do you sometimes think I am [] ?
	Would it matter to you ?
	What if I were [] ?
  
  
  
#  decomp: * you are *
#    reasmb: goto you

regex:	YOUARE0
	invokes: YOU
	.*you are.*

#  decomp: * are *

regex:	ARE1
	invokes: ARE
	/bare/b

action:	ARE
	priority: 7
	bind: are
	Did you think they might not be [] ?
	Would you like it if they were not [] ?
	What if they were not [] ?
	Are they always [] ?
	Possibly they are [].
	Are you positive they are [] ?
    
regex: YOUR0
	invokes: YOUR
	your
	
action:	YOUR
	priority: 7
	bind: your
	Why are you concerned over my [] ?
	What about your own [] ?
	Are you worried about someone else's [] ?
	Really, my [] ?
	What makes you think of my [] ?
	Do you want my [] ?
    
regex: WAS2
	invokes: WAS
	/bwas/b
	
action:	WAS
	priority: 7
	bind: was
	What if you were [] ?
	Do you think you were [] ?
	Were you [] ?
	What would it mean if you were [] ?
	What does ' [] ' suggest to you ?
    
#    reasmb: goto what

regex:	IWAS0
	invokes: IWAS
	i was/b
	
action:	IWAS
	priority: 7
	bind: was
	Were you really ?
	Why do you tell me you were [] now ?
	Perhaps I already know you were [].

regex:	WASYOU0
	invokes: WASYOU
	/bwas you/b
	
action:	WASYOU
	priority: 7
	bind: you
	Would you like to believe I was [] ?
	What suggests that I was [] ?
	What do you think ?
	Perhaps I was [].
	What if I had been [] ?
    
regex: I0
	invokes: I
	desire
	want
	need
	
action:	I
	priority: 7
	bind: want
	What would it mean to you if you got [] ?
	Why do you want [] ?
	Suppose you got [] soon.
	What if you never got [] ?
	What would getting [] mean to you ?
	What does wanting [] have to do with this discussion ?

regex:	IAMSAD0
	invokes: IAMSAD
	i am.+(sad|unhappy|depressed|sick)
	
action: IAMSAD
	priority: 7
	bind: am
	I am sorry to hear that you are [].
	Do you think coming here will help you not to be [] ?
	I'm sure it's not pleasant to be [].
	Can you explain what made you [] ?

regex:	IAMHAPPY0
	invokes: IAMHAPPY
	i am.+(happy|elated|glad|better)
	
action:	IAMHAPPY	
	priority: 7
	bind: am
	How have I helped you to be [] ?
	Has your treatment made you [] ?
	What makes you [] just now ?
	Can you explain why you are suddenly [] ?

regex:	IWAS2
	invokes: WAS
	.+was.+
	
 # decomp: * i @belief i *
 regex: IBELIEF0
 	invokes: IBELIEF
 	.*i.+(belief|feel|think|believe|wish).+i.*
 
action:	IBELIEF
	priority: 7
	bind: i
	Do you really think so ?
	But you are not sure you [].
	Do you really doubt you [] ?

regex: BELIEFYOU0
	invokes: YOU
	.*i.+(belief|feel|think|believe|wish).+you.+

#  decomp: * i am *
regex:	IAM1
	invokes: IAMSTAR
	/bi am/b
	
action:	IAMSTAR
	priority: 7
	bind: am
	Is it because you are [] that you came to me ?
	How long have you been [] ?
	Do you believe it is normal to be [] ?
	Do you enjoy being [] ?
	Do you know anyone else who is [] ?

#  decomp: * i @cannot *
regex:	ICANNOT0
	invokes: ICANNOT
	.*i.+(cannot|cant).*
	
action:	ICANNOT
	priority: 7
	How do you know that you can't [] ?
	Have you tried ?
    Perhaps you could [] now.
    Do you really want to be able to [] ?
    What if you could [] ?

#  decomp: * i don't *
regex:	IDONT0
	invokes: IDONT
	.*i dont.*
	
action: IDONT
	Don't you really [] ?
	Why don't you [] ?
	Do you wish to be able to [] ?
	Does that trouble you ?

#  decomp: * i feel *
regex:	IFEEL0
	invokes: IFEEL
	i feel
	
action:	IFEEL
	priority: 7
	bind: feel
	Tell me more about such feelings.
	Do you often feel [] ?
	Do you enjoy feeling [] ?
	Of what does feeling [] remind you ?

#  decomp: * i * you *
regex:	IYOU2
	invokes: IYOU
	i.+you
	
action:	IYOU
	priority: 7
	bind: you
	Perhaps in your fantasies we [] each other.
	Do you wish to [] me ?
	You seem to need to [] me.
	Do you [] anyone else ?

#  decomp: *
#    reasmb: You say (1) ?
#    reasmb: Why do you say (1) ?
#    reasmb: Can you elaborate on that ?
#    reasmb: Do you say (1) for some special reason ?
#    reasmb: That's quite interesting.
    
regex: YOUREM0
	invokes: ALIKE
	you remind me of

regex:	YOUARE1
	invokes: YOUARE
	you are
	
action: YOUARE
	priority: 7
	bind: are
	What makes you think I am [] ?
	Does it please you to believe I am [] ?
	Do you sometimes wish you were [] ?
	Perhaps you would like to be [].

#  decomp: * you* me *
regex:	YOUME0
	invokes: YOUME
	you.+me
	
action:	YOUME
	priority: 7
	Why do you think I [] you ?
	You like to think I [] you -- don't you ?
	What makes you think I [] you ?
	Really, I [] you ?
	Do you wish to believe I [] you ?
	Suppose I did [] you -- what would that mean ?
	Does someone else believe I [] you ?

#  decomp: * you *
regex:	YOU3
	invokes: YOU
	you/b
	
action:	YOU
	priority: 7
	bind: you
	We were discussing you -- not me.
	Oh, I [] ?
	You're not really talking about me -- are you ?
	What are your feelings now ?

regex: YES0
	invokes: YES
	^yes$
	
action:	YES
	priority: 5
	Please go on.
	Please tell me some more about this.
	Why don't you tell me a little more about this.
	I see.
	I understand.
    
regex: NOONE0
	invokes: NOONE
	no one
	
action:	NOONE
	priority: 7
	bind: one
	Are you sure, no one [] ?
	Surely someone [] .
	Can you think of anyone at all ?
	Are you thinking of a very special person ?
	Who, may I ask ?
	You have a particular person in mind, don't you ?
	Who do you think you are talking about ?

regex: NO0
	invokes: NO
	^no$

action:	NO
	priority: 5
	Are you saying no just to be negative?
	Does this make you feel unhappy? 
	Why not ?
	Why 'no' ?
    
regex: MY2
	invokes: MY
	my family
	my mother
	my mom
	my father
	my dad
	my sister
	my brother
	my wife
	my children
	my child
	
action:	MY
	priority: 7
	bind: my
	Tell me more about your family.
	Who else besides your [] ?
	Your [] ?
	What else comes to mind when you think of your [] ?

regex:	MINE0
	invokes: MINE
	my $mine
	
action:	MINE
	priority: 7
	requires: mine
	bind: my
	Your [] ?
	Why do you say your [] ?
	Does that suggest anything else which belongs to you ?
	Is it important to you that your [] ?
#    reasm_for_memory: Let's discuss further why your [].
#    reasm_for_memory: Earlier you said your [].
#    reasm_for_memory: But your [].
#    reasm_for_memory: Does that have anything to do with the fact that your [] ?

regex: CAN0
	invokes: CAN
	/bcan/b

action:	CAN
	priority: 7
	bind: can
	You believe I can [] don't you ?
	You want me to be able to [].
	Perhaps you would like to be able to [] yourself.

regex:	CANI0
	invokes: CANI
	^can I/b
	
action:	CANI
	priority: 7
	bind: i
	Whether or not you can [] depends on you more than on me.
	Do you want to be able to [] ?
	Perhaps you don't want to [].
    
regex: WHAT0
	invokes: WHAT
	what
	who
	when
	how
	why
	
action:	WHAT
	priority: 7
	Why do you ask ?
	Does that question interest you ?
	What is it you really want to know ?
	Are such questions much on your mind ?
	What answer would please you most ?
	What do you think ?
	What comes to mind when you ask that ?
	Have you asked such questions before ?
	Have you asked anyone else ?
    

regex: BECAUSE0
	invokes: BECAUSE
	because
	
action:	BECAUSE
	prority: 7
	Is that the real reason ?
	Don't any other reasons come to mind ?
	Does that reason seem to explain anything else ?
	What other reasons might there be ?

regex: WHY2
	invokes: WHYDONTYOU
	why don't you
	
action:	WHYDONTYOU
	priority: 7
	bind: you
	Do you believe I don't [] ?
	Perhaps I will [] in good time.
	Should you [] yourself ?
	You want me to [] ?

regex:	WHY3
	invokes: WHYCANTI
	why cant i
	
action:	WHYCANTI
	priority: 7
	bind: i
	Do you think you should be able to [] ?
	Do you want to be able to [] ?
	Do you believe this will help you to [] ?
	Have you any idea why you can't [] ?

#  decomp: *
#    reasmb: goto what


regex: EVERYONE2
	invokes: EVERYONE
	everyone
	everybody
	nobody
	noone

action:	EVERYONE
	priority: 7
	Really, [] ?
	Surely not [].
	Can you think of anyone in particular ?
	Who, for example?
	Are you thinking of a very special person ?
	Who, may I ask ?
	Someone special perhaps ?
	You have a particular person in mind, don't you ?
	Who do you think you're talking about ?

#regex: everybody 2
#  decomp: *
#    reasmb: goto everyone
#regex: nobody 2
#  decomp: *
#    reasmb: goto everyone
#regex: noone 2
#  decomp: *
#    reasmb: goto everyone


regex: ALWAYS1
	invokes: ALWAYS
	always
	
action: ALWAYS
	priority: 7
	Can you think of a specific example ?
	When ?
	What incident are you thinking of ?
	Really, always ?

regex: ALIKE10
	invokes: ALIKE
	/balike.*

action: ALIKE
	priority: 9
	In what way ?
	What resemblence do you see ?
	What does that similarity suggest to you ?
	What do you suppose that resemblence means ?
	What is the connection, do you suppose ?
	Could there really be some connection ?
	How ?

regex: LIKE10
	invokes: ALIKE
	be
	am
	is
	are
	was
	like

regex: DIFFERENT0
	invokes: DIFFERENT
	different

action: DIFFERENT
	priority: 7
    How is it different ?
	What differences do you see ?
	What does that difference suggest to you ?
	What other distinctions do you see ?
	What do you suppose that disparity means ?
	Could there be some connection, do you suppose ?
	How ?

regex: SWEARING10
	invokes: SWEARING
	fuck
	fucker
	shit
	damn
	shut up
	
action: SWEARING
	priority: 7
	Does it make you feel strong to use that kind of language ?
	Are you venting your feelings now ?
	Are you angry ?
	Does this topic make you feel angry ? 
	Is something making you feel angry ? 
	Does using that kind of language make you feel better ? 
