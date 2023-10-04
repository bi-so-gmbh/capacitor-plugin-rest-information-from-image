export interface RestInformationPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
