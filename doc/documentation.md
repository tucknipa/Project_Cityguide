City Guide Documentation
========================

This is a documentation for City Guide app created by [Robo Templates](http://robotemplates.com/). City Guide is a native Android application. You can find here useful info about configuring, customizing, building and publishing the app.

* [Live demo on Google Play](https://play.google.com/store/apps/details?id=com.robotemplates.cityguide)


## Features

* Support for Ice Cream Sandwich (Android 4.0.3) and newer
* Developed with Android Studio & Gradle
* Material design following Android Design Guidelines
* Eight color themes (blue, brown, carrot, gray, green, indigo, red, yellow)
* Animations and effects
* Animated action bar
* Animated floating action button
* Parallax scrolling effect
* Quick return effect
* Ripple effect
* Navigation drawer menu with categories
* List of POI
* Search for POI with suggestion
* Favorite spots
* Geolocation
* Interactive map with clickable spots
* Map layers (normal, satellite, hybrid, terrain)
* POI detail screen (info, map, description)
* Static map image
* Navigate to the spot
* Share POI
* Open web link of the POI
* Phone call and e-mail intents
* Current distance to the POI
* Support for metric and imperial units
* About dialog
* Rate app on Google Play
* Data (categories, POI) is stored in local SQLite database
* Images can be loaded from the Internet or locally
* Caching images
* App works in offline mode
* Google Analytics
* AdMob
* Responsive design and tablet support (portrait, landscape, handling orientation change)
* Support for high-resolution displays (xxxhdpi)
* Multi-language support
* Possibility to build the project without Android Studio / Eclipse (using Gradle & Android SDK)
* Easy configuration
* Well documented
* Top quality clean code created by experienced senior Android developer
* Free support


## Android SDK & Android Studio

This chapter describes how to install Android SDK and Android Studio. You don't have to install Android Studio, but it's better. The project can be built without Android Studio, using Gradle and Android SDK. Gradle is a build system used for building final APK file.

1. Install [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
2. Install [Android SDK](https://developer.android.com/sdk/index.html)
3. Run Android SDK Manager and [download necessary SDK packages](https://developer.android.com/sdk/installing/adding-packages.html), make sure that you have installed Android SDK Tools, Android SDK Platform-tools, Android SDK Build-tools, Android Support Repository, Android Support Library and Google Play services
4. Install [Android Studio](https://developer.android.com/sdk/index.html)
5. Now you should be able to open/edit the Android project and build APK
6. You can also install [Genymotion](http://www.genymotion.com/) - fast Android emulator


## Project structure

Project has the following structure (directories are marked by square braces):

- [doc] - documentation
- [extras] - contains extras
- [extras]/[keystore]
- [extras]/[keystore]/cityguide.keystore - keystore certificate for signing APK
- [extras]/[keystore]/cityguide.properties - alias and password for keystore
- [gradle]
- [gradle]/[wrapper] - Gradle Wrapper
- [mobile] - main module
- [mobile]/[libs] - contains 3rd party libraries
- [mobile]/[src] - contains source code
- [mobile]/[src]/[main]
- [mobile]/[src]/[main]/[assets] - asset files (prepopulated database, images)
- [mobile]/[src]/[main]/[java] - java sources
- [mobile]/[src]/[main]/[res] - xml resources, drawables
- [mobile]/[src]/[main]/AndroidManifest.xml - manifest file
- [mobile]/build.gradle - main build script
- [mobile]/proguard-rules.pro - Proguard config (not used)
- .gitignore - Gitignore file
- build.gradle - parent build script
- gradle.properties - build script properties containing path to keystore
- gradlew - Gradle Wrapper (Unix)
- gradlew.bat - Gradle Wrapper (Windows)
- README.md - readme file
- settings.gradle - build settings containing list of modules

Java packages:

- com.robotemplates.cityguide - contains application class and main config class
- com.robotemplates.cityguide.activity - contains activities representing screens
- com.robotemplates.cityguide.adapter - contains all adapters
- com.robotemplates.cityguide.content - contains content provider for search suggestions
- com.robotemplates.cityguide.database - contains database helper and tools for managing asynchronous database calls
- com.robotemplates.cityguide.database.dao - database access objects
- com.robotemplates.cityguide.database.data - data model wrapper
- com.robotemplates.cityguide.database.model - database models representing SQL tables
- com.robotemplates.cityguide.database.query - database queries
- com.robotemplates.cityguide.dialog - contains dialogs
- com.robotemplates.cityguide.fragment - contains fragments with main application logic
- com.robotemplates.cityguide.geolocation - contains geolocation helper
- com.robotemplates.cityguide.listener - contains listeners
- com.robotemplates.cityguide.utility - contains utilities
- com.robotemplates.cityguide.view - contains custom views, layouts, decorations and other tools for working with views


## Configuration

This chapter describes how to configure the project to be ready for publishing. All these steps are very important!


### 1. Import

Unzip the package and import/open the project in Android Studio. Choose "Import project" on Quick Start screen and select "cityguide-x.y.z" directory.


### 2. Rename package name

1. Create new package in _java_ directory, e.g. "com.mycompany.myapp". Right click on _mobile/src/main/java_ directory -> New -> Package.
2. Select all packages and classes in "com.robotemplates.cityguide" and move (drag) them to the new package. Confirm by click on "Do Refactor".
3. Delete the old package "com.robotemplates.cityguide".
4. Open _mobile/src/main/AndroidManifest.xml_ and rename the package name. Select package name "com.robotemplates.cityguide" -> Right click on selected text -> Refactor -> Rename -> enter the new package name, select "Search in comments and strings" option -> Refactor -> Do Refactor.
5. Clean the project. Main menu -> Build -> Clean Project.
6. Replace all occurrences of "com.robotemplates.cityguide" for a new package name, e.g. "com.mycompany.myapp". Right click on _mobile_ directory -> Replace in Path -> set old and new package names, Case sensitive to true -> Find -> Replace.
7. Clean the project again. Main menu -> Build -> Clean Project.
8. Synchronize the project. Main menu -> Tools -> Android -> Sync Project with Gradle Files.
9. If you see "Activity class does not exist" error, restart Android Studio.


### 3. Rename application name

Open _mobile/src/main/res/values/strings.xml_ and change "City Guide" to your own name. Change _app\_name_ and _drawer\_title_ strings.


### 4. Create launcher icon

Right click on _mobile/src/main/res_ directory -> New -> Image Asset -> Asset type Launcher Icons, Resource name "ic\_launcher", create the icon -> Next -> Finish.

You can also change the icon replacing _ic\_launcher.png_ file in _mipmap-mdpi_, _mipmap-hdpi_, _mipmap-xhdpi_, _mipmap-xxhdpi_, _mipmap-xxxhdpi_ directories. See [Android Cheatsheet for Graphic Designers](http://petrnohejl.github.io/Android-Cheatsheet-For-Graphic-Designers/#screen-densities-and-icon-dimensions) for correct launcher icon dimensions.

Another possibility is to create launcher icons using [Android Asset Studio](http://romannurik.github.io/AndroidAssetStudio/icons-launcher.html).


### 5. Choose color theme

Open _mobile/src/main/AndroidManifest.xml_ and change value of `application.android:theme` attribute. There are 8 themes you can use:

* Theme.CityGuide.Blue
* Theme.CityGuide.Brown
* Theme.CityGuide.Carrot
* Theme.CityGuide.Gray
* Theme.CityGuide.Green
* Theme.CityGuide.Indigo
* Theme.CityGuide.Red
* Theme.CityGuide.Yellow

You also have to modify MainActivity's theme. Main Activity uses a special theme with transparent status bar because of navigation drawer status overlay effect. It is an `activity.android:theme` attribute. Choose one of these themes:

* Theme.CityGuide.TransparentStatusBar.Blue
* Theme.CityGuide.TransparentStatusBar.Brown
* Theme.CityGuide.TransparentStatusBar.Carrot
* Theme.CityGuide.TransparentStatusBar.Gray
* Theme.CityGuide.TransparentStatusBar.Green
* Theme.CityGuide.TransparentStatusBar.Indigo
* Theme.CityGuide.TransparentStatusBar.Red
* Theme.CityGuide.TransparentStatusBar.Yellow


### 6. Prepare database and images

Data (categories, POI) is stored in local SQLite database. Prepopulated database with POI (points of interest) is stored in _mobile/src/main/assets/cityguide.db_. This prepopulated database is automatically copied on device storage on first run of the application and also if the database is updated (see below for more info about database update). Database is in SQLite 3.0 format and has the following structure (SQL script):

```sql
CREATE TABLE `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR , `image` VARCHAR );
CREATE TABLE `pois` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `category_id` BIGINT , `name` VARCHAR , `intro` VARCHAR , `description` VARCHAR , `image` VARCHAR , `link` VARCHAR , `latitude` DOUBLE PRECISION , `longitude` DOUBLE PRECISION , `address` VARCHAR , `phone` VARCHAR , `email` VARCHAR , `favorite` SMALLINT );
CREATE INDEX `pois_category_idx` ON `pois` ( `category_id` );
```

As you can see, there are 2 SQL tables (categories, pois) and 1 index (category\_id) which is also foreign key. This database schema corresponds to database models in the code. You can find models in _com.robotemplates.cityguide.database.model_ package.

This app contains a prepopulated database with demo data. Open database file _mobile/src/main/assets/cityguide.db_ in any SQLite editor and modify data in the database as you need. You can add/remove/edit POI and categories. There are many [SQLite editors](http://www.sqlite.org/cvstrac/wiki?p=ManagementTools). We recommend [SQLite Studio](http://sqlitestudio.pl/) because it is free, open source, cross-platform, portable and intuitive. If you are working with SQLite Studio, don't forget to commit changes. Don't modify structure of the database, modify only data! Database tables have following columns:

categories:
* id (integer) - Unique primary key
* name (string) - Category name
* image (string) - URL of the category image. This field is optional and if it is empty or null, no category image is shown. URL should be in this format: _assets://categories/mycategory.png_. It points to _mobile/src/main/assets/categories_ folder where all category images should be stored.

pois:
* id (integer) - Unique primary key
* category_id (integer) - Foreign key pointing to category id
* name (string) - POI name
* intro (string) - Short introduction text on POI detail screen. This field is optional and if it is empty or null, no text is shown.
* description (string) - Main description text of the POI
* image (string) - URL of the POI image. Image can be loaded from the Internet (URL with standard HTTP protocol) or locally from assets. Local URL should be in this format: _assets://pois/mypoi.jpg_. It points to _mobile/src/main/assets/pois_ folder where all local POI images should be stored.
* link (string) - URL of the web page. This field is optional and if it is empty or null, no web link is shown on the detail screen.
* latitude (double) - Latitude of the POI
* longitude (double) - Longitude of the POI
* address (string) - Address of the POI. This field is optional and if it is empty or null, no address is shown on the detail screen.
* phone (string) - Phone number. This field is optional and if it is empty or null, no phone number is shown on the detail screen.
* email (string) - E-mail address. This field is optional and if it is empty or null, no e-mail address is shown on the detail screen.
* favorite (boolean) - True/false value if the POI is favorite. This field should stay 0 by default. This is the only column modified by the app. All other columns are read only.

There are two special categories: "All spots" and "Favorites". Keep in mind that these categories are automatically added to the menu and does not have to be in the database. Categories are ordered by _id_ and POI are ordered alphabetically by _name_. Search query is looking for a match in _name_, _intro_, _description_ and _address_ fields. Searching is case insensitive.

If you modify prepopulated database in _assets_ folder, internal database on device storage will not be updated automatically. If you make any change in the prepopulated database, you have to increment database version. Open configuration file _/mobile/src/main/java/com/robotemplates/cityguide/CityGuideConfig.java_ and increment number in _DATABASE\_VERSION_ constant. Database helper detects that database data has been changed and copy the prepopulated database on device storage so data in the app will be updated. You have to increment database version every time when you want to publish a new build on Google Play and you have changed the data in prepopulated database.

Name of the prepopulated database is defined in configuration file _/mobile/src/main/java/com/robotemplates/cityguide/CityGuideConfig.java_ in _DATABASE\_NAME_ constant. Database file name should correspond to the file in _mobile/src/main/assets_ directory. The database file should be stored in this directory and not in any sub-directory.


### 7. Setup Google Analytics

Open _mobile/src/main/res/xml/analytics\_app\_tracker.xml_ and change UA code (_ga\_trackingId_ parameter) to your own UA code. You can enable/disable Google Analytics in configuration file _/mobile/src/main/java/com/robotemplates/cityguide/CityGuideConfig.java_.


### 8. Setup AdMob

Open _mobile/src/main/res/values/admob.xml_ and change unit ids (_admob\_unit\_id\_poi\_list_, _admob\_unit\_id\_poi\_detail_ and _admob\_unit\_id\_map_ parameters) to your own unit ids (banner ids). You should also specify your test device id (_admob\_test\_device\_id_ parameter) and use test mode when debugging the app. Requesting test ads is recommended when testing your application so you do not request invalid impressions. You can find your hashed device id in the logcat output by requesting an ad when debugging on your device. You can enable/disable AdMob banners in configuration file _/mobile/src/main/java/com/robotemplates/cityguide/CityGuideConfig.java_.


### 9. Create signing keystore

You need to create your own keystore to sign an APK file before publishing on Google Play. You can create the keystore via [keytool utility](http://docs.oracle.com/javase/7/docs/technotes/tools/solaris/keytool.html) which is part of Java JDK.

1. Run following command: `keytool -genkey -v -keystore cityguide.keystore -alias <your_alias> -keyalg RSA -keysize 2048 -validity 36500` where `<your_alias>` is your alias name. For example your company name or app name. If you are going to publish the app on Google Play, you have to set the validity attribute.
2. Copy new _cityguide.keystore_ file into _extras/keystore_ directory.
3. Open _extras/keystore/cityguide.properties_ and set keystore alias and passwords.
4. Done. Remember that _cityguide.keystore_ and _cityguide.properties_ are automatically read by Gradle script when creating a release APK via _assembleRelease_ command. Paths to these files are defined in _gradle.properties_.


### 10. Setup Google Maps

You need to create an API project in the [Google Developers Console](https://console.developers.google.com), get your app's certificate information and create a new Android API key. Sign-up to Google Developers Console -> APIs & auth -> APIs. Enable "Google Maps Android API v2" and "Static Maps API". Go to APIs & auth -> Credentials. Create a new key for Android applications and add certificate SHA1 fingerprint and package name for your debug and release certificates. Debug keystore is usually available in _/Users/myusername/.android/debug.keystore_ directory. Release keystore is available in _extras/keystore/cityguide.keystore_. Run following command: `keytool -list -v -keystore cityguide.keystore` to get SHA1 fingerprint.

Please follow official [documentation](https://developers.google.com/maps/documentation/android/signup) how to get API key. Then open _mobile/src/main/res/values/strings.xml_ and change API key (_maps\_api\_key_ parameter) to your own API key. After you setup the API key, you should completly uninstall the app (if it is installed) from your device and install again or clear app's data. Otherwise you might have problems with displaying the map.

App uses two APIs:

* [Google Maps Android API](https://developers.google.com/maps/documentation/android/) - for interactive map with clickable spots on map screen
* [Google Maps Static Maps API](https://developers.google.com/maps/documentation/staticmaps/) - for map image preview on POI detail screen (map image is cached)

City Guide also uses geolocation for receiving current position of the device. This position is used for calculating distance from POI. If geolocation is disabled on device, distance is not visible. Distance is displayed in metric or imperial units - it depends on locale settings of the device.


## Customization

This chapter describes some optional customizations of the app.


### Custom colors and icons

You can customize colors in _mobile/src/main/res/values/colors.xml_.

There are 11 category icons. If you need to create the icon for the category, it is recommended to use [Android Asset Studio](http://romannurik.github.io/AndroidAssetStudio/index.html). See [Android Cheatsheet for Graphic Designers](http://petrnohejl.github.io/Android-Cheatsheet-For-Graphic-Designers/#screen-densities-and-icon-dimensions) for correct icon dimensions. Use the icons with highest DPI.


### Custom banner logo in drawer menu

There is a pink texture shown in the drawer menu. You can easily change this texture replacing _banner.png_ file in _drawable-xxhdpi_ directory.


### About dialog

If you want to change the text in About dialog, just open _mobile/src/main/res/values/strings.xml_ and edit _dialog\_about\_message_ string. Note that this text is in HTML format and can also contains links.


### Multi-language support

Create a new directory _mobile/src/main/res/values-xx_ where _xx_ is an [ISO 639-1 code](http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) of the language you want to translate. For example "values-es" for Spanish, "values-fr" for French, "values-de" for German etc. Copy strings.xml from _mobile/src/main/res/values_ into the new directory. Now you can translate texts for specific languages. The language is automatically determined by system device settings. If there is no match with _values-xx_ language, default language in _mobile/src/main/res/values_ is selected. See [Localizing with Resources](http://developer.android.com/guide/topics/resources/localization.html) for more info.


## Building & publishing

This chapter describes how to build APK with Gradle and prepare app for publishing. Android Studio uses Gradle for building Android applications.

You don't need to install Gradle on your system, because there is a [Gradle Wrapper](http://www.gradle.org/docs/current/userguide/gradle_wrapper.html). The wrapper is a batch script on Windows, and a shell script for other operating systems. When you start a Gradle build via the wrapper, Gradle will be automatically downloaded and used to run the build.

1. Open the project in Android Studio
2. Open configuration file _/mobile/src/main/java/com/robotemplates/cityguide/CityGuideConfig.java_ and set constants as required (see below for more info)
3. Open main build script _/mobile/build.gradle_ and set constants as required (see below for more info)
4. Run `gradlew assemble` in console
5. APK should be available in _/mobile/build/outputs/apk_ directory

**Note:** You will also need a "local.properties" file to set the location of the SDK in the same way that the existing SDK requires, using the "sdk.dir" property. Example of "local.properties" on Windows: `sdk.dir=C:\\adt-bundle-windows\\sdk`. Alternatively, you can set an environment variable called "ANDROID\_HOME".

**Tip:** Command `gradlew assemble` builds both - debug and release APK. You can use `gradlew assembleDebug` to build debug APK. You can use `gradlew assembleRelease` to build release APK. Debug APK is signed by debug keystore. Release APK is signed by own keystore, stored in _/extras/keystore_ directory.

**Signing process:** Keystore passwords are automatically loaded from property file during building the release APK. Path to this file is defined in "keystore.properties" property in "gradle.properties" file. If this property or the file does not exist, user is asked for passwords explicitly.


### CityGuideConfig.java

This is the main configuration file. There are some true/false switches. It is very important to correctly set these switches before building the APK.

* LOGS - true for enabling debug logs
* ANALYTICS - true for enabling Google Analytics
* ADMOB\_POI\_LIST\_BANNER - true for enabling Google AdMob banner on POI list screen
* ADMOB\_POI\_DETAIL\_BANNER - true for enabling Google AdMob on POI detail screen
* ADMOB\_MAP\_BANNER - true for enabling Google AdMob on map screen

**Important:** Following configuration should be used for release APK, intended for publishing on Google Play:

```java
public static final boolean LOGS = false;
public static final boolean ANALYTICS = true;
public static final boolean ADMOB_POI_LIST_BANNER = true;
public static final boolean ADMOB_POI_DETAIL_BANNER = true;
public static final boolean ADMOB_MAP_BANNER = true;
``` 


### build.gradle

This is the main build script and there are 4 important constants for defining version code and version name.

* VERSION\_MAJOR
* VERSION\_MINOR
* VERSION\_PATCH
* VERSION\_BUILD

See [Versioning Your Applications](http://developer.android.com/tools/publishing/versioning.html#appversioning) in Android documentation for more info.


## Dependencies

* [Android Support Library](http://developer.android.com/tools/support-library/index.html)
* [AppCompat](https://developer.android.com/reference/android/support/v7/appcompat/package-summary.html)
* [FloatingActionButton](https://github.com/makovkastar/FloatingActionButton)
* [Google Play Services](http://developer.android.com/google/play-services/index.html)
* [Material Dialogs](https://github.com/afollestad/material-dialogs)
* [OrmLite](http://ormlite.com/)
* [RecyclerView Multiselect](https://github.com/bignerdranch/recyclerview-multiselect)
* [StickyScrollViewItems](https://github.com/emilsjolander/StickyScrollViewItems)
* [Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader)


## Demo content

Following images are used in the demo app:

* Eiffel Tower, [CC BY-ND 2.0](https://creativecommons.org/licenses/by-nd/2.0/), https://www.flickr.com/photos/jwhitesmith/5237068251
* Musee d'Orsay - Paris, [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0/), https://www.flickr.com/photos/la\_bretagne\_a\_paris/17537283732/
* Pyramide du Louvre, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/david\_bertho/14114830632/
* Notre Dame's Face, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/kamgtr/7568427554/
* Palais du Luxembourg, Paris, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/randyconnolly/15130715532
* Arc de triomphe de l'Etoile, [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0/), https://www.flickr.com/photos/chagiajose/2472801470
* Sacre-Coeur, [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0/), https://www.flickr.com/photos/gavingilmour/2510254127
* Place des Vosges, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/84554176@N00/8705750707
* Avenue des Champs-Elysees, [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0/), https://www.flickr.com/photos/chagiajose/6968359942
* Pantheon, Paris, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/garyullah/8869799567
* The Centre Pompidou, Paris, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/oh-paris/6093712086
* Grande Arche de la Defense, [CC BY-ND 2.0](https://creativecommons.org/licenses/by-nd/2.0/), https://www.flickr.com/photos/klebtahi/2706168797
* Coupe de France 2010, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/psgmag/4570537428
* Tribune Paris lors de PSG 1-3 OM, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/psgmag/3358593608
* Court Philippe Chatrier, [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0/), https://www.flickr.com/photos/la\_bretagne\_a\_paris/5756481852
* Moulin Rouge, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/juanedc/8124444986
* Au Lapin Agile, Montmartre, [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0/), https://www.flickr.com/photos/helder/2213861623
* Shakespeare and Company Bookstore, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/familyclan/14354270945
* Galeries Lafayette, [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0/), https://www.flickr.com/photos/maitreyoda/14036300378
* Marche Bastille, [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/), https://www.flickr.com/photos/maveric2003/15089335726
* Charles de Gaulle Terminal 2F, [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0/), https://www.flickr.com/photos/fischerfotos/15121210497
* Pitie-Salpetriere Hospital, [CC BY-ND 2.0](https://creativecommons.org/licenses/by-nd/2.0/), https://www.flickr.com/photos/alan\_adriana/3861321643


## Changelog

* Version 1.0.0
	* Initial release


## Developed by

[Robo Templates](http://robotemplates.com/)


## License

[Codecanyon licenses](http://codecanyon.net/licenses)
