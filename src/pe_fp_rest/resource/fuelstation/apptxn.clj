(ns pe-fp-rest.resource.fuelstation.apptxn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fuel Station Application Transaction Use Cases
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def fpapptxn-fuelstation-create 7)
(def fpapptxn-fuelstation-edit   8)
(def fpapptxn-fuelstation-sync   9)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fuel Station-related Use Case Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def fpapptxnlog-localcreatefuelstation-initiated               0) ;recorded client-side
(def fpapptxnlog-localcreatefuelstation-done                    1) ;recorded client-side
(def fpapptxnlog-localcreatefuelstation-canceled                2) ;recorded client-side

(def fpapptxnlog-localeditfuelstation-initiated                 0) ;recorded client-side
(def fpapptxnlog-localeditfuelstation-done                      1) ;recorded client-side
(def fpapptxnlog-localeditfuelstation-canceled                  2) ;recorded client-side

(def fpapptxnlog-syncfuelstation-initiated                      0) ;recorded client-side
(def fpapptxnlog-syncfuelstation-remote-attempted               1) ;recorded client-side
(def fpapptxnlog-syncfuelstation-remote-skipped-no-conn         2) ;recorded client-side
(def fpapptxnlog-syncfuelstation-remote-proc-started            3)
(def fpapptxnlog-syncfuelstation-remote-proc-done-err-occurred  4)
(def fpapptxnlog-syncfuelstation-remote-proc-done-success       5)
(def fpapptxnlog-syncfuelstation-remote-attempt-resp-received   6) ;recorded client-side
