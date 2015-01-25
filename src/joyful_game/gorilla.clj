(ns joyful-game.gorilla
  (require [gorilla-repl.vega :as vega]))

(defn- uuid [] (str (java.util.UUID/randomUUID)))

(defn- plot-boxes [boxes {:keys [cell-size width height fill opacity] 
                          :or {cell-size 30
                               width :max
                               height :max
                               opacity 1
                               fill "green"}}]  
  "Takes sequence of boxes (map with :x :y :text :opacity keys) and draws them."
  (let [series-name (uuid)
        data (map (fn [{x :x y :y text :text box-opacity :opacity}] {:x (* cell-size x) :y (* cell-size y) :text text :opacity (or box-opacity opacity)}) boxes)]
    (gorilla-repl.vega/vega-view 
      {:scales []
       :marks [{:type "rect", 
                :from {:data series-name}, 
                :properties {:enter {
                                     :x {:field "data.x"}, 
                                     :width {:value (dec cell-size)}
                                     :y {:field "data.y"},                                  
                                     :height { :value (dec cell-size)}  
                                     :fill {:value fill} :fillOpacity {:field "data.opacity"}}}
                }
               {:type "text"
                :from {:data series-name}
                :properties {:enter {:text {:field "data.text"}
                                     :x {:field "data.x" :offset 15},   
                                     :width {:value (dec cell-size)}
                                     :y {:field "data.y"},
                                     :dy {:value (/ cell-size 2)}                                 
                                     :align {:value "center"}
                                     :baseline {:value "middle"}
                                     :fill {:value "black"}
                                     :fontSize {:value (* cell-size 0.6)}}}}
               ], 
       :data [{:name series-name, :values data}]
       :width (inc (apply max (map :x data)))
       :height (inc (apply max (map :y data)))
       })))

(defn plot-cells [cells & {fill :fill :or {fill "red"} :as options}]
  (plot-boxes (map (fn [[x y]] {:x x :y y}) cells) 
              (conj {:fill fill} options)))

(defn plot-frequencies [freqs & {:keys [opacity] :or {opacity 0.3} :as options}]
  (plot-boxes (map (fn [[[x y] frq]] {:x x :y y :text frq :opacity (* opacity frq)}) freqs) options))
