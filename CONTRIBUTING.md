# Contributing
If you wish to contribute to this project, feel free to [fork Watchlist][1], make changes and submit a [pull request][2]. Make sure to keep the commit style consistent.


## Setup

To work, the project will need a [configuration file][3] for Google Analytics and API keys from [TMDb][4] and [trakt.tv][5]. Place the configuration file named "google-services.json" in the "app" folder and create a file called "keys.xml" in the \res\values folder with these values:-

	<string name="tmdb_api_key">YOUR_TMDB_KEY_HERE</string>
	<string name="trakt_api_key">YOUR_TRAKT_KEY_HERE</string>
	
 [1]: https://github.com/Ronak-LM/Watchlist/fork
 [2]: https://github.com/Ronak-LM/Watchlist/compare
 [3]: https://developers.google.com/mobile/add?platform=android&cntapi=analytics
 [4]: https://www.themoviedb.org/documentation/api
 [5]: http://docs.trakt.apiary.io/