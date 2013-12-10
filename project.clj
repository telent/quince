(defproject quince "0.1.0-SNAPSHOT"
  :description "QUINCE Is Not Cloud Email"
  :url "http://example.com/FIXME"
  :main quince.core

  ;; The GNU Affero General Public License is a modified version of
  ;; the ordinary GNU GPL version 3. It has one added requirement: if
  ;; you run the program on a server and let other users communicate
  ;; with it there, your server must also allow them to download the
  ;; source code corresponding to the program that it's running. If
  ;; what's running there is your modified version of the program, the
  ;; server's users must get the source code as you modified it.

  :license {:name "GNU Affero General Public License"
            :url "https://www.gnu.org/licenses/agpl-3.0.html"}

  :profiles {:dev {:dependencies [[midje "1.5.0"]]}}

  :dependencies [[org.clojure/clojure "1.5.1"]])
