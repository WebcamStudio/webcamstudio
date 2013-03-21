pacmd list-source-outputs | egrep 'index|process.binary' | sed -e "s/^.*application.process.binary = //g" -e "s/^.*index: //g" -e 's/"//g' | sed -e "N; s/\n/,/g"
