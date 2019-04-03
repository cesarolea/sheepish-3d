# Sheepish 3D

A (very) simple raycasting engine written in ClojureScript. It's a companion to a series of articles on raycasting posted at https://devz.mx/raycasting/

## Getting Started

- Clone the repository to your local computer
- Inside the repository directory: `lein fig-dev`
- Browse to `localhost:9500`

The project includes a `resources/public/index.html` with multiple canvas elements commented out. Each canvas is a different example; simply uncomment one (or several) and reload to see the other examples included.

### Prerequisites

- Clojure
- leiningen

### Development workflow

The project includes [figwheel-main](https://figwheel.org/), so any changes done to the ClojureScript sources are reloaded in the browser. To start figwheel:

```
lein fig-dev
```

It will start the figwheel process and wait for a connection to start the ClojureScript repl. Navigate to `localhost:9500`, it should connect and at that moment the repl is available.

An nREPL is also included so you can connect your editor and send code for evaluation directly from the source (tested with Emacs only). To connect your editor start a Clojure repl:

```
lein repl :headless :port 6666
```

Once started, connect to the repl as usual. In Emacs use `cider-connect-clj` (usually bound to C-c M-c) and enter `localhost` and `6666` when prompted for Host and Port respectively.

Once in the Clojure repl evaluate `(start)` and it should start the figwheel process. Navigate to `localhost:9500`, it should connect and at that moment the repl is available.

### Compiling
From the project directory:

```
lein fig-prod
```

It will generate JavaScript with all optimizations turned on. Output is written to `resources/public/cljs-out/prod-main.js`.

## Built With

* [ClojureScript](https://clojurescript.org/)
* [Figwheel](https://figwheel.org/)
* [Quil](http://quil.info/)

## Authors

* **César Olea** - *Initial work* - [Personal Homepage](https://blog.cesarolea.com)

## License

This project is licensed under the CC0 License.

<p xmlns:dct="http://purl.org/dc/terms/" xmlns:vcard="http://www.w3.org/2001/vcard-rdf/3.0#">
  <a rel="license"
     href="http://creativecommons.org/publicdomain/zero/1.0/">
    <img src="http://i.creativecommons.org/p/zero/1.0/88x31.png" style="border-style: none;" alt="CC0" />
  </a>
  <br />
  To the extent possible under law,
  <a rel="dct:publisher"
     href="https://github.com/cesarolea/sheepish-3d">
    <span property="dct:title">César Olea</span></a>
  has waived all copyright and related or neighboring rights to
  <span property="dct:title">Sheepish-3d</span>.
This work is published from:
<span property="vcard:Country" datatype="dct:ISO3166"
      content="MX" about="https://github.com/cesarolea/sheepish-3d">
  Mexico</span>.
</p>

## Acknowledgments

* id Software for inspiration and countless hours of demons and shotguns (and the textures, sorry about that).
* [Fabien Sanglard](http://fabiensanglard.net/) for his awesome articles and especially the Wolfenstein black book that inspired me to finally sit down and code.
* [This raycasting tutorial](https://permadi.com/1996/05/ray-casting-tutorial-table-of-contents/)
* [This other raycaster in Clojure](https://github.com/hpointu/clj-raycast-world)