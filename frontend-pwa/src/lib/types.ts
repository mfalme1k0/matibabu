export type Gender = "MALE" | "FEMALE" | "OTHER" | "UNKNOWN";

export interface Patient {
  id: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: Gender;
  phoneNumber: string;
  address: string;
  createdAt: string;
  updatedAt: string;
}

export interface RegisterPatientRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: Gender;
  phoneNumber: string;
  address: string;
  nationalId?: string;
  birthCertificateNumber?: string;
}

// Mirrors Spring Data's Page<T>
export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
