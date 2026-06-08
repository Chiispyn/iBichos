// src/__tests__/pages/Auditoria/auditoriaUtils.test.tsx
import { render } from '@testing-library/react';
import { describe, test, expect } from 'vitest';
import { getActionLabel, getActionIcon } from '../../../pages/Auditoria/auditoriaUtils';

describe('TC-64 — Funciones Utilitarias de Auditoría (auditoriaUtils)', () => {
  
  // --- TEST DE TEXTOS ---
  test('getActionLabel devuelve las descripciones correctas', () => {
    // Casos de éxito (lo que está en el diccionario)
    expect(getActionLabel('MAKE_ADMIN')).toBe('Nombró a un nuevo Administrador');
    expect(getActionLabel('STRIKE_AND_REJECT')).toBe('Rechazó por fraude y dio un Strike');
    expect(getActionLabel('UNBAN_USER')).toBe('Levantó el baneo a un usuario');

    // Caso de fallback (si mandan un texto que no existe, debe devolver el mismo texto)
    expect(getActionLabel('ACCION_INVENTADA')).toBe('ACCION_INVENTADA');
  });

  // --- TEST DE ÍCONOS (JSX PURO) ---
  test('getActionIcon asigna las clases CSS correctas según la semántica de la acción', () => {
    
    // 1. Acciones Positivas (Verde / text-success)
    const iconSuccess = getActionIcon('APPROVE_CAPTURE');
    expect(iconSuccess.props.className).toContain('text-success');

    // 2. Acciones Negativas/Destructivas (Rojo / text-danger)
    const iconDanger = getActionIcon('BAN_USER');
    expect(iconDanger.props.className).toContain('text-danger');

    // 3. Acciones de Advertencia (Amarillo / text-warning)
    const iconWarning = getActionIcon('STRIKE_AND_REJECT');
    expect(iconWarning.props.className).toContain('text-warning');

    // 4. Acción Desconocida o Neutra (Gris / text-secondary)
    const iconNeutral = getActionIcon('OTHER_ACTION');
    expect(iconNeutral.props.className).toContain('text-secondary');
  });
  
});