<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist SYSTEM "file://localhost/System/Library/DTDs/PropertyList.dtd">
<plist version="0.9">
<dict>
	<key>CFBundleName</key>
	<string>${appname}</string>
	<key>CFBundleIdentifier</key>
	<string>${mainClass}</string>
	<key>CFBundleVersion</key>
	<string>100.0</string>
	<key>CFBundleAllowMixedLocalizations</key>
	<string>true</string>
	<key>CFBundleExecutable</key>
	<string>JavaApplicationStub</string>
	<key>CFBundleDevelopmentRegion</key>
	<string>English</string>
	<key>CFBundlePackageType</key>
	<string>APPL</string>
	<key>CFBundleSignature</key>
	<string>????</string>
	<key>CFBundleInfoDictionaryVersion</key>
	<string>6.0</string>
	<key>CFBundleIconFile</key>
	<string>kepler.icns</string>
	<key>Java</key>
	<dict>
		<key>MainClass</key>
		<string>${mainClass}</string>
		<key>JVMVersion</key>
		<string>1.6+</string>
		<key>ClassPath</key>
	    <string>${classpath}</string>
	    <key>Arguments</key>
	    <string>${arguments}</string>
	</dict>
</dict>
</plist>