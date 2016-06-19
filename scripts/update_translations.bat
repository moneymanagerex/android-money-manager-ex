:: copy translations from the below location.
:: Set the correct location to the root of the unzipped translations file from Crowdin.
@echo off

set src_root=d:\temp
set dest_root=d:\src\android-money-manager-ex\app\src\main\res

:: Bosnian
set lang=bs
set country=BA
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Chinese, modern
set lang=zh
set country=CN
call copy_translation.cmd %src_root% %dest_root% %lang% %country%

:: Chinese, traditional
set lang=zh
set country=TW
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Czech
set lang=cs
set country=CZ
call copy_translation.cmd %src_root% %dest_root% %lang% %country%

:: Dutch
set lang=nl
set country=NL
call copy_translation.cmd %src_root% %dest_root% %lang% %country%

:: Finnish
set lang=fi
set country=FI
call copy_translation.cmd %src_root% %dest_root% %lang% %country%

:: French
set lang=fr
set country=FR
call copy_translation.cmd %src_root% %dest_root% %lang% %country%

:: German
set lang=de
set country=DE
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Greek
set lang=el
set country=GR
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Hebrew, he iw-IL -> he
set lang=he
set locale=iw
set country=IL
call copy_translation.cmd %src_root% %dest_root% %lang% %country% %locale%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country% %locale%

:: Hungarian
set lang=hu
set country=HU
call copy_translation.cmd %src_root% %dest_root% %lang% %country%

:: Indonesian, id in-ID -> in
:: special case
set lang=id
set locale=in
set country=ID
call copy_translation_to_locale.cmd %src_root% %dest_root% %lang% %country% %locale%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country% %locale%

:: Italian
set lang=it
set country=IT
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Japanese
set lang=ja
set country=JP
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Polish
set lang=pl
set country=PL
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
::call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Portugese, Brasilian
set lang=pt
set country=BR
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Portugese
set lang=pt
set country=PT
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Romanian
set lang=ro
set country=RO
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Russian
set lang=ru
set country=RU
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Slovak
set lang=sk
set country=SK
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Spanish
set lang=es
set country=ES
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Turkish
set lang=tr
set country=TR
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
::call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Ukrainian
set lang=uk
set country=UA
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

:: Vietnamese
set lang=vi
set country=VN
call copy_translation.cmd %src_root% %dest_root% %lang% %country%
call copy_dropbox.cmd %src_root% %dest_root% %lang% %country%

pause