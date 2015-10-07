(ns clj-dynamo.irc-format)


(def colors [:white :black :blue :green :lightRed :brown :purple :orange :yellow :lightGreen :cyan :lightCyan :lightBlue :pink :grey :lightGrey])

(def color-map (zipmap colors (range 16)))


(defn color-text
  [text color]
  (str "\003" (color color-map) text "\003"))


(defn bold
  [text]
  (str "\002" text "\002"))


(defn underline
  [text]
  (str "\037" text "\037"))
