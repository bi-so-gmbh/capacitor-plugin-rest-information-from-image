export interface RestInformationPlugin {
    scan(scanCall: IScanCall): Promise<object>;
}
/**
 * All settings that can be passed to the plugin.
 */
export interface ISettings {
    /** when true the device will beep on image capture */
    beepOnSuccess?: boolean;
    /** when true the device will vibrate on image capture */
    vibrateOnSuccess?: boolean;
    /** between '0' and '1', determines the percentage of the screen will be covered */
    detectorSize?: number;
    /** aspect ratio of the detector in format 2:1 */
    detectorAspectRatio?: string;
    /** when true the detection area will be outlined */
    drawFocusRect?: boolean;
    /** color of the detection area outline as hex code (supports alpha) */
    focusRectColor?: string;
    /** corner radius of the detection area */
    focusRectBorderRadius?: number;
    /** border thickness of the detection area outline */
    focusRectBorderThickness?: number;
    /** when true a line will mark the center of the detection area */
    drawFocusLine?: boolean;
    /** color of the focus line as hex code (supports alpha) */
    focusLineColor?: string;
    /** thickness of the focus line */
    focusLineThickness?: number;
    /** when true the area outside the detector will be filled */
    drawFocusBackground?: boolean;
    /** color to fill the background with as hex code (supports alpha) */
    focusBackgroundColor?: string;
    /** the color of the loading spinner as hex (supports alpha) */
    loadingCircleColor?: string;
    /** the size of the loading spinner */
    loadingCircleSize?: number;
    /** image width in px, if width and height are not valid, closest valid size will be used */
    imageWidth?: number;
    /** image height in px, if width and height are not valid, closest valid size will be used */
    imageHeight?: number;
    /** image compression, value between '0' and '1', '1' is uncompressed */
    imageCompression?: number;
    /** when true the image will be saved to the device */
    saveImage?: boolean;
    /** prefix of the image name, will be extended with the current date and time */
    imageName?: string;
    /**
     * the folder inside the external storage to save the image to on android. needs to be in
     * a folder an app is allowed to work in (eg. Documents/ or Download/).
     * On iOS the folder can't be changed and manually and defaults to 'Documents/<AppName>'
     */
    androidImageLocation?: string;
}
/**
 * Data used for the http request. Url, headers and body are required, though both header
 * and body can be an empty object. base64Key and imageTypeKey configure how the image and the
 * image type will be named when they are added to the body.
 */
export interface IRequest {
    /** the url the image will be posted to */
    url: string;
    /** the headers to include, can be an empty object */
    headers: Record<string, string>;
    /** the body to include, can be an empty object */
    body: Record<string, any>;
    /** the key under which the base64 data of the image will be sent */
    base64Key?: string;
    /** the key under which the image filetype will be sent */
    imageTypeKey?: string;
}
/**
 * The configuration object for the plugin. IRequest is mandatory because it configures needed
 * things like the url, settings can be left empty, which will use the default settings.
 */
export interface IScanCall {
    /** request configuration */
    request: IRequest;
    /** scanner settings, can be left empty which will use default settings */
    settings?: ISettings;
}
