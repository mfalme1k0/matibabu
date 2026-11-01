import { request } from "./api";
import type { Page, Patient, RegisterPatientRequest } from "./types";

export const patientsApi = {
  list: (page = 0, size = 20) => request<Page<Patient>>(`/api/patients?page=${page}&size=${size}`),
  get: (id: string) => request<Patient>(`/api/patients/${id}`),
  // Backend search is an exact phone-number lookup that 404s on no match.
  byPhone: (phone: string) => request<Patient>(`/api/patients/search?phoneNumber=${encodeURIComponent(phone)}`),
  register: (data: RegisterPatientRequest) => request<Patient>("/api/patients", { method: "POST", json: data }),
  update: (id: string, data: RegisterPatientRequest) =>
    request<Patient>(`/api/patients/${id}`, { method: "PUT", json: data }),
  remove: (id: string) => request<void>(`/api/patients/${id}`, { method: "DELETE" }),
};
