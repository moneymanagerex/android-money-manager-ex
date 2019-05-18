"""
Script for updating the translated string files in the source code from a compressed archive
from Crowdin
"""
import shutil
import os
from pathlib import Path

home = str(Path.home())

# Settings
src_root = f"{home}/Downloads/android-money-manager-ex"
dst_root = f"{home}/src/android-money-manager-ex/app/src/main/res"
#

def directory_exists(path: str):
    """ Checks if the given path exists and is a directory """
    if os.path.exists(path) and os.path.isdir(path):
        return path
    else:
        return None

def source_directory_exists(name: str):
    """ Test if the directory exists """
    source_dir = f"{src_root}/{name}"
    return directory_exists(source_dir)

def get_source_dir(locale: str, country: str):
    """ Discovers the source directory for language """
    # Try the more-specific first. i.e. zh-CN
    name = f"{locale}-{country}"
    source_dir = source_directory_exists(name)
    if source_dir:
        return source_dir

    # Otherwise, try the generic one. i.e. ar
    name = f"{locale}"
    source_dir = source_directory_exists(name)
    if source_dir:
        return source_dir

    return source_dir

def destination_dir_exists(name: str):
    """ Checks if the destination directory exists """
    path = f"{dst_root}/{name}"
    return directory_exists(path)

def get_destination_dir(locale: str, country: str):
    """ Determines the destination directory for the translations """
    # Check the more-specific path first.
    name = f"values-{locale}-r{country}"
    dst_dir = destination_dir_exists(name)
    if dst_dir:
        return dst_dir
    
    # try the generic
    name = f"values-{locale}"
    dst_dir = destination_dir_exists(name)
    if dst_dir:
        return dst_dir

def copy_translation(language):
    """ Copy one language/locale to the destination """
    # copy %source%\res\values-%src_locale%\*.* %dest_root%\values-%dest_lang%

    locale: str = language[0]
    country: str = language[1]
    crowdin_locale: str = locale
    if (len(language) > 2):
        crowdin_locale = language[2]
    dest_lang: str = locale
    if (len(language) > 3):
        dest_lang = language[3]

    print(f"=> processing {locale}")

    # Sources

    source_dir = get_source_dir(locale, country)
    # Take only the stuff from 'res'
    source_dir += f"/res/values-{crowdin_locale}-r{country}"
    print(f"Source: {source_dir}")
    # List all the source files
    files = os.listdir(source_dir)
    #print(files)

    # Destination.

    #destination_dir = get_destination_dir(locale, country)
    destination_dir = get_destination_dir(dest_lang, country)
    print(f"destination: {destination_dir}")

    for filename in files:
        full_src_path = f"{source_dir}/{filename}"
        full_dst_path = f"{destination_dir}/{filename}"
        
        print(f"copying: {filename}")

        shutil.copyfile(full_src_path, full_dst_path)
        
def update_translations():
    """ copy translated files into the correct locations """
    print("############################ Copying Translations ##############################")
    languages = [
        ("ar", "SA"),
        ("bs", "BA"),
        ("zh", "CN"),
        ("zh", "TW"),
        ("cs", "CZ"),
        ("da", "DK"), # Denmark
        ("nl", "NL"),
        ("fr", "FR"),
        ("fil", "PH"), # Philipines
        ("de", "DE"),
        ("el", "GR"), # Greece
        ("he", "IL", "iw", "iw"), # Hebrew, Israel
        ("hu", "HU"),
        ("id", "ID", "in", "in"), # Indonesia
        ("it", "IT"),
        ("ja", "JP"),
        ("pl", "PL"),
        ("pt", "BR"), # Portugese, Brasil
        ("pt", "PT"), # Portugese, Portugal
        ("ro", "RO"),
        ("ru", "RU"),
        ("sk", "SK"), # Slovakia
        ("sl", "SI"), # Slovenia
        ("es", "ES"), # Spain
        ("tr", "TR"), # Turkey
        ("uk", "UA"), # Ukraine
        ("ur", "IN"), # Urdu, India
        ("ur", "PK"), # Urdu, Pakistan
        ("vi", "VN") # Vietnam
    ]
    for language in languages:
        copy_translation(language)


if __name__ == "__main__":
    update_translations()

