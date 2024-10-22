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

| Prop           | Type                                            | Description                                                         |
| -------------- | ----------------------------------------------- | ------------------------------------------------------------------- |
| **`request`**  | <code><a href="#irequest">IRequest</a></code>   | request configuration                                               |
| **`settings`** | <code><a href="#isettings">ISettings</a></code> | scanner settings, can be left empty which will use default settings |


#### IRequest

Data used for the http request. Url, headers and body are required, though both header
and body can be an empty object. base64Key and imageTypeKey configure how the image and the
image type will be named when they are added to the body.

| Prop               | Type                                                            | Description                                                   |
| ------------------ | --------------------------------------------------------------- | ------------------------------------------------------------- |
| **`url`**          | <code>string</code>                                             | the url the image will be posted to                           |
| **`headers`**      | <code><a href="#record">Record</a>&lt;string, string&gt;</code> | the headers to include, can be an empty object                |
| **`body`**         | <code><a href="#record">Record</a>&lt;string, any&gt;</code>    | the body to include, can be an empty object                   |
| **`base64Key`**    | <code>string</code>                                             | the key under which the base64 data of the image will be sent |
| **`imageTypeKey`** | <code>string</code>                                             | the key under which the image filetype will be sent           |


#### ISettings

All settings that can be passed to the plugin.

| Prop                           | Type                 | Description                                                                                                          |
| ------------------------------ | -------------------- | -------------------------------------------------------------------------------------------------------------------- |
| **`beepOnSuccess`**            | <code>boolean</code> | when true the device will beep on image capture                                                                      |
| **`vibrateOnSuccess`**         | <code>boolean</code> | when true the device will vibrate on image capture                                                                   |
| **`detectorSize`**             | <code>number</code>  | between '0' and '1', determines the percentage of the screen will be covered                                         |
| **`detectorAspectRatio`**      | <code>string</code>  | aspect ratio of the detector in format 2:1                                                                           |
| **`drawFocusRect`**            | <code>boolean</code> | when true the detection area will be outlined                                                                        |
| **`focusRectColor`**           | <code>string</code>  | color of the detection area outline as hex code (supports alpha)                                                     |
| **`focusRectBorderRadius`**    | <code>number</code>  | corner radius of the detection area                                                                                  |
| **`focusRectBorderThickness`** | <code>number</code>  | border thickness of the detection area outline                                                                       |
| **`drawFocusLine`**            | <code>boolean</code> | when true a line will mark the center of the detection area                                                          |
| **`focusLineColor`**           | <code>string</code>  | color of the focus line as hex code (supports alpha)                                                                 |
| **`focusLineThickness`**       | <code>number</code>  | thickness of the focus line                                                                                          |
| **`drawFocusBackground`**      | <code>boolean</code> | when true the area outside the detector will be filled                                                               |
| **`focusBackgroundColor`**     | <code>string</code>  | color to fill the background with as hex code (supports alpha)                                                       |
| **`loadingCircleColor`**       | <code>string</code>  | the color of the loading spinner as hex (supports alpha)                                                             |
| **`loadingCircleSize`**        | <code>number</code>  | the size of the loading spinner                                                                                      |
| **`imageWidth`**               | <code>number</code>  | image width in px, if width and height are not valid, closest valid size will be used                                |
| **`imageHeight`**              | <code>number</code>  | image height in px, if width and height are not valid, closest valid size will be used                               |
| **`imageCompression`**         | <code>number</code>  | image compression, value between '0' and '1', '1' is uncompressed                                                    |
| **`saveImage`**                | <code>boolean</code> | when true the image will be saved to the device                                                                      |
| **`imageName`**                | <code>string</code>  | prefix of the image name, will be extended with the current date and time                                            |
| **`androidImageLocation`**     | <code>string</code>  | the folder inside Documents to save the image to on android, on iOS it defaults to the app name and can't be changed |


### Type Aliases


#### Record

Construct a type with a set of properties K of type T

<code>{ [P in K]: T; }</code>

</docgen-api>
