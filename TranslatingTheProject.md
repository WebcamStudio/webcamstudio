# Introduction #

Here are described the steps required to participate in the translation of WebcamStudio

# Details #
[Java](http://en.wikipedia.org/wiki/Java_%28programming_language%29) has the internal ability to support multi-language applications. WebcamStudio implements this ability as it is using the [Languages.properties file](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/Languages.properties) as the base for the English labels.

The content of the file is quite simple:
```ini

The Tag Name = The Tag Descripton
```

One line for each tag. The text must be in [Unicode](http://en.wikipedia.org/wiki/Unicode), so it is best to use the [NetBeans environment](https://netbeans.org/) to edit those files especially if the language contains accents like French.

For each specific language, a new file is created in the format: **Languages\_fr.properties**, _**fr**_ being for French.

To add a new language, one must import the base file [Languages.properties](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/Languages.properties), and copy it to the desired language (using the 2 letters of the language). And then, you just translate each label as needed.

On the next build, the new file will be integrated and upon loading, Java looking at the current locale, will load the proper language file. If no proper language file is found, then the default file [Languages.properties](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/Languages.properties) (in this particular case the default is English) will be loaded.

# How to translate WebcamStudio step by step guide #
You have two options translating the application:
  1. Using [NetBeans IDE](https://netbeans.org/)
    * Download and install the latest version of the [NetBeans IDE](https://netbeans.org/)
    * [Checkout the source code of WebcamStudio](https://code.google.com/p/webcamstudio/source/checkout) in a folder
    * Open the project in NetBeans
    * In the sources of the project, go to **webcamstudio**
    * All Languages.properties files are in there
    * Add or Update the proper language file
    * Rebuild and test
    * Once everything is working, commit the modification (_if you are a project developer and have commit permissions_). If you are not a project developer with commit permissions, you may send to anyone from the WebcamStudioTeam (using the email addresses listed in [People](https://code.google.com/p/webcamstudio/people/list)) your changes.
    * **Important for developers**, set the commit message as a comment saying what was changed and why
  1. Using an ordinary code editor like Kate, GEdit etc
    * Download the raw [Languages.properties](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/Languages.properties) file.
    * Rename it to the desired language (using the 2 letters of the language). For example for French the file must be named: **Languages\_fr.properties**
    * Translate each label in the file as needed.
    * When ready please send your file to anyone from the WebcamStudioTeam (using the email addresses listed in [People](https://code.google.com/p/webcamstudio/people/list)).