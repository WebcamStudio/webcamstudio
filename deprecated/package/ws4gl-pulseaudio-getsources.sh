pacmd list-sources | grep name: | sed -e "s/<//g" -e "s/>//g" -e "s/name://g " -e "s/^. //g"
