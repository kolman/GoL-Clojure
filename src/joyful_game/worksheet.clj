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
;;; So let's start hacking the joyful Game of Life in Clojure!
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

;; @@

;; @@
