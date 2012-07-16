(ns test-tower.main
  (:import  [java.util Date GregorianCalendar])
  (:require [taoensso.tower.ring :as ring])
  (:use [clojure.test]
        [taoensso.tower :as tower :only (with-locale with-scope parse-number t)]))

;; TODO Tests

(defmacro wza [& body] `(with-locale :en_ZA ~@body))
(defmacro wus [& body] `(with-locale :en_US ~@body))
(defmacro wde [& body] `(with-locale :de_DE ~@body))

(deftest test-compare)

(deftest test-number-formatting
  (are [expected actual] (is (= expected (wza (tower/format-number actual))))
       "1,000.1"       1000.10
       "100,000,000.1" 100000000.10
       "1,000"         1000)

  (are [expected actual] (is (= expected (wus (tower/format-number actual))))
       "1,000.1"       1000.10
       "100,000,000.1" 100000000.10
       "1,000"         1000)

  (are [expected actual] (is (= expected (wde (tower/format-number actual))))
       "1.000,1"       1000.10
       "100.000.000,1" 100000000.10
       "1.000"         1000)

  (wus
    (is (= "1,000.1"     (tower/format-number   1000.10)))
    (is (= "1,000"       (tower/format-integer  1000)))
    (is (= "$1,000.10"   (tower/format-currency 1000.10))))

  (wde
    (is (= "1.000,1"     (tower/format-number   1000.10)))
    (is (= "1.000"       (tower/format-integer  1000)))
    (is (= "1.000,10 €"  (tower/format-currency 1000.10)))))

(deftest format-percent
  (are [expected actual] (is (= expected (wza (tower/format-percent actual))))
       "314%"   (float (/ 22 7))
       "200%"   2
       "50%"    (float (/ 1 2))
       "75%"    (float (/ 3 4))))

(deftest format-currency
  (are [expected actual] (is (= expected (wza (tower/format-currency actual))))
       "R 123.45" 123.45
       "R 12,345.00" 12345
       "R 123,456,789.00" 123456789
       "R 123,456,789.20" 123456789.20)

    (are [expected actual] (is (= expected (wus (tower/format-currency actual))))
       "$123.45" 123.45
       "$12,345.00" 12345
       "$123,456,789.00" 123456789
       "$123,456,789.20" 123456789.20)

    (are [expected actual] (is (= expected (wde (tower/format-currency actual))))
       "123,45 €" 123.45
       "12.345,00 €" 12345
       "123.456.789,00 €" 123456789
       "123.456.789,20 €" 123456789.20))

(deftest test-number-parsing
  (are [expected actual] (is (= expected (wza (tower/parse-number actual))))
       1000.01    "1000.01"
       1000.01    "1,000.01"
       1000000.01 "1,000,000.01"
       1.23456    "1.23456")

  (are [expected actual] (is (= expected (wus (tower/parse-number actual))))
       1000.01    "1000.01"
       1000.01    "1,000.01"
       1000000.01 "1,000,000.01"
       1.23456    "1.23456")

  (are [expected actual] (is (= expected (wde (tower/parse-number actual))))
       1000.01    "1000,01"
       1000.01    "1.000,01"
       1000000.01 "1.000.000,01"
       1.23456    "1,23456"))

(deftest test-currency-parsing
  (are [expected actual] (is (= expected (wza (tower/parse-currency actual))))
       123.45        "R 123.45"
       12345         "R 123,45"
       123456.01     "R 123,456.01"
       123456789.01  "R 123,456,789.01")

  (are [expected actual] (is (= expected (wus (tower/parse-currency actual))))
       123.45        "$123.45"
       12345         "$123,45"
       123456.01     "$123,456.01"
       123456789.01  "$123,456,789.01")

  (are [expected actual] (is (= expected (wde (tower/parse-currency actual))))
       123.45  "123,45 €"
       12345   "123.45 €"
       123456.01   "123.456,01 €"
       123456789.01   "123.456.789,01 €"))

(deftest test-dt-formatting
  ;; Please refer to http://docs.oracle.com/javase/1.4.2/docs/api/java/util/Date.html to see why we substract 1900 from year and 1 from month
  ;; that's JDK format.
  (are [expected y m d] (is (= expected (wus (tower/format-date (Date. (Date/UTC (- y 1900) (- m 1) d 0 0 0))))))
       "Feb 1, 2012" 2012 2 1
       "Mar 25, 2012" 2012 3 25)

  (are [expected y m d] (is (= expected (wza (tower/format-date (Date. (Date/UTC (- y 1900) (- m 1) d 0 0 0))))))
       "01 Feb 2012" 2012 2 1
       "25 Mar 2012" 2012 3 25)

  (are [expected y m d] (is (= expected (wde (tower/format-date (Date. (Date/UTC (- y 1900) (- m 1) d 0 0 0))))))
       "01.02.2012" 2012 2 1
       "25.03.2012" 2012 3 25))

(deftest test-dt-parsing)
(deftest test-text-formatting)
(deftest test-dictionary-compiler)
(deftest test-translations)