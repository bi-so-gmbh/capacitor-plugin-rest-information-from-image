# capacitor-plugin-rest-information-from-image

Gets image based information from a REST api that can be configured. The information returned has to be in json format, and it only does POST calls. 
The image will be sent as part of the body as a base64 string, the key is configurable. Also part of the body is the image type, currently hardcoded as jpeg, whose key can also be configured.

## Install

```bash
npm install capacitor-plugin-rest-information-from-image
npx cap sync
```

## API

<docgen-index>

* [`scan(...)`](#scan)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### scan(...)

```typescript
scan(scanCall: IScanCall) => Promise<object>
```

| Param          | Type                                            |
| -------------- | ----------------------------------------------- |
| **`scanCall`** | <code><a href="#iscancall">IScanCall</a></code> |

**Returns:** <code>Promise&lt;object&gt;</code>

--------------------


### Interfaces


#### IScanCall

The configuration object for the plugin. <a href="#irequest">IRequest</a> is mandatory because it configures needed
things like the url, settings can be left empty, which will use the default settings.

| Prop           | Type                                            |
| -------------- | ----------------------------------------------- |
| **`request`**  | <code><a href="#irequest">IRequest</a></code>   |
| **`settings`** | <code><a href="#isettings">ISettings</a></code> |


#### IRequest

Data used for the http request. Url, headers and body are required, though both header
and body can be an empty object. base64Key and imageTypeKey configure how the image and the
image type will be named when they are added to the body.

| Prop               | Type                                                            |
| ------------------ | --------------------------------------------------------------- |
| **`url`**          | <code>string</code>                                             |
| **`headers`**      | <code><a href="#record">Record</a>&lt;string, string&gt;</code> |
| **`body`**         | <code><a href="#record">Record</a>&lt;string, any&gt;</code>    |
| **`base64Key`**    | <code>string</code>                                             |
| **`imageTypeKey`** | <code>string</code>                                             |


#### ISettings

All settings that can be passed to the plugin. The `detectorSize` value must be between
`0` and `1`, because it determines how many percent of the screen should be covered by
the detector.
If the value is greater than 1 the detector will not be visible on the screen.

| Prop                           | Type                 |
| ------------------------------ | -------------------- |
| **`beepOnSuccess`**            | <code>boolean</code> |
| **`vibrateOnSuccess`**         | <code>boolean</code> |
| **`detectorSize`**             | <code>number</code>  |
| **`detectorAspectRatio`**      | <code>string</code>  |
| **`drawFocusRect`**            | <code>boolean</code> |
| **`focusRectColor`**           | <code>string</code>  |
| **`focusRectBorderRadius`**    | <code>number</code>  |
| **`focusRectBorderThickness`** | <code>number</code>  |
| **`drawFocusLine`**            | <code>boolean</code> |
| **`focusLineColor`**           | <code>string</code>  |
| **`focusLineThickness`**       | <code>number</code>  |
| **`drawFocusBackground`**      | <code>boolean</code> |
| **`focusBackgroundColor`**     | <code>string</code>  |
| **`loadingCircleColor`**       | <code>string</code>  |
| **`loadingCircleSize`**        | <code>number</code>  |
| **`imageWidth`**               | <code>number</code>  |
| **`imageHeight`**              | <code>number</code>  |


### Type Aliases


#### Record

Construct a type with a set of properties K of type T

<code>{ [P in K]: T; }</code>

</docgen-api>
