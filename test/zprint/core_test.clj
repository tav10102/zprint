(ns zprint.core-test
  (:require [expectations :refer :all]
            [zprint.core :refer :all]
            [zprint.zprint :refer :all]
            [zprint.config :refer :all :exclude
             [set-options! configure-all! get-options]]
            [clojure.repl :refer :all]))

;;
;; # Anonymous Function Tests
;;

;
; Keep tests from configuring from any $HOME/.zprintrc or local .zprintrc
;

(set-options! {:configured? true})

;; 
;; Helper functions for gensyms
;;
;; Basically, did they get all of the information
;;

(defn clean-gensym
  "Removes the unique numbers off of the end of a gensym. p1__3456# becomes 
  p1__"
  [x]
  (if (and (symbol? x) (re-find #"\#$" (name x)))
    (symbol (re-find #".*__" (name x)))
    x))

(defn clean-regex
  "Removes the unique numbers off of the end of a gensym. p1__3456# becomes 
  p1__"
  [x]
  (if (instance? java.util.regex.Pattern x) "regex" x))

(defn trim-gensym-regex
  "Fix up all of the gensyms in a structure so that they compare correctly."
  [x]
  (->> x
       (clojure.walk/postwalk clean-gensym)
       (clojure.walk/postwalk clean-regex)))

(defn testfn
  "This fn is designed to see if reader macros, specifically the
  anonymous fn one, works."
  [a b c]
  (map #(println %) b))

(def x (source-fn 'testfn))
(expect (trim-gensym-regex (read-string x))
        (trim-gensym-regex (read-string (zprint-str x {:parse-string? true}))))

(defn testfn1
  "This fn is designed to see if reader macros, specifically the
  anonymous fn one, works."
  [a b c]
  (let [d a e b f c] (map #(println %) b)))

(def x1 (source-fn 'testfn1))
(expect (trim-gensym-regex (read-string x1))
        (trim-gensym-regex (read-string (zprint-str x1 {:parse-string? true}))))

(defn testfn2
  "This fn is designed to see if reader macros, specifically the
  anonymous fn one, works."
  [a b c]
  (let [d a
        e b
        f c]
    (if (and :a :b)
      [:a :b :c :aaaaaaaaaaaaa :bbbbbbbbbbbbbbb :ccccccccccccc :ddddddddd
       :eeeeeeeeeee :ffffffff]
      (concat (list :averylongkeyword :anotherverylongkeyword
                    :athirdverylongkeywor :afourthone
                    :fifthone :sixthone)))))

(def x2 (source-fn 'testfn2))
(expect (read-string x2) (read-string (zprint-str x2 {:parse-string? true})))

;;
;; # Fidelity
;;
;; The following tests check to see that everything that went in, comes out.
;; There are no actual checks to see if it is pretty, just that it is still
;; all there.
;;

(defn testfn3
  [a b c d]
  [:a :b :c :d]
  [[:a :b :c :d]]
  [:aaaaaaaaaaaaaaaaaa :bbbbbbbbbbbbbbb :ccccccccccccc :ddddddddddddddd
   :eeeeeeeeeeeeeee :fffffffffffffffff :ggggggggggggg]
  [[:aaaaaaaaaaaaaaaaaa :bbbbbbbbbbbbbbb :ccccccccccccc :ddddddddddddddd
    :eeeeeeeeeeeeeee :fffffffffffffffff :ggggggggggggg]]
  [[[:aaaaaaaaaaaaaaaaaa :bbbbbbbbbbbbbbb :ccccccccccccc :ddddddddddddddd
     :eeeeeeeeeeeeeee :fffffffffffffffff :ggggggggggggg]]]
  [[[[:aaaaaaaaaaaaaaaaaa :bbbbbbbbbbbbbbb :ccccccccccccc :ddddddddddddddd
      :eeeeeeeeeeeeeee :fffffffffffffffff :ggggggggggggg]]]]
  (list :aaaaaaaaaaaaaaaaaa
        :bbbbbbbbbbbbbbb :ccccccccccccc
        :ddddddddddddddd :eeeeeeeeeeeeeee
        :fffffffffffffffff :ggggggggggggg)
  (list (list :aaaaaaaaaaaaaaaaaa
              :bbbbbbbbbbbbbbb :ccccccccccccc
              :ddddddddddddddd :eeeeeeeeeeeeeee
              :fffffffffffffffff :ggggggggggggg))
  (list (list (list :aaaaaaaaaaaaaaaaaa
                    :bbbbbbbbbbbbbbb :ccccccccccccc
                    :ddddddddddddddd :eeeeeeeeeeeeeee
                    :fffffffffffffffff :ggggggggggggg))))

(def x3 (source-fn 'testfn3))
(expect (read-string x3) (read-string (zprint-str x3 {:parse-string? true})))

(defn testfn4
  "This function has a map."
  [a b c]
  {:first-key [:aaaaaaaaaa :ggggggggg :bbbbbbbb :cccccccccccc :dddddddddd
               :eeeeeeeee :fffffffff],
   :second-key :ddddd,
   :third-key
     (let [a b
           d c]
       (map a [:eeeeeeeee :ffffff :bbbbbbb :ccccccccccccccccccc :ggggggggg])),
   :fourth-key [:aaaaaaaaaa :ggggggggg
                [:jjjjjjjjjjj :mmmmmmmmmmmmm :nnnnnnnnnnnn :oooooooooooo
                 :ppppppp] :bbbbbbbb :cccccccccccc :dddddddddd :eeeeeeeee
                :fffffffff]})

(def x4 (source-fn 'testfn4))
(expect (read-string x4) (read-string (zprint-str x4 {:parse-string? true})))

(defn testfn5
  [xxx yyy zzz ccc ddd eee]
  (if (and :abcd :efg) (list xxx yyy zzz) (list ccc ddd eee)))

(def x5 (source-fn 'testfn5))
(expect (read-string x5) (read-string (zprint-str x5 {:parse-string? true})))

(defn testfn6
  [xxx yyy zzz ccc ddd eee]
  (if (and :abcd :efgh) (list xxx yyy zzz) (list ccc ddd eee)))

(def x6 (source-fn 'testfn6))
(expect (read-string x6) (read-string (zprint-str x6 {:parse-string? true})))

(defn testfn7 [x] (list x) (map inc x))

(def x7 (source-fn 'testfn7))
(expect (read-string x7) (read-string (zprint-str x7 {:parse-string? true})))

(defn testfn8
  "Test two comment lines after a cond test."
  [x]
  (cond ; one
        ; two
        :stuff
          ; middle
          ; second middle
          :bother
        ; three
        ; four
        :else nil))

(def x8 (source-fn 'testfn8))
(expect (read-string x8) (read-string (zprint-str x8 {:parse-string? true})))

(defn testfn9
  "Test two comment lines after a cond test and the vector non-coll
  printing."
  [x as sdklf dfjskl sdkfj dlskjf lskdjf sdljf lskdjf sldjf lksdj sldkj sdjklf
   sslj sldk lsdkjf sldkj lskj]
  (cond ; one
        ; two
        :stuff
          ; middle
          ; second middle
          :bother
        ; three
        ; four
        :else nil))

(def x9 (source-fn 'testfn9))
(expect (read-string x9) (read-string (zprint-str x9 {:parse-string? true})))

(defn testfn10
  "Test two comment lines after a cond test and the vector non-coll
  printing."
  [l-str r-str
   {:keys [width],
    {:keys [zfocus-style zstring zmap zfirst zsecond zsexpr zcoll zvector?
            znth]}
      :zf,
    :as options} ind style floc zloc]
  (cond ; one
        ; two
        :stuff
          ; middle
          ; second middle
          :bother
        ; three
        ; four
        :else nil))

(def x10 (source-fn 'testfn10))
(expect (read-string x10) (read-string (zprint-str x10 {:parse-string? true})))

(defn testfn11
  "Test two comment lines after a cond test and the vector non-coll
  printing and a loop with a null vector."
  [l-str r-str
   {:keys [width],
    {:keys [zfocus-style zstring zmap zfirst zsecond zsexpr zcoll zvector?
            znth]}
      :zf,
    :as options} ind style floc zloc]
  (cond
    ; one
    ; two
    :stuff
      ; middle
      ; second middle
      (loop [a width
             b l-str
             c style
             d zloc
             e []]
        (let [e a f b] (recur e f c d e)))
    ; three
    ; four
    :else nil))

(def x11 (source-fn 'testfn11))
(expect (read-string x11) (read-string (zprint-str x11 {:parse-string? true})))

(defn testfn12
  "Test comments."
  [a b c d]
  (cond ; comment
        ;
        a (println b)
        b (println c)
        :else d))

(def x12 (source-fn 'testfn12))
(expect (read-string x12) (read-string (zprint-str x12 {:parse-string? true})))

(defn testfn13
  ([{:keys [width], {:keys [zsexpr zstring zfirst zcomment?]} :zf, :as options}
    coll sort?]
   nil)
  ([options coll] (testfn13 options coll nil)))

(def x13 (source-fn 'testfn13))
(expect (read-string x13) (read-string (zprint-str x13 {:parse-string? true})))

(defn testfn14
  [{:keys [width], {:keys [zsexpr zstring zfirst zcomment?]} :zf, :as options}
   coll sort?]
  nil)

(def x14 (source-fn 'testfn14))
(expect (read-string x14) (read-string (zprint-str x14 {:parse-string? true})))

(defn testfn15
  [aaaa bbbb cccc dddd]
  (let [[stuff bother] (split-with aaaa (next bbbb))] (list stuff bother)))

(def x15 (source-fn 'testfn15))
(expect (read-string x15) (read-string (zprint-str x15 {:parse-string? true})))

;;
;; Test if paren is placed after comment line.
;;

(defn testfn16
  []
  (when :true
    ; comment 1
    ; comment 2
    ))

(def x16 (source-fn 'testfn16))
(expect (read-string x16) (read-string (zprint-str x16 {:parse-string? true})))

(defn testfn17
  []
  (let [a true
        c false
        ;comment
        ]
    {:a :b,
     ;comment
     }
    (when :true
      ; comment 1
      ; comment 2
      )))

(def x17 (source-fn 'testfn17))
(expect (read-string x17) (read-string (zprint-str x17 {:parse-string? true})))

;;
;; Get indent under anon fn right
;;

(defn testfn18
  []
  (list #(list (assoc {} :alongkey :alongervalue)
               (assoc {} :anotherlongkey :anotherevenlongervalue))))

(def x18 (source-fn 'testfn18))
(expect (read-string x18) (read-string (zprint-str x18 {:parse-string? true})))

;;
;; # Test merge-deep
;;
;; Merges maps including maps to any level
;;

(expect {:c {:e :j}, :a :b} (merge-deep {:a :b, :c {:e :f}} {:c {:e :j}}))

(expect {:c {:e :z, :k :l}, :a :b}
        (merge-deep {:a :b, :c {:e :f}} {:c {:e :j}} {:c {:k :l}} {:c {:e :z}}))

(expect {:c {:e :f, :h :i}, :a :b}
        (merge-deep {:a :b, :c {:e :f}} {:c {:h :i}}))

(expect {:c :g, :a :b} (merge-deep {:a :b, :c {:e :f}} {:c :g}))

;;
;; # Test :parse-string-all? and its relationship to
;;        :parse {:left-space :keep | :drop} and
;;        :parse {:interpose nil | true | false | "<something>"}

(expect "(defn abc [] (println :a)) (println :a)"
        (zprint-str
          "    (defn abc [] (println :a))      \n\n\n\n\n   (println :a)"
          {:parse-string-all? true,
           :parse {:left-space :keep, :interpose " "}}))

(expect "(defn abc [] (println :a))\n(println :a)"
        (zprint-str
          "    (defn abc [] (println :a))      \n\n\n\n\n   (println :a)"
          {:parse-string-all? true,
           :parse {:left-space :keep, :interpose "\n"}}))

(expect "(defn abc [] (println :a))\n\n(println :a)"
        (zprint-str
          "    (defn abc [] (println :a))      \n\n\n\n\n   (println :a)"
          {:parse-string-all? true,
           :parse {:left-space :keep, :interpose "\n\n"}}))

(expect "(defn abc [] (println :a))\n(println :a)"
        (zprint-str
          "    (defn abc [] (println :a))      \n\n\n\n\n   (println :a)"
          {:parse-string-all? true,
           :parse {:left-space :keep, :interpose true}}))

(expect "(defn abc [] (println :a))\n(println :a)"
        (zprint-str
          "    (defn abc [] (println :a))      \n\n\n\n\n   (println :a)"
          {:parse-string-all? true,
           :parse {:left-space :keep, :interpose nil}}))

(expect "    (defn abc [] (println :a))      \n\n\n\n\n   (println :a)"
        (zprint-str
          "    (defn abc [] (println :a))      \n\n\n\n\n   (println :a)"
          {:parse-string-all? true,
           :parse {:left-space :keep, :interpose false}}))

(expect "(defn abc [] (println :a))\n\n\n\n\n(println :a)"
        (zprint-str
          "    (defn abc [] (println :a))      \n\n\n\n\n   (println :a)"
          {:parse-string-all? true,
           :parse {:left-space :drop, :interpose false}}))

(expect
  "(defn x [] (println x))\n(defn y\n  []\n  (println y)\n  (println z)\n  (println a)\n  (println b)\n  (println c)\n  (println f)\n  (println g)\n  (println h))"
  (zprint-str
    "(defn x [] (println x))\n    (defn y [] (println y) (println z) (println a) (println b) (println c) (println f) (println g) (println h))"
    {:parse-string-all? true, :parse {:left-space :drop, :interpose false}}))

(expect
  "(defn x [] (println x))\n    (defn y\n      []\n      (println y)\n      (println z)\n      (println a)\n      (println b)\n      (println c)\n      (println f)\n      (println g)\n      (println h))"
  (zprint-str
    "(defn x [] (println x))\n    (defn y [] (println y) (println z) (println a) (println b) (println c) (println f) (println g) (println h))"
    {:parse-string-all? true, :parse {:left-space :keep, :interpose false}}))

;;
;; # Test to see if multiple copies of zprint can be run at the same time.
;;
;; This is a bit probabalistic, in that it doesn't always fail.
;;

;; These functions let us clean up after a failure.`

#_(defn bind-var
    "Change the root binding of a single var."
    [the-var var-value]
    (.bindRoot ^clojure.lang.Var the-var var-value))

#_(defn clear-bindings
    "Set to null all of vars in a binding-map."
    [binding-map]
    (mapv #(bind-var % nil) (keys binding-map))
    (reset! zprint.redef/ztype [:none 0]))

(def fs
  (mapv slurp
    ["src/zprint/core.cljc" "src/zprint/zutil.cljc" "src/zprint/ansi.cljc"]))

(expect
  nil
  (redef-state [zprint.zfns zprint.config]
               (reset! zprint.redef/ztype [:none 0])
               #_(clear-bindings zprint.zutil/zipper-binding-map)
               (try (doall (pr-str (pmap #(zprint-file-str % "x") fs)) nil)
                    (catch Exception e
                      (do #_(clear-bindings zprint.zutil/zipper-binding-map)
                          (reset! zprint.redef/ztype [:none 0])
                          (str "Failed to pass test to run multiple zprints "
                               "in the same JVM simultaneouls!")))
                    (finally (redef-state [zprint.zfns zprint.config] nil)))))
(expect
  nil
  (redef-state [zprint.zfns zprint.config]
               (reset! zprint.redef/ztype [:none 0])
               #_(clear-bindings zprint.zutil/zipper-binding-map)
               (try (doall (pr-str (pmap #(zprint-file-str % "x") fs)) nil)
                    (catch Exception e
                      (do #_(clear-bindings zprint.zutil/zipper-binding-map)
                          (reset! zprint.redef/ztype [:none 0])
                          (str "Failed to pass test to run multiple zprints "
                               "in the same JVM simultaneouls!")))
                    (finally (redef-state [zprint.zfns zprint.config] nil)))))
(expect
  nil
  (redef-state [zprint.zfns zprint.config]
               (reset! zprint.redef/ztype [:none 0])
               #_(clear-bindings zprint.zutil/zipper-binding-map)
               (try (doall (pr-str (pmap #(zprint-file-str % "x") fs)) nil)
                    (catch Exception e
                      (do #_(clear-bindings zprint.zutil/zipper-binding-map)
                          (reset! zprint.redef/ztype [:none 0])
                          (str "Failed to pass test to run multiple zprints "
                               "in the same JVM simultaneouls!")))
                    (finally (redef-state [zprint.zfns zprint.config] nil)))))
;;
;; Make sure next tests don't have a problem with the bindings
;;
;; Try *really* hard to clean things up after failure so that it doesn't
;; cascade into other tests.
;;

(expect nil
        (redef-state [zprint.zfns zprint.config]
                     (Thread/sleep 1000)
                     (reset! zprint.redef/ztype [:none 0])
                     #_(clear-bindings zprint.zutil/zipper-binding-map)
                     nil))

;;
;; Test to see if we get exception when trying to use zprint on either a zipper
;; or a structure when we are already using it on the other thing.
;;

(defn multi-test-fail
  "Test the multithreaded capabilities and that they fail when they are  
  supposed to.  This *should* fail!!!."
  []
  (reset! zprint.redef/ztype [:none 0])
  #_(reset! zprint.redef/ztype-history [])
  (doall (let [fs (map slurp
                    ["src/zprint/core.cljc" "src/zprint/zutil.cljc"
                     "src/zprint/ansi.cljc"])
               fss (concat (butlast fs)
                           (list (zprint.config/get-options))
                           (vector (last fs)))]
           (pmap #(if (string? %)
                    (doall (zprint.core/zprint-file-str % "x"))
                    (doall (zprint-str %)))
                 fss))))

;
; These two tests are from finish_test.clj, and both should work before
; the next test.  They are here just to ensure that things are working
; now.
;

(expect 37
        (count (czprint-str [:x :b {:c :d, 'e 'f} '(x y z) "bother"]
                            {:color? false})))

(expect 15
        (count (czprint-str "(x b c)\n {:a :b}"
                            {:parse-string-all? true, :color? false})))


(expect
  "multi-test-fail got an exception as it was supposed to do"
  (try (multi-test-fail)
       (catch Exception e
         ; This is to keep the threads that were still running from messing
         ; up the next tests!
         (do (Thread/sleep 2000)
             (reset! zprint.redef/ztype [:none 0])
             "multi-test-fail got an exception as it was supposed to do"))))

;
; And there they are again, just to ensure the same thing.
;

(expect 37
        (count (czprint-str [:y :b {:c :d, 'e 'f} '(x y z) "bother"]
                            {:color? false})))

(expect 15
        (count (czprint-str "(y b c)\n {:a :b}"
                            {:parse-string-all? true, :color? false})))
