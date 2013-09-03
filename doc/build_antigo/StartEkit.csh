#! /bin/csh
## RunEkit.csh - a C Shell script by Ivan Trendafilov for Ekit.
## Saves interface and language user preferences to ekit.conf
## and remembers them for future use. Enables permanent 
## customization of the program interface.
## Supported:
##  - 21 Languages
##  - Toolbars
##  - Source code view
##  - Icons
##  - Edit mode
##  - Spellcheck
##  - Debugger

# Set $ekitjar variable to the Ekit jar file.
set ekitjar = ekit.jar

# Checks if ekit.conf exists, and if so, loads the settings from it.
if (-e ekit.conf) then
	set paramcode = `cat ekit.conf | head -1`
	java -jar $ekitjar com.hexidec.ekit.Ekit $paramcode
	goto done
endif

echo "Welcome to Ekit! This appears to be the first time you've run Ekit!"
echo "You need to set up your default preferences for the program."
echo "This script will lead you through the necessary steps."
echo "Do you wish to do continue? (y/n):"
set answer = $<                      # C shell input from stdin
goback:
if ($answer == "Y" || $answer == "y") then
	goto continue
endif
if ($answer == "N" || $answer == "n") then
	java -jar $ekitjar com.hexidec.ekit.Ekit
	exit 0
endif

if ($answer != "Y" && $answer != "y" && $answer != "n" && $answer != "N") then
	echo "Unacceptable input"
	goto goback
endif

continue:
echo "What language would you like to use Ekit in?"
echo "1) English (U.S.) 	en_US (default)"
echo "2) Bulgarian 		bg_BG"
echo "3) German 		de_DE"
echo "4) English (U.K.) 	en_UK"
echo "5) Spanish (Spain) 	es_ES"
echo "6) Spanish (Mexico) 	es_MX"
echo "7) Finnish 		fi_FI"
echo "8) French 		fr_FR"
echo "9) Hungarian 		hu_HU"
echo "10) Italian (Switz.)	it_CH"
echo "11) Japanese		ja_JP"
echo "12) Dutch		nl_NL"
echo "13) Norwegian		no_NO"
echo "14) Polish		pl_PL"
echo "15) Portuguese (Brazil)	pt_BR"
echo "16) Portuguese (Port.)	pt_PT"
echo "17) Russian		ru_RU"
echo "18) Swedish		se_SE"
echo "19) Slovenian 		sl_SL"
echo "20) Turkish		tr_TR"
echo "21) Chinese (Simpl.)	zh_CN"
echo "Enter langcode (e.g. bg_BG):"
set paramcode = $<
set paramcode = -l$paramcode

echo "How would you like to run Ekit:"
echo "1) Show single toolbar"
echo "2) Multiple toolbars (default)"
echo "3) Hide all toolbars"
echo "Enter your choice: (1..3)"
set toolbar = $<
if($toolbar != "1" && $toolbar != "2" && $toolbar != "3") then
	echo "Invalid choice $toolbar. Setting the default."
	set toolbar = "2"
endif

if($toolbar == "1") then
	set paramcode = "-t $paramcode"
endif

if($toolbar == "2") then
	set paramcode = "-t+ $paramcode"
endif
if($toolbar == "3") then
	set paramcode = "-T $paramcode"
endif	

echo "Would you like to:"
echo "1) Show the source window"
echo "2) Hide the source window (default)" 
echo "Enter your choice: (1..2)"
set srcwindow = $<
if($srcwindow != "1" && $srcwindow != "2") then
	echo "Invalid choice $srcwindow. Setting the default."
	set srcwindow = "2"
endif

if($srcwindow == "1") then
	set paramcode = "-s $paramcode"
endif
if($srcwindow == "2") then
	set paramcode = "-S $paramcode"
endif

echo "Would you like to have icons in your menus:"
echo "1) Show icons (default)"
echo "2) Hide icons"
echo "Enter your choice: (1..2)"
set icons = $<
if($icons != "1" && $icons != "2") then
	echo "Invalid choice $icons. Setting the default."
	set icons = "1"
endif
if($icons == "1") then
	set paramcode = "-m $paramcode"
endif
if($icons == "2") then
	set paramcode = "-M $paramcode"
endif

echo "Please set exclusive edit mode:"
echo "1) On (default)"
echo "2) Off"
echo "Enter your choice: (1..2)"
set editmode = $<
if($editmode != "1" && $editmode != "2") then
	echo "Invalid choice $editmode. Setting the default."
	set editmode = "1"
endif

if($editmode == "1") then
	set paramcode = "-x $paramcode"
endif
if($editmode == "2") then
	set paramcode = "-X $paramcode"
endif
echo "Would you like to include the spellchecker?"
echo "1) Include"
echo "2) Omit (default)"
echo "Enter your choice: (1..2)"
set spellcheck = $<
if($spellcheck != "1" && $spellcheck != "2") then
	echo "Invalid choice $spellcheck. Setting the default."
	set spellcheck = "2"
endif
if($spellcheck == "1") then
	set paramcode = "-v $paramcode"
endif
if($spellcheck == "2") then
	set paramcode = "-V $paramcode"
endif

echo "Show the debug menu?"
echo "1) Yes"
echo "2) No (default)"
echo "Enter your choice: (1..2)"
set dbg = $<
if($dbg != "1" && $dbg != "2") then
	echo "Invalid choice $dbg. Setting the default."
	set dbg = "2"
endif

if($dbg == "1") then
	set paramcode = "-d $paramcode"
endif
if($dbg == "2") then
	set paramcode = "-D $paramcode"
endif
echo "Should you wish to reset your preferences, you need to delete the ekit.conf file" 
echo "Generated: $paramcode"
echo "Paramcode saved to ekit.conf"
echo $paramcode > ekit.conf
echo "Executing Ekit"
java -jar $ekitjar com.hexidec.ekit.Ekit $paramcode

done:
	exit 0
