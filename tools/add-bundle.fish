#!/usr/bin/fish
set location .
if [ (string split "/" (pwd --physical))[-1] = "tools" ]
	set location ..
end

function prompt
	echo (read --prompt-str "$argv[1] > ")
end

set internal_name (prompt "internal achievement name")

if [ -z "$internal_name" ]
	echo "aborting"
	exit 0
end

set display_name (prompt "display name")
set description (prompt "description")

echo >> $location/mod-src/assets/bundles/bundle.properties
echo "achievement.$internal_name.name = $display_name" >> $location/mod-src/assets/bundles/bundle.properties
echo "achievement.$internal_name.description = $description" >> $location/mod-src/assets/bundles/bundle.properties
