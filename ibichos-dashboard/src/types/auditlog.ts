export interface AuditLog {
  id: string;
  adminId: string;
  adminEmail?: string;
  action: string;
  targetId: string;
  targetType: string;
  timestamp: Date;
}