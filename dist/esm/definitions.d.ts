export interface RestInformationPlugin {
    scan(scanCall: IScanCall): Promise<Object>;
}
/**
 * All settings that can be passed to the plugin. The `detectorSize` value must be between
 * `0` and `1`, because it determines how many percent of the screen should be covered by
 * the detector.
 * If the value is greater than 1 the detector will not be visible on the screen.
 */
export interface ISettings {
    beepOnSuccess?: boolean;
    vibrateOnSuccess?: boolean;
    detectorSize?: number;
    detectorAspectRatio?: string;
    drawFocusRect?: boolean;
    focusRectColor?: string;
    focusRectBorderRadius?: number;
    focusRectBorderThickness?: number;
    drawFocusLine?: boolean;
    focusLineColor?: string;
    focusLineThickness?: number;
    drawFocusBackground?: boolean;
    focusBackgroundColor?: string;
    loadingCircleColor?: string;
    loadingCircleSize?: number;
}
/**
 * Data used for the http request. Url, headers and body are required, though both header
 * and body can be an empty object. base64Key and imageTypeKey configure how the image and the
 * image type will be named when they are added to the body.
 */
export interface IRequest {
    url: string;
    headers: Record<string, string>;
    body: Record<string, any>;
    base64Key?: string;
    imageTypeKey?: string;
}
/**
 * The configuration object for the plugin. IRequest is mandatory because it configures needed
 * things like the url, settings can be left empty, which will use the default settings.
 */
export interface IScanCall {
    request: IRequest;
    settings?: ISettings;
}
