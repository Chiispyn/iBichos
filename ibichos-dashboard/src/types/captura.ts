export interface Captura {
  id: string;
  imageUrl: string;
  category: string;
  dangerLevel: string;
  confidence: number;
  userId: string;
  needsReview?: boolean; // Opcional porque en Catálogo no siempre se usa
  status?: string;
  userDisplayName?: string;
  userEmail?: string;
  insectName?: string;
  scientificName?: string;
  lat?: number;
  lng?: number;
  moderatedBy?: string;
  moderatorEmail?: string;
  timestamp?: any; // Idealmente aquí luego podrías tiparlo como Timestamp de Firebase
}