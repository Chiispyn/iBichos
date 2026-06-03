import { CheckCircle2, XCircle, AlertTriangle, Clock } from 'lucide-react';

export const getActionLabel = (action: string): string => {
  const dict: Record<string, string> = {
    'REJECT_CAPTURE': 'Rechazó una captura',
    'APPROVE_CAPTURE': 'Aprobó una captura dudosa',
    'HIDE_CAPTURE': 'Ocultó/Eliminó una captura',
    'STRIKE_AND_REJECT': 'Rechazó por fraude y dio un Strike',
    'MAKE_ADMIN': 'Nombró a un nuevo Administrador',
    'REMOVE_ADMIN': 'Revocó permisos de Administrador',
    'BAN_USER': 'Baneó (Shadowban) a un usuario',
    'UNBAN_USER': 'Levantó el baneo a un usuario',
    'UPDATE_CATEGORY': 'Corrigió la especie de un insecto',
    'UPDATE_DANGER_LEVEL': 'Modificó el nivel de peligro de un insecto'
  };
  return dict[action] || action;
};

export const getActionIcon = (action: string) => {
  if (action.includes('REJECT') || action.includes('BAN') || action.includes('HIDE') || action.includes('REMOVE')) {
    return <XCircle className="text-danger" size={18} />;
  }
  if (action.includes('APPROVE') || action.includes('MAKE_ADMIN') || action.includes('UNBAN')) {
    return <CheckCircle2 className="text-success" size={18} />;
  }
  if (action.includes('STRIKE')) {
    return <AlertTriangle className="text-warning" size={18} />;
  }
  return <Clock className="text-secondary" size={18} />;
};