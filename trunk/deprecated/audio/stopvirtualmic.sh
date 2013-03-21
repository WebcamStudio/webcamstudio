INDEX=$( cat /tmp/webcamstudio_audio_index )
echo $INDEX
pactl unload-module $INDEX
rm /tmp/webcamstudio_audio_index

