@echo off
setlocal enabledelayedexpansion

:: Déclaration des variables
set "work_dir=C:\Users\nec\Documents\GitHub\sprint"
set "src=%work_dir%\framework"
set "lib=%work_dir%\lib"
set "destination=D:\Webdynamique\framework\lib"

:: Créer une liste de tous les fichiers .java dans le répertoire src et ses sous-répertoires
dir /s /B "%src%\*.java" > sources.txt
:: Exécuter la commande javac
javac -d "%src%" -cp "C:\Users\nec\Documents\GitHub\sprint\lib\*" @sources.txt
:: Supprimer les fichiers sources.txt et libs.txt après la compilation
del sources.txt
cd "%src%"
jar cvf "%destination%\Front.jar" *
echo Déploiement terminé.
