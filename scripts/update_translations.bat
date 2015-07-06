:: copy translations from the below location.
:: Set the correct location to the root of the unzipped translations file from Crowdin.
@echo off

set src_root=c:\temp
set dest_root=c:\dev\android-money-manager-ex\res
::set src_root=d:\temp
::set dest_root=d:\dev\GitHub\android-money-manager-ex\res

:: Bosnian
set lang=bs
set locale=BA
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Chinese, modern
set lang=zh
set locale=CN
call copy_translation.bat %src_root% %dest_root% %lang% %locale%

:: Chinese, traditional
set lang=zh
set locale=TW
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Czech
set lang=cs
set locale=CZ
call copy_translation.bat %src_root% %dest_root% %lang% %locale%

:: Dutch
set lang=nl
set locale=NL
call copy_translation.bat %src_root% %dest_root% %lang% %locale%

:: French
set lang=fr
set locale=FR
call copy_translation.bat %src_root% %dest_root% %lang% %locale%

:: German
set lang=de
set locale=DE
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Hebrew, he iw-IL
set lang=he
set locale=IL
set locale_spec=iw
call copy_translation.bat %src_root% %dest_root% %lang% %locale%

:: Hungarian
set lang=hu
set locale=HU
call copy_translation.bat %src_root% %dest_root% %lang% %locale%

:: Italian
set lang=it
set locale=IT
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Japanese
set lang=ja
set locale=JP
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Polish
set lang=pl
set locale=PL
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
::call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Portugese, Brasilian
set lang=pt
set locale=BR
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Portugese
set lang=pt
set locale=PT
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Russian
set lang=ru
set locale=RU
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Spanish
set lang=es
set locale=ES
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Turkish
set lang=tr
set locale=TR
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
::call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

:: Vietnamese
set lang=vi
set locale=VN
call copy_translation.bat %src_root% %dest_root% %lang% %locale%
call copy_dropbox.bat %src_root% %dest_root% %lang% %locale%

pause