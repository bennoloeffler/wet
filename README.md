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
I JUST DEPLOYED IT LIKE THAT:
0. build the app with shadow-cljs
```shadow-cljs release app```
You may remove all the dev stuff from shadow-cljs.edn
1. created a new repl.it project:
html css js
2. copied the files from this project by hand:
js code from 
target/public/js/compiled/app.js
per CTRL-C CTRL-V 
to file script.js in repl.it
3. uploaded png as timer.png and added to html

