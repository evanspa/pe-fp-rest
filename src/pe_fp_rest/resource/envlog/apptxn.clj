(ns pe-fp-rest.resource.envlog.apptxn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Environment Log Application Transaction Use Cases
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def fpapptxn-envlog-create      13)
(def fpapptxn-envlog-edit        14)
(def fpapptxn-envlog-sync        15)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Environment Log-related Use Case Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def fpapptxnlog-localcreateenvlog-initiated               0) ;recorded client-side
(def fpapptxnlog-localcreateenvlog-done                    1) ;recorded client-side
(def fpapptxnlog-localcreateenvlog-canceled                2) ;recorded client-side

(def fpapptxnlog-localeditenvlog-initiated                 0) ;recorded client-side
(def fpapptxnlog-localeditenvlog-done                      1) ;recorded client-side
(def fpapptxnlog-localeditenvlog-canceled                  2) ;recorded client-side

(def fpapptxnlog-syncenvlog-initiated                      0) ;recorded client-side
(def fpapptxnlog-syncenvlog-remote-attempted               1) ;recorded client-side
(def fpapptxnlog-syncenvlog-remote-skipped-no-conn         2) ;recorded client-side
(def fpapptxnlog-syncenvlog-remote-proc-started            3)
(def fpapptxnlog-syncenvlog-remote-proc-done-err-occurred  4)
(def fpapptxnlog-syncenvlog-remote-proc-done-success       5)
(def fpapptxnlog-syncenvlog-remote-attempt-resp-received   6) ;recorded client-side
