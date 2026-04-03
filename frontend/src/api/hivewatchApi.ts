import axios from 'axios';
import type { Hive, TemperatureReading, WriteHive, WriteTemperatureReading } from '../types';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function getHives(): Promise<Hive[]> {
  const response = await api.get('/api/hives');
  return response.data;
}

export async function createHive(payload: WriteHive): Promise<Hive> {
  const response = await api.post('/api/hives', payload);
  return response.data;
}

export async function updateHive(id: number, payload: WriteHive): Promise<Hive> {
  const response = await api.put(`/api/hives/${id}`, payload);
  return response.data;
}

export async function deleteHive(id: number): Promise<void> {
  await api.delete(`/api/hives/${id}`);
}

export async function getReadings(): Promise<TemperatureReading[]> {
  const response = await api.get('/api/readings');
  return response.data;
}

export async function createReading(payload: WriteTemperatureReading): Promise<TemperatureReading> {
  const response = await api.post('/api/readings', payload);
  return response.data;
}

export async function updateReading(
  id: number,
  payload: WriteTemperatureReading,
): Promise<TemperatureReading> {
  const response = await api.put(`/api/readings/${id}`, payload);
  return response.data;
}

export async function deleteReading(id: number): Promise<void> {
  await api.delete(`/api/readings/${id}`);
}

export async function assignReadingToHive(
  readingId: number,
  hiveId: number,
): Promise<TemperatureReading> {
  const response = await api.put(`/api/readings/${readingId}/assign-hive/${hiveId}`);
  return response.data;
}
