# AlphacraftLauncher
This launcher aims to repurpose the original Minecraft Launcher from June 29, 2010. 

The launcher will, by default, download the 'Beta 1.1_02' version of Minecraft, as it was the last version of Minecraft to be released in 2010. 

If you'd like to change this, download the source files, navigate to the GameUpdater class, then to the "loadFiles()" method and change the first URL value (*piston-data.mojang.com*) to a different version of the same URL. Otherwise, you need to change the URL and the "version" String to match the URL.

**You need Java 8u261 or greater to run this application.**

## Issues
- - Game doesn't recognise authenticated accounts and will default to "Player".
- - Skin does not update for cracked accounts and will default to "Player.png".
- - HDPatcher does not apply when launching the game.
> If you find any issues, please let me know about it! Thanks.
