
# wet - web-timer
Simple web timer to be used in browser, mobile, tablet, desktop.

## deployed here
github pages: https://bennoloeffler.github.io/wet/

## hints for css & sass
https://bulma.io/documentation/customize/with-node-sass/  
https://css-tricks.com/styling-cross-browser-compatible-range-inputs-css/

## dev
```
npm start (start sass watching and building)
```
```
shadow-clj watch app
```
```
(start) webserver in repl, intellij idea
```
```
localhost:3000
```

## Deploying for fast start (without fat jar and web server... just html, css, js)
DEPLOY LIKE THAT (not yet automated :-( ):

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

## TODOs

- when running, remove the mouse drag on the timer, so that iOS can be moved up/down by drag
- arrows: set the total time - and also the resume time
- when jumping, set full minutes
- when pressed SHIFT, used seconds. Otherwise, do minutes
- prevent screensaver: https://www.educative.io/answers/how-to-keep-your-screen-awake-using-javascript
- SPACE does not work on ios <-- --> do not work on ios
- use arc for visualisation
- slider off and time from bottom to top, when running
- have a big round button for moving the circle

