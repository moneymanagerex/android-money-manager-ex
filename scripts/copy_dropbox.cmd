:: copy translation of the dropbox manual.
:: Called from update_translations.

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

:: source, values directory. Hebrew is specific (he, iw-IL). This is mostly to accommodate that case.
if [%locale_spec%] == [] (
	set locale_spec=%lang%
)

:: Destination directory.
if exist %dest_root%\raw-%lang% (
	set dest=%dest_root%\raw-%lang%
)
if exist %dest_root%\raw-%lang%-r%locale% (
	set dest=%dest_root%\raw-%lang%-r%locale%
)


::echo %src%\%src_locale%\res\values-%locale_spec%-r%locale%\*.* %dest%

copy %src%\%src_locale%\help\help_dropbox.html %dest%