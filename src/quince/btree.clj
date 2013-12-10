(ns quince.btree
  (:use [midje.sweet]))

(defn height [tree]
  (if (nil? tree)
    0
    (+ 1 (max (height (:left tree)) (height (:right tree))))))

(defn imbalance [tree]
  "Returns the difference between the heights of the right and the left subtree"
  (- (height (:right tree)) (height (:left tree)) ))

(defn rebalance [tree]
  (assert (not (< -2 (imbalance tree) 2)))
  (let [left (:left tree)
        right (:right tree)]
    (cond
     ;; if left-heavy and left child is left-heavy, rotate so that child
     ;; and tree swap
     (and (<= (imbalance tree) -2)
          (<= (imbalance left) -1))
     (let [inside (:right left)]
       (assoc left :right (assoc tree :left inside)))

     ;; if left-heavy and child is right-heavy, arrange with
     ;; grandchild=> new root, child=>new left node, tree=>new right node
     (and (<= (imbalance tree) -2)
          (>= (imbalance left) 1))
     (let [grandchild (:right left)
           inner-left (:left grandchild)
           inner-right (:right grandchild)]
       {:key (:key grandchild) :value (:value grandchild)
        :left (assoc left :right inner-left)
        :right (assoc tree :left inner-right)})

    ;; if right-heavy, similar but opposite
     (and (>= (imbalance tree) 2)
          (>= (imbalance right) 1))
     (let [inside (:left right)]
       (assoc right :left (assoc tree :right inside)))

     ;; and the double rebalance ditto
     (and (>= (imbalance tree) 2)
          (<= (imbalance right) -1))
     (let [grandchild (:left right)
           inner-left (:left grandchild)
           inner-right (:right grandchild)]
       {:key (:key grandchild) :value (:value grandchild)
        :left (assoc tree :right inner-left)
        :right (assoc right :left inner-right)})

     ;; otherwise don't know how to rebalance
     true (assert nil))))

(defn tree-insert [tree key value]
  (let [tree
        (cond (nil? tree)
              {:left nil :right nil :key key :value value}
              (<= key (:key tree))
              (assoc tree :left (tree-insert (:left tree) key value))
              (> key (:key tree))
              (assoc tree :right (tree-insert (:right tree) key value))
              )
        imbal (imbalance tree)]
    (if (< -2 imbal 2)
      tree
      (let [r (rebalance tree)]
        (assert (< -2 (imbalance r) 2))
        r))))

(fact "the height of a tree is the max number of nodes between the root and any leaf"
      (let [tree (reduce (fn [tree key] (tree-insert tree key {}))
                         nil [     8
                                4     12
                               2 6  10  14])]
        (height tree)) => 3)

(fact "adding a key/value to nil makes a tree with one node"
      (tree-insert nil 42 [1 2 3 4]) =>
      {:left nil :right nil :key 42 :value [1 2 3 4]})

(fact "adding an earlier node makes it the left child"
      (let [node1 (tree-insert nil 42 [1 2 3 4])]
        (tree-insert node1 10 [1 1 1]))
      => #(= (:key (:left %)) 10))

(fact "adding a later node makes it the right child"
      (let [node1 (tree-insert nil 42 [1 2 3 4])]
        (tree-insert node1 64 [1 1 1]))
      => #(= (:key (:right %)) 64))

(facts "about imbalance"
       (let [left (reduce (fn [tree key] (tree-insert tree key {}))
                          nil [8 4 12 2 6  10  14 1])
             right (reduce (fn [tree key] (tree-insert tree key {}))
                           nil [-2 -3 -1])]
         (imbalance {:left left :right right :key 0 :value :hello}))
       => -2)

(defn tree-insert-seq [seq]
  (reduce (fn [tree key] (tree-insert tree key {})) nil seq))
(defn balanced? [tree] (< -2 (imbalance tree) 2))

(fact "tree-insert never creates a tree with imbalance greater than 1"
      (tree-insert-seq [5 4 3 2 1]) => balanced?
      (tree-insert-seq [1 2 3 4 5]) => balanced?
      (tree-insert-seq [4 1 2]) => balanced?
      (tree-insert-seq [4 6 5]) => balanced?
      (tree-insert-seq [1 2 3 4 5 6 7 8 9]) => balanced?
      (tree-insert-seq (reverse [1 2 3 4 5 6 7 8 9])) => balanced?
      (tree-insert-seq [1 7 2 6 8 3 2 3 57 8  2 457 4 6 5]) => balanced?
      )
