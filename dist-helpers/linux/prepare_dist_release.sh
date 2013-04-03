#!/bin/bash
#
# WebcamStudio for GNU/Linux release handle
# Copyright (C) 2013 PhobosK <phobosk@kbfx.net>
# Version 1.0
#
# This script does:
#   - modifies all the needed files from the SVN tree for a release of WebcamStudio.
#   - then it syncs the changes to the online repo.
#   - then prepares source and binary files from the SVN tree.
#   - it uses some env variables that may be set before it is run. If they are not set it autodetects them.
#
# NOTE: This script should be run on Debian like systems

color_ok="\\033[1;32m"
color_error="\\033[1;31m"
color_normal="\\033[0;39m"
color_warn="\\033[1;33m"

prog_ok(){
	echo
	echo -e "${color_ok}$@${color_normal}"
	}

prog_err(){
	echo
	echo -e "${color_error}ERROR: $@${color_normal}\n"
	}

prog_warn(){
	echo -e "${color_warn}$@${color_normal}"
	}

confirm() {
	printf "${color_warn}%s (Y)es/(N)o? [%s]${color_normal}  " "$1" "$2"
	unset user_input
	typeset -g -l confirm_answer="$2"
	read user_input
	if [ "$user_input" != "" ]; then
		confirm_answer="$user_input"
	fi
	}

check_app_version() {
	# Check the current app version that is in debian/changelog
	export APP_NAME="$( head -1 ${SVN_DIR}/debian/changelog | cut -f1 -d ' ' )"

	APP_VERSION_MAJOR="$( head -1 ${SVN_DIR}/debian/changelog | cut -f2 -d '(' | cut -f1 -d '.' )"
	APP_VERSION_MINOR="$( head -1 ${SVN_DIR}/debian/changelog | cut -f2 -d '(' | cut -f2 -d '.' | cut -f1 -d ')')"
	export APP_VERSION="${APP_VERSION_MAJOR}.${APP_VERSION_MINOR}"
	}

get_version_input() {
	printf "${color_warn}\t%s of ${APP_NAME}? [%s]$color_normal:  " "$1" "$2"
	unset user_input
	typeset -g -i answer="$2"
	read user_input
	if [ "$user_input" != "" ]; then
		answer="$user_input"
	fi
	}

# Ensure we have a console text editor set
if ! eval "which editor > /dev/null 2>&1"; then
	prog_warn "It seems that you do not have a console text editor set..."
	if  eval "which nano > /dev/null 2>&1"; then
		prog_ok "Found ${color_warn}nano${color_ok}..."
		TEXT_EDITOR=$(which nano)
	elif eval "which vi > /dev/null 2>&1"; then
		prog_ok "Found ${color_warn}vi${color_ok}..."
		TEXT_EDITOR=$(which vi)
	else
		prog_err "No console text editor found (editor, vi or nano). ABORTING..."
		exit 1
	fi
else
	TEXT_EDITOR=$(which editor)
fi
prog_ok "Console text editor set to: ${color_warn}${TEXT_EDITOR}${color_ok}..."

# Ensure we have the subversion installed
if ! eval "which svnversion > /dev/null 2>&1"; then
	prog_err "The 'subversion' app not installed. Please install it by: 'apt-get install subversion'. ABORTING..."
	exit 1
else
	SVNVERSION_APP=$(which svnversion)
fi
prog_ok "'svnversion' app found: ${color_warn}${SVNVERSION_APP}${color_ok}..."

PACKAGES_PREPARED=0
_DEBFULLNAME="PhobosK"
_DEBEMAIL="phobosk@kbfx.net"
DATE_CHANGELOG="$(LC_ALL=C date '+%d %b %Y')"
DATE_DEB="$(LC_ALL=C date '+%a, %d %b %Y %H:%M:%S %z')"

# Ensure we have a packager name and mail
if [ -z "${DEBFULLNAME}" ] ; then
	prog_warn "The shell variable DEBFULLNAME is not set. Using the built-in one... (${_DEBFULLNAME})"
	export DEBFULLNAME="${_DEBFULLNAME}"
fi
if [ -z "${DEBEMAIL}" ] ; then
	prog_warn "The shell variable DEBEMAIL is not set. Using the built-in one... (${_DEBEMAIL})"
	export DEBEMAIL="${_DEBEMAIL}"
fi

CUR_DIR=`pwd`
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"
SVN_DIR="$( dirname "$( dirname "${SCRIPT_DIR}" )" )"

check_app_version

READY_DIR="$( dirname "${SVN_DIR}" )/${APP_NAME}-files"

# We will work from the base SVN folder
cd "${SVN_DIR}"

# Ensure we are in the SVN repo
if ! [ -d '.svn' ]; then
	prog_err "The $(pwd) is not an SVN folder.\nThis script runs only in an SVN base folder. ABORTING..."
	cd "${CUR_DIR}"
	exit 1
fi

# Prepare a new release changes?
echo -e "\n"
confirm "Shall i prepare a new official ${APP_NAME} release? (with changelogs, modifications etc)" "Y"
if [ "$confirm_answer" = "y" -o "$confirm_answer" = "yes" ]; then
	# Get the new version from the user
	while :
	do
		prog_ok "CURRENT ${APP_NAME} is version: ---> ${APP_VERSION} <---"
		get_version_input "VERSION_MAJOR" "${APP_VERSION_MAJOR}"
		APP_VERSION_MAJOR="$answer"
		get_version_input "VERSION_MINOR" "${APP_VERSION_MINOR}"
		APP_VERSION_MINOR="$answer"
		APP_VERSION="${APP_VERSION_MAJOR}.${APP_VERSION_MINOR}"

		printf "\n${color_warn}${APP_NAME} that will be prepared will be version: ---> ${APP_VERSION} <---\nIs this OK (q to quit)? [Y]:${color_normal}  "
		unset user_input
		read user_input
		if [ "$user_input" = "" -o "$user_input" = "y" -o "$user_input" = "Y" ]; then
			break
		elif [ "$user_input" = "q" -o "$user_input" = "Q" -o "$user_input" = "quit" ]; then
			prog_err "User requested QUIT. NO modifications made."
			cd "${CUR_DIR}"
			exit
		fi
	done

	# SVN update and sync before we change anything in the tree
	prog_ok "Updating the SVN from the online repo..."
	svn up
	if [ $? -ne 0 ]; then
		prog_err "Updating the SVN from the online repo. Please examine the output and re-run the script."
		cd "${CUR_DIR}"
		exit 1
	fi

	# Apply changes to the SVN tree
	prog_ok "Applying the required changes in ${SVN_DIR} folder..."
	sed -i -r "s/(\W*String\W*version\W*=\W*\").*(\")/\1${APP_VERSION}\2/" "${SVN_DIR}/src/webcamstudio/Version.java"

	# Get a new entry for the CHANGELOG from the user, using a text editor
	printf "\n${color_warn}Press any key to add a ${APP_NAME} changelog entry...${color_normal}"
	read user_input
	TMP_FILE=$(mktemp)

cat <<END >>"${TMP_FILE}"
${APP_NAME}-${APP_VERSION}    ${DATE_CHANGELOG}

  * DELETE ME - PUT HERE ALL CHANGES CONCERNING ${APP_NAME}
  * Fixes: PLEASE CHANGE ME
  * Adds: PLEASE CHANGE ME
  * Changes: PLEASE CHANGE ME
  * New upstream version


END
	cat "${SVN_DIR}/CHANGELOG" >>"${TMP_FILE}"

	${TEXT_EDITOR} +3 "${TMP_FILE}"
	if [ $? -ne 0 ]; then
		prog_err "Creating the ${APP_NAME} changelog entry..."
		cd "${CUR_DIR}"
		exit 1
	fi
	prog_ok "Applying the changes to ${SVN_DIR}/CHANGELOG..."
	mv -f "${TMP_FILE}" "${SVN_DIR}/CHANGELOG"


	# Get a new entry for the Ubuntu/Debian changelog from the user, using a text editor
	printf "\n${color_warn}Press any key to add an Ubuntu/Debian changelog entry...${color_normal}"
	read user_input
	TMP_FILE=$(mktemp)

cat <<END >>"${TMP_FILE}"
${APP_NAME} (${APP_VERSION}) unstable; urgency=low

  * DELETE ME - PUT HERE ONLY CHANGES CONCERNING THE DEB PACKAGES
  * Fixes: PLEASE CHANGE ME
  * Adds: PLEASE CHANGE ME
  * Changes: PLEASE CHANGE ME
  * New upstream version

 -- ${DEBFULLNAME} <${DEBEMAIL}>  ${DATE_DEB}

END
	cat "${SVN_DIR}/debian/changelog" >>"${TMP_FILE}"

	${TEXT_EDITOR} +3 "${TMP_FILE}"
	if [ $? -ne 0 ]; then
		prog_err "Creating the Ubuntu/Debian changelog entry..."
		cd "${CUR_DIR}"
		exit 1
	fi
	prog_ok "Applying the changes to ${SVN_DIR}/debian/changelog..."
	mv -f "${TMP_FILE}" "${SVN_DIR}/debian/changelog"

	# SVN update and sync
	prog_ok "Adding new files and folders to SVN..."
	svn add --force .
	if [ $? -ne 0 ]; then
		prog_err "Adding new files and folders to SVN. Please examine the output and re-run the script."
		cd "${CUR_DIR}"
		exit 1
	fi

	prog_ok "Commitiing the new version changes to the online repo..."
	svn commit -m "[RELEASE] New release version: ${APP_VERSION}"
	if [ $? -ne 0 ]; then
		prog_err "Commitiing the new version changes to the online repo. Please examine the output and re-run the script."
		cd "${CUR_DIR}"
		exit 1
	fi
fi

# Prepare official source tarball?
confirm "Shall i prepare an official ${APP_NAME} source tarball" "Y"
if [ "$confirm_answer" = "y" -o "$confirm_answer" = "yes" ]; then
	PACKAGES_PREPARED=1
	
	# Ensure we clean up the SVN tree before we start
	prog_ok "Cleaning up the SVN tree..."
	debuild clean

	if [ $? -ne 0 ]; then
		prog_err "Cleaning up the SVN tree..."
		cd "${CUR_DIR}"
		exit 1
	fi

	# Insert the SVN revision text for the app
	APP_VERSION_RELEASE="$( ${SVNVERSION_APP} | head -1 )"
	echo ${APP_VERSION_RELEASE} > "${SVN_DIR}/src/webcamstudio/build.txt"
	
	prog_ok "Building the dist DEB source files only, with debuild..."
	if [ -z "${GPGKEY}" ] ; then
		prog_warn "The shell variable GPGKEY is not set. That means all generated files will not be signed."
		prog_warn "Don't forget to sign them with 'debsign *.changes' if they will be distributed."
		debuild -S -us -uc -Zbzip2 -nc -tc
	else
		debuild -S -k${GPGKEY} -Zbzip2 -nc -tc
	fi
	
	if [ $? -ne 0 ]; then
		prog_err "Creating the dist official source tarball..."
		
		cd "${CUR_DIR}"
		exit 1
	fi
fi

# Prepare DEB binary packs?
confirm "Shall i prepare ${APP_NAME} Debian binary packages" "Y"
if [ "$confirm_answer" = "y" -o "$confirm_answer" = "yes" ]; then
	PACKAGES_PREPARED=1
	
	prog_ok "Building the dist DEB binary file with debuild..."
	if [ -z "${GPGKEY}" ] ; then
		prog_warn "The shell variable GPGKEY is not set. That means all generated files will not be signed."
		prog_warn "Don't forget to sign them with 'debsign *.changes' if they will be distributed."
		debuild -b -us -uc -tc
	else
		debuild -b -k${GPGKEY} -tc
	fi
	
	if [ $? -ne 0 ]; then
		prog_err "Creating the dist Debian binary packages..."
		cd "${CUR_DIR}"
		exit 1
	fi
fi

if [ "$PACKAGES_PREPARED" = "1" ]; then
	echo -e "\n\n"
	
	# Ensure we have the ready folder created
	if ! [ -d "${READY_DIR}" ]; then
		prog_ok "Creating ${READY_DIR} folder..."
		mkdir -p "${READY_DIR}"
	fi
	
	prog_ok "Moving all generated dist files to ${READY_DIR} folder..."
	find "$( dirname "${SVN_DIR}" )" -maxdepth 1 -type f -regex ".*\.\(dsc\|changes\|build\|bz2\|deb\)" -exec mv -f {} "${READY_DIR}" \; -print

	prog_ok "All Dist files are ready. You can find them here:"
	prog_warn "  ${READY_DIR}\n"

	ls -la ${READY_DIR}/*

	echo -e "\n"
	prog_ok "Also use the produced source tarball as the only tarball for the release."
	prog_warn "Otherwise the different source tarballs will differ in their checksums for the different distributions."
fi

echo -e "\n"

cd "${CUR_DIR}"
