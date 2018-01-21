:: To update the translations, build and download the zip file from Crowdin.
:: Unzip translations into the below location (src_root).
:: Set the correct paths for src_root and dest_root.
:: Run this script.
@echo off

set src_root=d:\temp
set dest_root=d:\src\android-money-manager-ex\app\src\main\res

:: Arabic
set src_lang=ar
set src_locale=%src_lang%-rSA
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Bosnian
set src_lang=bs
set src_locale=%src_lang%-rBA
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Chinese, modern
set src_lang=zh-CN
set src_locale=zh-rCN
set dest_lang=%src_locale%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Chinese, traditional
set src_lang=zh-TW
set src_locale=zh-rTW
set dest_lang=%src_locale%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Czech
set src_lang=cs
set src_locale=%src_lang%-rCZ
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Danish
set src_lang=da
set src_locale=%src_lang%-rDK
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Dutch
set src_lang=nl
set src_locale=%src_lang%-rNL
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Finnish
REM set src_lang=fi
REM set src_locale=%src_lang%-rFI
REM set dest_lang=%src_lang%
REM call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: French
set src_lang=fr
set src_locale=%src_lang%-rFR
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Filipino
set src_lang=fil
set src_locale=%src_lang%-rPH
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: German
set src_lang=de
set src_locale=%src_lang%-rDE
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Greek
set src_lang=el
set src_locale=%src_lang%-rGR
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Hebrew, he iw-IL -> he
set src_lang=he
set src_locale=iw-rIL
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Hungarian
set src_lang=hu
set src_locale=%src_lang%-rHU
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Indonesian, id in-ID -> in
set src_lang=id
set src_locale=in-rID
set dest_lang=in
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Italian
set src_lang=it
set src_locale=%src_lang%-rIT
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Japanese
set src_lang=ja
set src_locale=%src_lang%-rJP
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Polish
set src_lang=pl
set src_locale=%src_lang%-rPL
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Portugese, Brasilian
set src_lang=pt-BR
set src_locale=pt-rBR
set dest_lang=%src_locale%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Portugese
set src_lang=pt-PT
set src_locale=pt-rPT
set dest_lang=pt
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Romanian
set src_lang=ro
set src_locale=%src_lang%-rRO
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Russian
set src_lang=ru
set src_locale=%src_lang%-rRU
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Slovak
set src_lang=sk
set src_locale=%src_lang%-rSK
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Slovenian
set src_lang=sl
set src_locale=%src_lang%-rSI
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Spanish
set src_lang=es-ES
set src_locale=es-rES
set dest_lang=es
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Turkish
set src_lang=tr
set src_locale=%src_lang%-rTR
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Ukrainian
set src_lang=uk
set src_locale=%src_lang%-rUA
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

:: Vietnamese
set src_lang=vi
set src_locale=%src_lang%-rVN
set dest_lang=%src_lang%
call copy_translation.cmd %src_root% %src_lang% %src_locale% %dest_root% %dest_lang% 

pause