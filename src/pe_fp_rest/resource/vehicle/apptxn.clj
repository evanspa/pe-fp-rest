(ns pe-fp-rest.resource.vehicle.apptxn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Vehicle Application Transaction Use Cases
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def fpapptxn-vehicle-create     4)
(def fpapptxn-vehicle-edit       5)
(def fpapptxn-vehicle-sync       6)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Vehicle-related Use Case Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def fpapptxnlog-localcreatevehicle-initiated               0) ;recorded client-side
(def fpapptxnlog-localcreatevehicle-done                    1) ;recorded client-side
(def fpapptxnlog-localcreatevehicle-canceled                2) ;recorded client-side

(def fpapptxnlog-localeditvehicle-initiated                 0) ;recorded client-side
(def fpapptxnlog-localeditvehicle-done                      1) ;recorded client-side
(def fpapptxnlog-localeditvehicle-canceled                  2) ;recorded client-side

(def fpapptxnlog-syncvehicle-initiated                      0) ;recorded client-side
(def fpapptxnlog-syncvehicle-remote-attempted               1) ;recorded client-side
(def fpapptxnlog-syncvehicle-remote-skipped-no-conn         2) ;recorded client-side
(def fpapptxnlog-syncvehicle-remote-proc-started            3)
(def fpapptxnlog-syncvehicle-remote-proc-done-err-occurred  4)
(def fpapptxnlog-syncvehicle-remote-proc-done-success       5)
(def fpapptxnlog-syncvehicle-remote-attempt-resp-received   6) ;recorded client-side
