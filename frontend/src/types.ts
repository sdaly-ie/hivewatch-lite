export type Hive = {
  id: number;
  name: string;
  location: string;
};

export type WriteHive = {
  name: string;
  location: string;
};

export type TemperatureReading = {
  id: number;
  temperature: number;
  recordedAt: string;
  hiveId: number;
  hiveName?: string;
};

export type WriteTemperatureReading = {
  temperature: number;
  recordedAt: string;
  hiveId: number;
};
