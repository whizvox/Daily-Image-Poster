# Daily Image Poster

A tool to facilitate posting daily images to Reddit. Uses Java Swing as the frontend, Java's HttpClient to submit
requests and receive responses from Reddit using their API, and JImageHash to check if similar images have been posted
before.

Still work-in-progress.

## Technologies

* Java Swing: frontend GUI
* HttpClient (standard Java library): communicating with Reddit's REST API
* [SQLite](https://www.sqlite.org/index.html): database
* [Jackson](https://github.com/FasterXML/jackson): JSON serialization
* [slf4j-simple](https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html): logging
* [JImageHash](https://github.com/KilianB/JImageHash): image fingerprinting

## How to Run

Execute `./gradlew run` in the root directory of this project in your terminal of choice. A `run` directory will be
created and used as the working directory.

## License

This project is licensed under the terms of the MIT license, a copy of which is provided in `LICENSE.txt`.
