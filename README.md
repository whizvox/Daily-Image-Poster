# Daily Image Poster

A tool to facilitate posting daily images to Reddit. Uses Java Swing as the frontend, Java's HttpClient to submit
requests and receive responses from Reddit using their API, and JImageHash to check if similar images have been posted
before. Can also use a local installation of waifu2x to upscale low resolution images using AI.

Officially functional. Needs more work to be done.

## Technologies

* Java Swing: frontend GUI
* HttpClient (standard Java library) and [Methanol](https://github.com/mizosoft/methanol): communicating with Reddit's
REST API
* [JImageHash](https://github.com/KilianB/JImageHash): image fingerprinting
* [waifu2x-converter-cpp](https://github.com/DeadSix27/waifu2x-converter-cpp): image upscaling
* [SQLite](https://www.sqlite.org/index.html): database
* [Jackson](https://github.com/FasterXML/jackson): JSON serialization
* [slf4j-simple](https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html): logging

## How to Run

Execute `./gradlew run` in the root directory of this project in your terminal of choice. A `run` directory will be
created and used as the working directory.

## License

This project is licensed under the terms of the MIT license, a copy of which is provided in `LICENSE.txt`.
