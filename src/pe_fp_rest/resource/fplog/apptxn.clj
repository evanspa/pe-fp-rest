(ns pe-fp-rest.resource.fplog.apptxn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fuel Purchase Log Application Transaction Use Cases
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def fpapptxn-fplog-create       10)
(def fpapptxn-fplog-edit         11)
(def fpapptxn-fplog-sync         12)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fuel Purchase Log-related Use Case Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def fpapptxnlog-localcreatefplog-initiated               0) ;recorded client-side
(def fpapptxnlog-localcreatefplog-done                    1) ;recorded client-side
(def fpapptxnlog-localcreatefplog-canceled                2) ;recorded client-side

(def fpapptxnlog-localeditfplog-initiated                 0) ;recorded client-side
(def fpapptxnlog-localeditfplog-done                      1) ;recorded client-side
(def fpapptxnlog-localeditfplog-canceled                  2) ;recorded client-side

(def fpapptxnlog-syncfplog-initiated                      0) ;recorded client-side
(def fpapptxnlog-syncfplog-remote-attempted               1) ;recorded client-side
(def fpapptxnlog-syncfplog-remote-skipped-no-conn         2) ;recorded client-side
(def fpapptxnlog-syncfplog-remote-proc-started            3)
(def fpapptxnlog-syncfplog-remote-proc-done-err-occurred  4)
(def fpapptxnlog-syncfplog-remote-proc-done-success       5)
(def fpapptxnlog-syncfplog-remote-attempt-resp-received   6) ;recorded client-side
