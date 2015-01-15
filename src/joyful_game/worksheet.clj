;; gorilla-repl.fileformat = 1

;; **
;;; # Conway's Game of Life
;;; 
;;; Have you ever attended a [Code Retreat](http://coderetreat.org/)? If yes, you might know that Conway's Game of Life is a little bit tricky - it seems easy, but is actually hard to do in 45 minutes, reserved for one session. After all, this was the reason the Game of Life was chosen for Code Retreat:) But Clojure ruined it all. Be warned - after going through this worksheet, Code Retreat will never be the same again!
;;; 
;;; For those not familiar with [Game of Life](http://coderetreat.org/gol), it is a "zero-player game", or rather an algorithm, that evolves an initial 2D grid of cells according to the four rules. At each step in time, the following transitions occur:
;;; 
;;; * Any live cell with fewer than two live neighbours dies, as if caused by underpopulation.
;;; * Any live cell with more than three live neighbours dies, as if by overcrowding.
;;; * Any live cell with two or three live neighbours lives on to the next generation.
;;; * Any dead cell with exactly three live neighbours becomes a live cell.
;;; 
;;; There are interesting patterns of moving and still-life [patterns](http://en.wikipedia.org/wiki/Conway%27s_Game_of_Life#Examples_of_patterns), and one of them - [the Glider](http://en.wikipedia.org/wiki/Glider_%28Conway%27s_Life%29#Hacker_emblem) - was even proposed as an emblem to represent the hacker subculture.
;;; 
;;; <img src="http://upload.wikimedia.org/wikipedia/commons/9/96/Animated_glider_emblem.gif">
;;; 
;;; ## Game of Life in Clojure
;;; 
;;; Typical implementation in object-oriented languages (as seen at Code Retreats) involve definition of the Cell class, overriding equals and hashCode methods. Then something like Board class and finding all neighbouring cells. If the 45-minutes session is not already over, another goal is to find all neighbouring cell for each cell and determine whether it should survive. And then comes the hard part of this approach - finding dead cells that should become alive.
;;; 
;;; It's truly impossible to fit this in 45 minutes; yet the complete algorithm in Clojure takes only few minutes. How is it possible?
;;; 
;;; It is a combination of two things: The algorithm and the powerful language.
;;; 
;;; So let's start hacking the joyful Game of Life in Clojure!
;; **

;; **
;;; ## Namespace and Imports
;;; 
;;; First, we declare the namespace for this piece of code and import some plotting functions that allows to visualise the grid.
;; **

;; @@
(ns joyful-game.worksheet
  (:require [gorilla-plot.core :as plot]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## The Data Structure
;;; 
;;; We can represent the grid of cells in various ways. The most simple and most convenient is a set of [x y] coordinates. Each coordinate is a living cell.
;; **

;; @@
; grid containing three living cells
(def board #{[1 1] [2 2] [0 2]})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;joyful-game.worksheet/board</span>","value":"#'joyful-game.worksheet/board"}
;; <=

;; **
;;; ## Finding the Neighbours
;;; 
;;; Then we define one handy function, that will return all possible neighbours for a given cell.
;; **

;; @@
(defn neighbours [[x y]] ; parameter cell is destructured into x and y
  (for [dx [-1 0 1] dy [-1 0 1] ; for all combinations of dx and dy...
        :when (not (= 0 dx dy))] ; ...except when dx = 0 = dy...
    [(+ x dx) (+ y dy)])) ; ...add dx to x and dy to y

; let's try it out
(neighbours [1 2])
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[0 1]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[0 2]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>3</span>","value":"3"}],"value":"[0 3]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[1 1]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>3</span>","value":"3"}],"value":"[1 3]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[2 1]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[2 2]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"},{"type":"html","content":"<span class='clj-long'>3</span>","value":"3"}],"value":"[2 3]"}],"value":"([0 1] [0 2] [0 3] [1 1] [1 3] [2 1] [2 2] [2 3])"}
;; <=

;; @@

;; @@
