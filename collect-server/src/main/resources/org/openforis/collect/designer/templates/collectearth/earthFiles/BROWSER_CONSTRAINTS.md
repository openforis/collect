# Browser constraints — Collect Earth balloon

Anything in this directory ships inside the Google Earth Pro balloon, which
embeds an old AppleWebKit. **Read this before bumping any library, writing
new JS/CSS, or assuming a modern feature works.**

## The runtime

```
Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/538.1 (KHTML, like Gecko)
  Google Earth Pro/7.3.7.1155 Safari/538.1
```

- Engine: WebKit 538.1, roughly Safari 8 (late 2014).
- Vendor: Apple Computer Inc.
- Default balloon viewport: **180 x 40 px** (tiny — design for narrow).

Measured 2026-05-17 with the feature probe at `tools/balloon-feature-probe/`
in the repo root. Re-run that probe if Google Earth Pro is ever updated
(the version is in the User-Agent string above).

## Hard rules

### JavaScript

- **ES5 only at parse time.** No `let`/`const`, arrow functions, classes,
  template literals, destructuring, default params, rest/spread, `**`,
  generators, async/await, optional chaining, nullish coalescing, numeric
  separators, BigInt. Any source must be hand-written ES5 *or* transpiled
  via Babel preset-env.
- **No ES2015+ built-ins at runtime.** No `Promise`, `Map`, `Set`, `WeakMap`,
  `WeakSet`, `Symbol`, `Proxy`, `Reflect`. No `Object.assign`/`entries`/
  `values`/`fromEntries`/`hasOwn`. No `Array.from`/`Array.of`/`#includes`/
  `#flat`/`#flatMap`/`#at`/`#findLast`. No `String#includes`/`#startsWith`/
  `#padStart`/`#replaceAll`/`#matchAll`. No `Number.isInteger`/`isNaN`.
  No `globalThis`, `structuredClone`, RegExp lookbehind, RegExp named groups.
  Polyfill via `core-js@3` if a library bump pulls these in.
- **No `fetch`** — use `XMLHttpRequest` (XHR2 with `withCredentials` and
  `responseType="blob"` works fine).
- **No `URLSearchParams`** — build query strings by hand.
- **No `Element.closest()`** — fine inside jQuery (JS-implemented),
  but raw DOM code in this directory must not call `.closest()` directly.
- **No `addEventListener` options object** — `{ passive: true }` is silently
  treated as a truthy `useCapture` boolean. Stick to the two/three-arg form
  (`addEventListener(type, fn, useCaptureBool)`).
- **No IntersectionObserver / ResizeObserver / PerformanceObserver**.
- **No `requestIdleCallback` / `queueMicrotask`**.
- **No customElements / Shadow DOM / `<template>`**.
- **No `crypto.subtle`, no `TextEncoder`/`TextDecoder`, no `DataTransfer`**.
- **No `navigator.clipboard`** — use `document.execCommand("copy")` with a
  textarea-select trick if you need to put text on the clipboard.

### What JS *does* work (so use it):

- ES5 baseline complete: JSON, Array.isArray, Object.defineProperty,
  Object.keys, Function#bind, Array#forEach/map/filter/reduce, String#trim.
- `XMLHttpRequest` + XHR2, `WebSocket`, `EventSource` (SSE),
  `MutationObserver`, `FormData`, `Blob`, `File`, `FileReader`.
- `element.classList`, `element.dataset`, `element.matches`.
- `URL` constructor, `requestAnimationFrame`, `matchMedia`, `Geolocation`,
  `history.pushState`, `postMessage`.
- `querySelectorAll`, `getComputedStyle`.

### CSS

- **No CSS Custom Properties (`var(--x)`).** Repeat literal values or
  template them server-side. This is the single biggest CSS constraint
  here — no theme variables.
- **No `display: grid`.** Flexbox or floats only.
- **No `gap`** (flex or grid). Use margins between flex children:
  `.parent > * + * { margin-left: 8px }`.
- **No `clamp() / min() / max()`.** Use Flexbox + `min-width`/`max-width`.
- **No `aspect-ratio`.** Use the padding-top-percentage trick.
- **No `inset` shorthand.** Use `top`/`right`/`bottom`/`left`.
- **No `object-fit`.** For image sizing, wrap in a div with `background-image`
  and `background-size: cover`.
- **No modern selectors**: `:has()`, `:is()`, `:where()`, `:focus-visible`,
  `:focus-within`.
- **No `CSS.supports()` API** — no `@supports` blocks. If you need
  feature-detect at runtime, do it from JS using computed-style probes
  (see `tools/balloon-feature-probe/probe.html` for techniques).
- **No `@container` queries**.
- **No `backdrop-filter`, `accent-color`, `color-mix()`, `oklch`,
  `conic-gradient`, `scroll-behavior`, `overscroll-behavior`**.

### What CSS *does* work:

- Flexbox (`display: flex`).
- `calc()`.
- `filter` (e.g. `blur(2px)`) — unprefixed.
- `position: sticky` — unprefixed (surprising, but confirmed).
- Baseline: `transform`, `transition`, `border-radius`, `box-shadow`,
  `rgba()`, gradients.

### Storage

- **`localStorage` and `sessionStorage` are DISABLED** — `setItem` throws.
  This is a sandbox restriction by Google Earth Pro, not a probe artifact.
  Persist state via cookies, IndexedDB, or send it to the server.
- **IndexedDB** is available.
- Cookies enabled.

## Library version ceilings

Anything kept under this directory should respect the floor above.
Maximum-safe versions for libraries the balloon currently uses or might use:

| Library                      | Max safe version           | Notes |
|------------------------------|----------------------------|-------|
| jQuery                       | 3.7.1                      | Latest 3.x; supports IE9+/Safari 5+ |
| jQuery UI                    | 1.13.3                     | Same compatibility floor as jQuery |
| Bootstrap                    | **3.4.1** (final BS3)      | BS4 dropped Safari 8 support |
| bootstrap-datetimepicker     | Eonasdan 4.17.49           | BS3-targeted; Tempus Dominus 5/6 are BS5-only |
| Moment.js                    | 2.30.1                     | Or switch to dayjs 1.11.x (also ES5) |
| jquery.steps                 | 1.1.0 (dormant project)    | Vendor as-is |
| jquery.blockUI               | 2.70.0                     | Dormant but still ES5 |
| SelectBoxIt                  | 3.8.1 (project dead 2013)  | Or replace with Selectize.js v0.12.x (only ES5 picker) |
| Selectize.js                 | 0.12.x (legacy jQuery line)| Tom-Select & Choices.js need ES2015+ runtimes + polyfills |

## Pipeline notes

There is currently **no build/transpile step** for the JS/CSS in this
directory — files are vendored verbatim and served as static resources by
`CollectEarthBalloonGenerator`. If we ever bump to a library that ships
ES2015+ source (Tom-Select, modern Selectize, modern Choices.js, etc.) we
need to first wire up Babel preset-env with a `Safari >= 8` target plus
`core-js@3` polyfills, then run library source through that on build.

Until then, **ES5 source is the only thing we can ship here**.

## Where to look next

- Java side of the generator: `collect-server/src/main/java/org/openforis/collect/io/metadata/collectearth/balloon/`
- Balloon HTML template: `../balloon_template_new.txt`
- The probe used to measure all the above: `tools/balloon-feature-probe/`
