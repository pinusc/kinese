(ns kinese.data
  (:require [reagent.core :as reagent]))

(defonce state (reagent/atom {:text-controls {:textarea? false
                                              :loading-random? false
                                              :loading? false}
                              :floating-menu {:open? false}
                              :shown-level 2
                              :dictionary '()}))
