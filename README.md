# png2icns
Converts a 1024x1024 .png file to a Mac OS X iconset. The tool is a command line application and needs Java 7 or Java 8.

#How to
Create a .png file containing your icon with a program like gimp (http://www.gimp.org). For best quality create an image with 1024px width and 1024 px height.

From a terminal window run:

``   java -jar png2icns.jar -i icon.png``

this will create the folder `/tmp/noname.iconset` and a file `noname.icns`
The folder conrains the scaled images with the correct naming. The .icns file is the iconset you can use in your application.

If you want to set the output folder an filename use the `-o` option.

``   java -jar png2icns.jar -i icon.png -o /Users/me/myapp``

This will create the folder `/Users/me/myapp.iconset` and the file `/Users/me/myapp.icns`.

The program needs the `iconutil` executable that comes with Xcode. You need to have XCode installed to use this program. Xcode is available from the AppStore.
