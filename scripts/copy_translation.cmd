:: Copy translations for a single locale.
:: Called from update_translations.bat.

@echo off 

set src=%~1
set dest_root=%~2

:: set VAR="%~1"
set lang=%~3
set locale=%~4
set locale_spec=%~5

:: source directory
if exist %src%\%lang% (
	set src_locale=%lang%
	echo %lang%
) 
if exist %src%\%lang%-%locale% (
	set src_locale=%lang%-%locale%
	echo %lang%-%locale%
)

:: source, values directory. 
:: Hebrew is specific (he, iw-IL). This is to accommodate cases with different codes.
if [%locale_spec%] == [] (
	set locale_spec=%lang%
)

:: Destination directory.
if exist %dest_root%\values-%lang% (
	set dest=%dest_root%\values-%lang%
	@echo destination: %dest%
)
if exist %dest_root%\values-%lang%-r%locale% (
	set dest=%dest_root%\values-%lang%-r%locale%
	@echo destination: %dest%
)


echo %src%\%src_locale%\res\values-%locale_spec%-r%locale%\*.* --- %dest%

copy %src%\%src_locale%\res\values-%locale_spec%-r%locale%\*.* %dest%
