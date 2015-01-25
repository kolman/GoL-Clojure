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
  (:require [gorilla-plot.core :as plot]
            [joyful-game.gorilla :as g]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## The Data Structure
;;; 
;;; The grid of cells can be represented in various ways. The most simple and most convenient is a set of [x y] coordinates. Each coordinate is a living cell.
;; **

;; @@
; grid containing three living cells
(def board #{[1 1] [2 0] [3 1]})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;joyful-game.worksheet/board</span>","value":"#'joyful-game.worksheet/board"}
;; <=

;; @@
(g/plot-cells board)
;; @@
;; =>
;;; {"type":"vega","content":{"scales":[],"marks":[{"type":"rect","from":{"data":"faa20955-7f9c-4817-a6fe-b41683a8b3eb"},"properties":{"enter":{"x":{"field":"data.x"},"width":{"value":29},"y":{"field":"data.y"},"height":{"value":29},"fill":{"value":"red"},"fillOpacity":{"field":"data.opacity"}}}},{"type":"text","from":{"data":"faa20955-7f9c-4817-a6fe-b41683a8b3eb"},"properties":{"enter":{"y":{"field":"data.y"},"baseline":{"value":"middle"},"align":{"value":"center"},"fill":{"value":"black"},"width":{"value":29},"dy":{"value":15},"x":{"offset":15,"field":"data.x"},"fontSize":{"value":18.0},"text":{"field":"data.text"}}}}],"data":[{"name":"faa20955-7f9c-4817-a6fe-b41683a8b3eb","values":[{"x":30,"y":30,"text":null,"opacity":1},{"x":60,"y":0,"text":null,"opacity":1},{"x":90,"y":30,"text":null,"opacity":1}]}],"width":91,"height":31},"value":"#gorilla_repl.vega.VegaView{:content {:scales [], :marks [{:type \"rect\", :from {:data \"faa20955-7f9c-4817-a6fe-b41683a8b3eb\"}, :properties {:enter {:x {:field \"data.x\"}, :width {:value 29}, :y {:field \"data.y\"}, :height {:value 29}, :fill {:value \"red\"}, :fillOpacity {:field \"data.opacity\"}}}} {:type \"text\", :from {:data \"faa20955-7f9c-4817-a6fe-b41683a8b3eb\"}, :properties {:enter {:y {:field \"data.y\"}, :baseline {:value \"middle\"}, :align {:value \"center\"}, :fill {:value \"black\"}, :width {:value 29}, :dy {:value 15}, :x {:offset 15, :field \"data.x\"}, :fontSize {:value 18.0}, :text {:field \"data.text\"}}}}], :data [{:name \"faa20955-7f9c-4817-a6fe-b41683a8b3eb\", :values ({:x 30, :y 30, :text nil, :opacity 1} {:x 60, :y 0, :text nil, :opacity 1} {:x 90, :y 30, :text nil, :opacity 1})}], :width 91, :height 31}}"}
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

;; **
;;; ## Counting Living Neighbours
;;; 
;;; Naive solution how to count neighbours is to loop through living cells, find all it's neighbours and then count it. It would work fine for living cells, but what about dead cells, that should become alive when they have exactly three neighbours? We should loop through all neighbouring dead cells of each living cell and count their neighbours too.
;;; 
;;; But there is a more elegant solution. What if we simply produce a list of all neighbours for each living cell and concat it to one huge list? When some cell occurs twice in such list, it means it has two neighbours.
;;; 
;;; Clojure has a function for counting items in list: frequencies
;; **

;; @@
(frequencies [:a :b :b :b :c :c])
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:a</span>","value":":a"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[:a 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:b</span>","value":":b"},{"type":"html","content":"<span class='clj-long'>3</span>","value":"3"}],"value":"[:b 3]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:c</span>","value":":c"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[:c 2]"}],"value":"{:a 1, :b 3, :c 2}"}
;; <=

;; @@
(frequencies [[0 0] [1 2] [2 3] [1 2]])
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"}],"value":"[0 0]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[0 0] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[1 2]"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[[1 2] 2]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"},{"type":"html","content":"<span class='clj-long'>3</span>","value":"3"}],"value":"[2 3]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[2 3] 1]"}],"value":"{[0 0] 1, [1 2] 2, [2 3] 1}"}
;; <=

;; **
;;; So let's try to take a board defined earlier, produce list of neighbours for each cell, concat it into one list and count frequencies:
;; **

;; @@
(frequencies (mapcat neighbours board)) ; mapcat is map + concat
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[2 2]"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[[2 2] 2]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"}],"value":"[0 0]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[0 0] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"},{"type":"html","content":"<span class='clj-long'>-1</span>","value":"-1"}],"value":"[2 -1]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[2 -1] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"}],"value":"[1 0]"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[[1 0] 2]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[1 1]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[1 1] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>4</span>","value":"4"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[4 2]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[4 2] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>3</span>","value":"3"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"}],"value":"[3 0]"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[[3 0] 2]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>3</span>","value":"3"},{"type":"html","content":"<span class='clj-long'>-1</span>","value":"-1"}],"value":"[3 -1]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[3 -1] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>4</span>","value":"4"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[4 1]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[4 1] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>-1</span>","value":"-1"}],"value":"[1 -1]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[1 -1] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[0 2]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[0 2] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"}],"value":"[2 0]"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[[2 0] 2]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>3</span>","value":"3"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[3 1]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[3 1] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[2 1]"},{"type":"html","content":"<span class='clj-long'>3</span>","value":"3"}],"value":"[[2 1] 3]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[1 2]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[1 2] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>3</span>","value":"3"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[3 2]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[3 2] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[0 1]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[0 1] 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>4</span>","value":"4"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"}],"value":"[4 0]"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[[4 0] 1]"}],"value":"{[2 2] 2, [0 0] 1, [2 -1] 1, [1 0] 2, [1 1] 1, [4 2] 1, [3 0] 2, [3 -1] 1, [4 1] 1, [1 -1] 1, [0 2] 1, [2 0] 2, [3 1] 1, [2 1] 3, [1 2] 1, [3 2] 1, [0 1] 1, [4 0] 1}"}
;; <=

;; @@
(plot/compose 
	(g/plot-cells board)
	(g/plot-frequencies (frequencies (mapcat neighbours board)) :opacity 0.2))
;; @@
;; =>
;;; {"type":"vega","content":{"width":91,"height":31,"padding":null,"scales":[],"axes":null,"data":[{"name":"8e9b99db-64c9-4ca3-b7ef-ac428fd78c7f","values":[{"x":30,"y":30,"text":null,"opacity":1},{"x":60,"y":0,"text":null,"opacity":1},{"x":90,"y":30,"text":null,"opacity":1}]},{"name":"ac81c46f-7f4d-4c37-ab2d-199356187aa0","values":[{"x":60,"y":60,"text":2,"opacity":0.4},{"x":0,"y":0,"text":1,"opacity":0.2},{"x":60,"y":-30,"text":1,"opacity":0.2},{"x":30,"y":0,"text":2,"opacity":0.4},{"x":30,"y":30,"text":1,"opacity":0.2},{"x":120,"y":60,"text":1,"opacity":0.2},{"x":90,"y":0,"text":2,"opacity":0.4},{"x":90,"y":-30,"text":1,"opacity":0.2},{"x":120,"y":30,"text":1,"opacity":0.2},{"x":30,"y":-30,"text":1,"opacity":0.2},{"x":0,"y":60,"text":1,"opacity":0.2},{"x":60,"y":0,"text":2,"opacity":0.4},{"x":90,"y":30,"text":1,"opacity":0.2},{"x":60,"y":30,"text":3,"opacity":0.6000000000000001},{"x":30,"y":60,"text":1,"opacity":0.2},{"x":90,"y":60,"text":1,"opacity":0.2},{"x":0,"y":30,"text":1,"opacity":0.2},{"x":120,"y":0,"text":1,"opacity":0.2}]}],"marks":[{"type":"rect","from":{"data":"8e9b99db-64c9-4ca3-b7ef-ac428fd78c7f"},"properties":{"enter":{"x":{"field":"data.x"},"width":{"value":29},"y":{"field":"data.y"},"height":{"value":29},"fill":{"value":"red"},"fillOpacity":{"field":"data.opacity"}}}},{"type":"text","from":{"data":"8e9b99db-64c9-4ca3-b7ef-ac428fd78c7f"},"properties":{"enter":{"y":{"field":"data.y"},"baseline":{"value":"middle"},"align":{"value":"center"},"fill":{"value":"black"},"width":{"value":29},"dy":{"value":15},"x":{"offset":15,"field":"data.x"},"fontSize":{"value":18.0},"text":{"field":"data.text"}}}},{"type":"rect","from":{"data":"ac81c46f-7f4d-4c37-ab2d-199356187aa0"},"properties":{"enter":{"x":{"field":"data.x"},"width":{"value":29},"y":{"field":"data.y"},"height":{"value":29},"fill":{"value":"green"},"fillOpacity":{"field":"data.opacity"}}}},{"type":"text","from":{"data":"ac81c46f-7f4d-4c37-ab2d-199356187aa0"},"properties":{"enter":{"y":{"field":"data.y"},"baseline":{"value":"middle"},"align":{"value":"center"},"fill":{"value":"black"},"width":{"value":29},"dy":{"value":15},"x":{"offset":15,"field":"data.x"},"fontSize":{"value":18.0},"text":{"field":"data.text"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 91, :height 31, :padding nil, :scales [], :axes nil, :data ({:name \"8e9b99db-64c9-4ca3-b7ef-ac428fd78c7f\", :values ({:x 30, :y 30, :text nil, :opacity 1} {:x 60, :y 0, :text nil, :opacity 1} {:x 90, :y 30, :text nil, :opacity 1})} {:name \"ac81c46f-7f4d-4c37-ab2d-199356187aa0\", :values ({:x 60, :y 60, :text 2, :opacity 0.4} {:x 0, :y 0, :text 1, :opacity 0.2} {:x 60, :y -30, :text 1, :opacity 0.2} {:x 30, :y 0, :text 2, :opacity 0.4} {:x 30, :y 30, :text 1, :opacity 0.2} {:x 120, :y 60, :text 1, :opacity 0.2} {:x 90, :y 0, :text 2, :opacity 0.4} {:x 90, :y -30, :text 1, :opacity 0.2} {:x 120, :y 30, :text 1, :opacity 0.2} {:x 30, :y -30, :text 1, :opacity 0.2} {:x 0, :y 60, :text 1, :opacity 0.2} {:x 60, :y 0, :text 2, :opacity 0.4} {:x 90, :y 30, :text 1, :opacity 0.2} {:x 60, :y 30, :text 3, :opacity 0.6000000000000001} {:x 30, :y 60, :text 1, :opacity 0.2} {:x 90, :y 60, :text 1, :opacity 0.2} {:x 0, :y 30, :text 1, :opacity 0.2} {:x 120, :y 0, :text 1, :opacity 0.2})}), :marks ({:type \"rect\", :from {:data \"8e9b99db-64c9-4ca3-b7ef-ac428fd78c7f\"}, :properties {:enter {:x {:field \"data.x\"}, :width {:value 29}, :y {:field \"data.y\"}, :height {:value 29}, :fill {:value \"red\"}, :fillOpacity {:field \"data.opacity\"}}}} {:type \"text\", :from {:data \"8e9b99db-64c9-4ca3-b7ef-ac428fd78c7f\"}, :properties {:enter {:y {:field \"data.y\"}, :baseline {:value \"middle\"}, :align {:value \"center\"}, :fill {:value \"black\"}, :width {:value 29}, :dy {:value 15}, :x {:offset 15, :field \"data.x\"}, :fontSize {:value 18.0}, :text {:field \"data.text\"}}}} {:type \"rect\", :from {:data \"ac81c46f-7f4d-4c37-ab2d-199356187aa0\"}, :properties {:enter {:x {:field \"data.x\"}, :width {:value 29}, :y {:field \"data.y\"}, :height {:value 29}, :fill {:value \"green\"}, :fillOpacity {:field \"data.opacity\"}}}} {:type \"text\", :from {:data \"ac81c46f-7f4d-4c37-ab2d-199356187aa0\"}, :properties {:enter {:y {:field \"data.y\"}, :baseline {:value \"middle\"}, :align {:value \"center\"}, :fill {:value \"black\"}, :width {:value 29}, :dy {:value 15}, :x {:offset 15, :field \"data.x\"}, :fontSize {:value 18.0}, :text {:field \"data.text\"}}}})}}"}
;; <=

;; @@

;; @@
