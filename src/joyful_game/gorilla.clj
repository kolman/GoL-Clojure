(ns joyful-game.gorilla
  (require [gorilla-repl.vega :as vega]))

(defn- uuid [] (str (java.util.UUID/randomUUID)))

(defn- include-zero [x]
  (if (> x 0) 0 x))

(defn- plot-boxes [boxes {:keys [cell-size width height fill opacity] 
                          :or {cell-size 30
                               width :max
                               height :max
                               opacity 1
                               fill "green"}}]  
  "Takes sequence of boxes (map with :x :y :text :opacity keys) and draws them."
  (let [series-name (uuid)
        data (map (fn [{x :x y :y text :text box-opacity :opacity}] {:x x :x2 (inc x) :y y :y2 (inc y) :text text :opacity (or box-opacity opacity)}) boxes)
        max-x (+ 2 (apply max (map :x boxes)))
        min-x (include-zero (dec (apply min (map :x boxes))))
        max-y (+ 2 (apply max (map :y boxes)))
        min-y (include-zero (dec (apply min (map :y boxes))))
        width (- max-x min-x)
        height (- max-y min-y)]
    (gorilla-repl.vega/vega-view 
      {:scales [{:name "x" :range "width" :domain [min-x max-x]}
                {:name "y" :range "height" :domain [min-y max-y] :reverse true}]
       :axes [{:type "x" :scale "x" :ticks "nice" :values (range min-x max-x) :grid true :orient "top" 
               :properties {:labels {:x {:scale "x" :offset (/ cell-size 2)}}}}
              {:type "y" :scale "y" :ticks "nice" :values (range min-y max-y) :grid true 
               :properties {:labels {:y {:scale "y" :offset (/ cell-size 2)}}}}
              {:type "y" :scale "y" :ticks 0 :orient "right"}
              {:type "x" :scale "x" :ticks 0 :orient "bottom"}
              {:type "x" :scale "x" :ticks 0 :orient "top"} ; redraw axis hidden by grid of y axis
              ]
       :marks [{:type "rect", 
                :from {:data series-name}, 
                :properties {:enter {:x {:scale "x" :field "data.x"}
                                     :x2 {:scale "x" :field "data.x2"}
                                     :y {:scale "y" :field "data.y"}
                                     :y2 {:scale "y" :field "data.y2"}
                                     :fill {:value fill} :fillOpacity {:field "data.opacity"}}} }
               {:type "text"
                :from {:data series-name}
                :properties {:enter {:text {:field "data.text"}
                                     :x {:scale "x" :field "data.x" :offset (/ cell-size 2)}
                                     :width {:value (dec cell-size)}
                                     :y {:scale "y" :field "data.y"}
                                     :dy {:value (/ cell-size 2)}
                                     :align {:value "center"}
                                     :baseline {:value "middle"}
                                     :fill {:value "black"}
                                     :fontSize {:value (* cell-size 0.6)}}}}
               ]
       :data [{:name series-name :values data}]
       :width (* cell-size width)
       :height (* cell-size height)
       })))

(defn plot-cells [cells & {fill :fill :or {fill "red"} :as options}]
  (plot-boxes (map (fn [[x y]] {:x x :y y}) cells) 
              (conj {:fill fill} options)))

(defn plot-frequencies [freqs & {:keys [opacity] :or {opacity 0.3} :as options}]
  (plot-boxes (map (fn [[[x y] frq]] {:x x :y y :text frq :opacity (* opacity frq)}) freqs) options))
