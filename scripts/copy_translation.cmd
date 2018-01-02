:: Copy translations for a single language.
:: Called from update_translations.bat.

::@echo off 

:: set VAR="%~1"

::echo Processing %~1 %~2 %~3 %~4 %~5

set src_root=%~1
set src_lang=%~2
set src_locale=%~3

set dest_root=%~4
set dest_lang=%~5

:: Source directory
set source=%src_root%\%src_lang%

::echo %source% ---} %dest_root% - %dest_lang%
echo %src_lang% 

:: Categories and Strings 
copy %source%\res\values-%src_locale%\*.* %dest_root%\values-%dest_lang%

:: Dropbox
copy %source%\help\*.* %dest_root%\raw-%dest_lang%