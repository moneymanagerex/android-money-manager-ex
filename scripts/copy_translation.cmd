:: Copy translations for a single language.
:: Called from update_translations.bat.

::@echo off 

:: set VAR="%~1"

set src_root=%~1
set src_lang=%~2
set src_locale=%~3

set dest_root=%~4
set dest_lang=%~5

:: Source directory
set source=%src_root%\%src_lang%

:: Destination directory.
::set destination=%dest_root%\values-%dest_lang%

::echo %src%\%src_lang%\res\values-%locale%-r%country%\*.* --- %dest%
copy %source%\res\values-%src_locale%\*.* %dest_root%\values-%dest_lang%

:: Dropbox
copy %source%\%src_lang%\help\help_dropbox.html %dest_root%\raw-%dest_lang%