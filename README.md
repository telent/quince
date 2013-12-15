# Quince

QUINCE Is Not Cloud Email

## Installation

* make sure you're running Java 1.7 (or later, maybe, though no later
  version exists at the time of writing)

* use leiningen

## Usage

Don't use it. It doesn't work

## Usage (aspirational)

Run the jar.  It starts a web server and then sends your preferred[*]
web browser to go and look at it.

[*] Hopefully. It invokes `xdg-open`: if the result of this is not to
invoke your preferred web browser, you may need to muck about with
stuff like `$BROWSER` environment variables or similar.

## Running tests

It uses Midje for tests.  To test the MIME parsing it downloads some
messages from a mailing list archive on the internet: because I do not
know the copyright status of those messages, they are not distributed
with the package.  So, before running tests you need to

$ make -C fixtures

## License

Distributed under the GNU Affero General Public License.  This means
that if you change it and then make the resulting system available to
other people, you need to make the code which implements your changes
avilable to them too.

If this is a problem for you, please feel free to get in touch with me
and discuss commercial licensing

