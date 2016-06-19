:: Copy translations for a single language.
:: The destination is the %locale parameter!
:: Called from update_translations.bat.

@echo off 

set src=%~1
set dest_root=%~2

:: set VAR="%~1"
set lang=%~3
set country=%~4
set locale=%~5

:: source directory
if exist %src%\%lang% (
	set src_lang=%lang%
	echo %lang%
) 
if exist %src%\%lang%-%country% (
	set src_lang=%lang%-%country%
	echo %lang%-%country%
)

:: source, values directory. 
:: Hebrew is specific (he, iw-IL). This is to accommodate cases with different codes.
if [%locale%] == [] (
	set locale=%lang%
)

:: Destination directory.
if exist %dest_root%\values-%locale% (
	set dest=%dest_root%\values-%locale%
	@echo destination: %dest%
)
if exist %dest_root%\values-%locale%-r%country% (
	set dest=%dest_root%\values-%locale%-r%country%
	@echo destination: %dest%
)


echo %src%\%src_lang%\res\values-%locale%-r%country%\*.* --- %dest%

copy %src%\%src_lang%\res\values-%locale%-r%country%\*.* %dest%
