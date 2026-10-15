export type Rol = 'USER' | 'MODERATOR';

export type ReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  rol: Rol;
  activo: boolean;
}

export interface Curso {
  id: number;
  titulo: string;
  descripcion: string;
  categoria: string;
  activo: boolean;
  promedioCalificacion: number;
  totalResenas: number;
}

export interface Resena {
  id: number;
  idCurso: number;
  tituloCurso: string;
  idUsuario: number;
  nombreUsuario: string;
  correoUsuario: string;
  calificacion: number;
  comentario: string;
  estado: ReviewStatus;
  fechaCreacion: string;
  fechaActualizacion: string;
}

export interface LoginRespuesta {
  token: string;
  tipoToken: string;
  rol: Rol;
  correo: string;
  nombre: string;
}

export interface ErrorApi {
  mensaje?: string;
  error?: string;
  codigoEstado?: number;
  detalles?: Record<string, string>;
}
