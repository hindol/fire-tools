(ns com.github.hindol.fire-tools
  (:require [rum.core :as rum]
            [citrus.core :as citrus]))

(def initial-state 0)

(defmulti control (fn [event] event))

(defmethod control :init
  []
  {:local-storage
   {:method  :get
    :key     :counter
    :on-read :init-ready}})

(defmethod control :init-ready
  [_ [counter]]
  (if-not (nil? counter)
    {:state (js/parseInt counter)}
    {:state initial-state}))

(defmethod control :inc
  [_ _ counter]
  (let [next-counter (inc counter)]
    {:state next-counter
     :local-storage
     {:method :set
      :data   next-counter
      :key    :counter}}))

(defmethod control :dec
  [_ _ counter]
  (let [next-counter (dec counter)]
    {:state next-counter
     :local-storage
     {:method :set
      :data   next-counter
      :key    :counter}}))

(defn local-storage
  [reconciler controller-name effect]
  (let [{:keys [method data key on-read]} effect]
    (case method
      :set (js/localStorage.setItem (name key) data)
      :get (->> (js/localStorage.getItem (name key))
                (citrus/dispatch! reconciler controller-name on-read))
      nil)))

(rum/defc Counter < rum/reactive
  [r]
  [:div
   [:button {:on-click #(citrus/dispatch! r :counter :dec)} "-"]
   [:span (rum/react (citrus/subscription r [:counter]))]
   [:button {:on-click #(citrus/dispatch! r :counter :inc)} "+"]])

(rum/defc NavbarToggler
  [label]
  [:button.navbar-toggler {:type           "button"
                           :data-bs-toggle "collapse"
                           :data-bs-target (str "#" label)
                           :aria-controls  label
                           :aria-expanded  "false"
                           :aria-label     "Toggle navigation"}
   [:span.navbar-toggler-icon]])

(rum/defc NavLink
  [label]
  [:a.nav-link {:href "#"} label])

(rum/defc Navbar
  [& contents]
  (let [toggle-target "navbarNavAltMarkup"]
    [:nav.navbar.navbar-expand-lg.navbar-light.bg-light
     [:div.container-fluid
      [:a.navbar-brand {:href "#"} "F.I.R.E. Tools"]
      (NavbarToggler toggle-target)
      [:div.collapse.navbar-collapse {:id toggle-target}
       (into
        [:div.navbar-nav]
        contents)]]]))

(rum/defc Main
  [& contents]
  [:div.container-lg
   (into
    [:main]
    contents)])

(rum/defc App
  [& contents]
  (into
   [:div]
   contents))

(defonce reconciler
  (citrus/reconciler
   {:state
    (atom {})
    :controllers
    {:counter control}
    :effect-handlers
    {:local-storage local-storage}}))

(defonce init-ctrl (citrus/broadcast-sync! reconciler :init))

(rum/mount (App (Navbar (NavLink "Home")
                        (NavLink "About"))
                (Main (Counter reconciler)))
           (.getElementById js/document "app"))
