# Contributing

If you wish to contribute to this project, feel free to [fork Watchlist][1], make changes and submit a [pull request][2]. Make sure to keep the commit style consistent.


## Setup

To work, the project will need API keys from [TMDb][3] and [trakt.tv][4]. Create a file called "keys.xml" in the \res\values folder with these values:-

	<string name="tmdb_api_key">YOUR_TMDB_KEY_HERE</string>
	<string name="trakt_api_key">YOUR_TRAKT_KEY_HERE</string>


## Signing Release Builds

To create release builds, you will have to add your keystore file with the name "keystore.jks" in the app folder. Create a "keystore.properties" file in the the project's root folder with these values:-

	storeFile=keystore.jks
	keyAlias=YOUR_KEY_ALIAS_HERE
	storePassword=YOUR_STORE_PASS_HERE
	keyPassword=YOUR_KEY_PASS_HERE
	
 [1]: https://github.com/Ronak-LM/Watchlist/fork
 [2]: https://github.com/Ronak-LM/Watchlist/compare
 [3]: https://www.themoviedb.org/documentation/api
 [4]: http://docs.trakt.apiary.io/