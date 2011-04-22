echo What is the version of this package?
read VALUE

rm -R -f package
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
mkdir package/usr/share/webcamstudio/webcamstudio-src
mkdir package/usr/share/webcamstudio/animations
mkdir package/usr/share/webcamstudio/widgets
mkdir package/DEBIAN
mkdir package/etc


sed -e "s/VERSION/$VALUE/" webcamstudio >package/usr/bin/webcamstudio
chmod 755 package/usr/bin/webcamstudio
cp webcamstudio.png package/usr/share/pixmaps
cp webcamstudio.desktop package/usr/share/applications
#cp webcamstudio-vloopback-installer.desktop package/usr/share/applications
cp ../trunk/dist/WebcamStudio.jar package/usr/lib/webcamstudio
cp ../trunk/dist/lib/* package/usr/lib/webcamstudio/lib
cp ../trunk/dist/README.TXT package/usr/lib/webcamstudio
cp ../trunk/vloopback/* package/usr/share/webcamstudio/webcamstudio-src
cp animations/*.anm package/usr/share/webcamstudio/animations
cp widgets/*.xml package/usr/share/webcamstudio/widgets
cp -R service/* package/etc
rm -R -f package/etc/init.d/.svn
rm -R -f package/etc/rc2.d/.svn
rm -R -f package/etc/rc3.d/.svn
rm -R -f package/etc/rc4.d/.svn
rm -R -f package/etc/rc5.d/.svn

TOTAL=$( du -k -c package | grep total | cut -d t -f 1 );

sed -e "s/VERSION/$VALUE/" -e "s/SIZE/$TOTAL/" webcamstudio.package >package/DEBIAN/control
cp postinst package/DEBIAN
chmod 755 package/DEBIAN/postinst

echo creating deb package...
dpkg -b package
mv package.deb webcamstudio_"$VALUE"_all.deb

# creating the tar.gz
rm -R archive
mkdir archive
mkdir archive/webcamstudio
mkdir archive/webcamstudio/lib
mkdir archive/webcamstudio/microphone
mkdir archive/webcamstudio/usr
mkdir archive/webcamstudio/usr/share
mkdir archive/webcamstudio/usr/share/webcamstudio
mkdir archive/webcamstudio/usr/share/webcamstudio/webcamstudio-src
mkdir archive/webcamstudio/etc

cp ../trunk/dist/WebcamStudio.jar archive/webcamstudio
cp ../trunk/dist/lib/* archive/webcamstudio/lib
cp ../trunk/dist/README.TXT archive/webcamstudio
cp -R package/etc archive/webcamstudio
cp ../trunk/vloopback/* archive/webcamstudio/usr/share/webcamstudio/webcamstudio-src
cp webcamstudio.png archive/webcamstudio

echo creating archive...
cd archive
# Create the readme file to install VLOOPBACK as a service
echo "How to install the vloopback module as a service" > webcamstudio/README-Install-VLoopback.txt
echo "------------------------------------------------" >> webcamstudio/README-Install-VLoopback.txt
echo " " >> webcamstudio/README-Install-VLoopback.txt
echo "Sinply copy the 'etc' and 'usr' folder in your " >> webcamstudio/README-Install-VLoopback.txt
echo "system folders and upon rebooting, the vloopback module " >> webcamstudio/README-Install-VLoopback.txt
echo "will be re-installed, even when the kernel was upgraded" >> webcamstudio/README-Install-VLoopback.txt
echo " " >> webcamstudio/README-Install-VLoopback.txt
echo "Remember to have the BUILD-ESSENTIALS to be able to compile the vloopback module" >> webcamstudio/README-Install-VLoopback.txt
tar -cf WebcamStudio_"$VALUE".tar webcamstudio/
gzip WebcamStudio_"$VALUE".tar
mv WebcamStudio_"$VALUE".tar.gz ..
cd ..

# moving files to uploaded folder...
echo moving...
mv WebcamStudio_"$VALUE".tar.gz ../uploaded
mv webcamstudio_"$VALUE"_all.deb ../uploaded

