#!/bin/bash
VALUE=$( grep version ../src/webcamstudio/Version.java | sed 's/    final static String version = "//g' | sed 's/";//g' )
echo "Packaging version $VALUE"

if [ -e package ]; then
    echo "Removing previous package folder..."
    rm -R package
fi
#creating folders
mkdir package
mkdir package/usr
mkdir package/usr/bin
mkdir package/usr/lib
mkdir package/usr/lib/webcamstudio
mkdir package/usr/lib/webcamstudio/lib
mkdir package/usr/share
mkdir package/usr/share/pixmaps
mkdir package/usr/share/applications
mkdir package/usr/share/webcamstudio
mkdir package/usr/share/webcamstudio/vloopback-src
mkdir package/DEBIAN

sed -e "s/VERSION/$VALUE/" webcamstudio >package/usr/bin/webcamstudio
cp ../vloopback/webcamstudio-x-install-vloopback package/usr/bin
chmod 755 package/usr/bin/webcamstudio
chmod 755 package/usr/bin/webcamstudio-x-install-vloopback
cp webcamstudio.png package/usr/share/pixmaps
cp webcamstudio.desktop package/usr/share/applications
cp webcamstudio-vloopback-installer.desktop package/usr/share/applications
cp ../dist/WebcamStudio.jar package/usr/lib/webcamstudio
cp ../dist/lib/* package/usr/lib/webcamstudio/lib
cp ../dist/README.TXT package/usr/lib/webcamstudio
cp ../vloopback/* package/usr/share/webcamstudio/vloopback-src

TOTAL=$( du -k -c package | grep total | cut -d t -f 1 );

sed -e "s/VERSION/$VALUE/" -e "s/SIZE/$TOTAL/" webcamstudio.package >package/DEBIAN/control
echo creating deb package...
dpkg -b package
mv package.deb webcamstudio_"$VALUE"_all.deb

rm -R package
