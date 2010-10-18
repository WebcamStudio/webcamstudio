gcc -c -fPIC libwebcamstudio.c -o libwebcamstudio.o    
gcc -shared -Wl -o libwebcamstudio.so.1.0.1  libwebcamstudio.o
sudo cp libwebcamstudio.so.1.0.1 /usr/lib

