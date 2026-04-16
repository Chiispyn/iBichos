export interface Usuario {


 // Tus campos personalizados
 rut: string;
 name: string;
 username: string;
 genre: string;
 email: string;
 password?: string; // Opcional porque al leer de la DB no siempre viene la pass
 birthdate: string;

 // Ubicación
 region: string;
 comuna: string;
 address: string;

 level: string;
 xp: number;

 // Compatibilidad con tu código anterior (mapeo de campos)
 createdAt?: string; // Mapearemos 'created' a este si es necesario
 updatedAt?: string; // Mapearemos 'updated' a este si es necesario
}

export type UserFormData = Omit<Usuario, 'id' | 'createdAt' | 'updatedAt'>;

// 🟢 Tipos necesarios para la autenticación
export interface LoginCredentials {
    email: string;
    password: string;
}

