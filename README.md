#### TODOs

- when running, remove the mouse drag on the timer, so that iOS can be moved up/down by drag
- arrows: set the total time - and also the resume time
- when jumping, set full minutes
- when pressed SHIFT, used seconds. Otherwise, do minutes
- prevent screensaver: https://www.educative.io/answers/how-to-keep-your-screen-awake-using-javascript
- SPACE does not work on ios <-- --> do not work on ios
- bug: does not play sound on ios, may be solved  
  - by: interaction, when pressing start first time
  - and: https://stackoverflow.com/questions/31776548/why-cant-javascript-play-audio-files-on-iphone-safari
- use arc for visualisation
- ok use a better sounds
- slider off and time from bottom to top, when running
- no: when time is up, show start as re-start button
- remove bug when time is negative
- have a big round button for moving the circle


# wet - web-timer
simple timer to be used in browser.

## deployed here
Current: github pages: https://bennoloeffler.github.io/wet/
Legacy: https://bels-timer.bennoloeffler.repl.co/
Legacy: https://replit.com/@bennoloeffler/timer

## hints for css & sass
https://bulma.io/documentation/customize/with-node-sass/
https://css-tricks.com/styling-cross-browser-compatible-range-inputs-css/

## dev / building
```
npm start (start sass watching and building)
```
```
shadow-clj watch app
```
```
(start) webserver in repl
```
```
lein uberjar
```
## running
To start a web server for the application, run:
```
lein run 
```

## Deploying for fast start (without fat jar, web server - just html, css, js)
DEPLOY LIKE THAT:

1. build the app with shadow-cljs
```shadow-cljs release app```

1. copy the new optimized app.js files from target/public/js to /docs
```cp target/cljsbuild/public/js/app.js docs/app.js```

1. copy a new created screen.css to /docs 
```cp resources/public/css/screen.css docs/screen.css```

1. copy all images from resources/public/img to /docs
```cp resources/public/img/*.png docs/``` 

1. copy sounds from resources/public/sounds to /docs
```cp resources/public/sounds/*.mp3 docs/```

1. commit at least the /doc folder and push to github
```cd doc && git add . && git commit -m "new release" && git push```
