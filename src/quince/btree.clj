(ns quince.btree
  (:use [midje.sweet]))


(defrecord Tree [key value left right]
  )

(def empty-tree (map->Tree {}))

(defn empty-tree? [tree]
  (or (nil? tree) (nil? (:key tree))))

(defn height [tree]
  (if (empty-tree? tree)
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
       (Tree. (:key grandchild) (:value grandchild)
              (assoc left :right inner-left)
              (assoc tree :left inner-right)))

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
       (Tree. (:key grandchild) (:value grandchild)
              (assoc tree :right inner-left)
              (assoc right :left inner-right)))

     ;; otherwise don't know how to rebalance
     true (assert nil))))

(defn tree-insert
  ([tree [key value]] (tree-insert tree key value))
  ([tree key value]
     (let [tree
           (cond (empty-tree? tree)
                 (Tree. key value nil nil)

                 (<= (compare key (:key tree)) 0)
                 (assoc tree :left (tree-insert (:left tree) key value))

                 (> (compare key (:key tree)) 0)
                 (assoc tree :right (tree-insert (:right tree) key value))
                 )
           imbal (imbalance tree)]
       (if (< -2 imbal 2)
         tree
         (let [r (rebalance tree)]
           (assert (< -2 (imbalance r) 2))
           r)))))

(fact "the height of a tree is the max number of nodes between the root and any leaf"
      (let [tree (reduce (fn [tree key] (tree-insert tree key {}))
                         empty-tree
                         [     8
                            4     12
                          2 6  10  14])]
        (height tree)) => 3)

(fact "adding a key/value to the empty tree makes a tree with one node"
      (tree-insert empty-tree 42 [1 2 3 4]) =>
      {:left nil :right nil :key 42 :value [1 2 3 4]})

(fact "adding an earlier node makes it the left child"
      (let [node1 (tree-insert empty-tree 42 [1 2 3 4])]
        (tree-insert node1 10 [1 1 1]))
      => #(= (:key (:left %)) 10))

(fact "adding a later node makes it the right child"
      (let [node1 (tree-insert empty-tree 42 [1 2 3 4])]
        (tree-insert node1 64 [1 1 1]))
      => #(= (:key (:right %)) 64))

(facts "about imbalance"
       (let [left (reduce (fn [tree key] (tree-insert tree key {}))
                          empty-tree [8 4 12 2 6  10  14 1])
             right (reduce (fn [tree key] (tree-insert tree key {}))
                           empty-tree [-2 -3 -1])]
         (imbalance {:left left :right right :key 0 :value :hello}))
       => -2)

(defn- tree-insert-seq [seq]
  (reduce (fn [tree key] (tree-insert tree key (- key)))
          empty-tree seq))

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

(defn tree-lookup [tree key]
  (and tree
       (condp = (Integer/signum (compare key (:key tree)))
           0 (:value tree)
           -1 (tree-lookup (:left tree) key)
           1 (tree-lookup (:right tree) key))))

(facts "tree-lookup"
      (let [nodes (map #(list %1 %2) (range 1 20) (reverse (range 1 20)))
            tree (reduce (fn [tree [k v]] (tree-insert tree k v))
                         empty-tree nodes)]
        (fact "finds the first value associated with the provided key"
              (tree-lookup tree 5) => 15
              (tree-lookup tree 1) => 19
              (tree-lookup tree 9) => 11)
        (fact "returns nil if not found"
              (tree-lookup tree 44) => nil
              )))

(defn tree-slice [tree start end]
  (let [less? (fn [a b] (< (compare a b) 0))
        k (:key tree)
        combine (fn [some one some-more]
                  #_[some one some-more]
                  (concat some [one] some-more))]
    (cond (empty-tree? tree)
          []
          (less? k start)
          (tree-slice (:right tree) start end)
          (not (less? k end))
          (tree-slice (:left tree) start end)
          true
          (combine
           (tree-slice (:left tree) start end)
           (:value tree)
           (tree-slice (:right tree) start end)))))

(fact "tree-slice returns the values of all nodes between two keys"
      (let [rnd (java.util.Random. 1000)
            tree
            (tree-insert-seq (take 25 (repeatedly #(.nextInt rnd 100))))]
        (tree-slice tree 35 50))
      => [-35 -36 -41 -41 -45 -46 -49])


(defn num-nodes-in [tree]
  (if (empty-tree? tree)
    0
    (+ 1 (num-nodes-in (:left tree)) (num-nodes-in (:right tree)))))

(fact "nodes-in counts the nodes"
      (num-nodes-in (tree-insert-seq (range 0 10))) => 10)
