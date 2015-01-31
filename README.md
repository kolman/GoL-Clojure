# Game of Life in Clojure

This is a tutorial that explains the implementation of the Game of Life in Clojure. It uses interactive [Gorilla-REPL](http://gorilla-repl.org/) to visualise the key parts of the algorithm.

The original algorithm was published here: http://clj-me.cgrand.net/2011/08/19/conways-game-of-life/ This is just slightly polished version to make it more readable for beginners.

## Usage

To play with interactive REPL, do the following:

- Make sure you have installed [Leiningen](http://leiningen.org/)
- Clone this repo, download dependencies and run the REPL: 
```
git clone https://github.com/kolman/GoL-Clojure
cd GoL-Clojure
lein deps
lein gorilla :port 8999
```
- Navigate your browser to [http://localhost:8999/worksheet.html?filename=src/joyful_game/worksheet.clj](http://localhost:8999/worksheet.html?filename=src/joyful_game/worksheet.clj)
- Press Ctrl+Shift+Enter to evaluate the whole page.
