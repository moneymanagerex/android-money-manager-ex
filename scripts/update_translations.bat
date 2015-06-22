:: copy translations from the below location.
:: Set the correct location to the root of the unzipped translations file from Crowdin.
@echo off

:: Bosnian
set lang=bs
set locale=BA
call copy_translation.bat %lang% %locale%
call copy_dropbox.bat %lang% %locale%

:: Chinese, modern
set lang=zh
set locale=CN
call copy_translation.bat %lang% %locale%

:: Chinese, traditional
set lang=zh
set locale=TW
call copy_translation.bat %lang% %locale%
call copy_dropbox.bat %lang% %locale%

:: Czech
set lang=cs
set locale=CZ
call copy_translation.bat %lang% %locale%

:: Dutch
set lang=nl
set locale=NL
call copy_translation.bat %lang% %locale%

:: French
set lang=fr
set locale=FR
call copy_translation.bat %lang% %locale%

:: German
set lang=de
set locale=DE
call copy_translation.bat %lang% %locale%
call copy_dropbox.bat %lang% %locale%

:: Hebrew, he iw-IL
set lang=he
set locale=IL
set locale_spec=iw
call copy_translation.bat %lang% %locale% %locale_spec%

:: Hungarian
set lang=hu
set locale=HU
call copy_translation.bat %lang% %locale%

:: Italian
set lang=it
set locale=IT
call copy_translation.bat %lang% %locale%

:: Japanese
set lang=ja
set locale=JP
call copy_translation.bat %lang% %locale%
call copy_dropbox.bat %lang% %locale%

:: Polish
set lang=pl
set locale=PL
call copy_translation.bat %lang% %locale%

:: Portugese, Brasilian
set lang=pt
set locale=BR
call copy_translation.bat %lang% %locale%
call copy_dropbox.bat %lang% %locale%

:: Portugese
set lang=pt
set locale=PT
call copy_translation.bat %lang% %locale%
call copy_dropbox.bat %lang% %locale%

:: Russian
set lang=ru
set locale=RU
call copy_translation.bat %lang% %locale%
call copy_dropbox.bat %lang% %locale%

:: Spanish
set lang=es
set locale=ES
call copy_translation.bat %lang% %locale%

:: Vietnamese
set lang=vi
set locale=VN
call copy_translation.bat %lang% %locale%
::call copy_dropbox.bat %lang% %locale%

pause