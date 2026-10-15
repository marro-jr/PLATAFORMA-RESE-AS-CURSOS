import { Curso, ErrorApi, LoginRespuesta, Resena, ReviewStatus, Usuario } from '../types';

const URL_BASE = '/api';

async function procesarRespuesta<T>(respuesta: Response): Promise<T> {
  if (!respuesta.ok) {
    let mensajeError = 'Ocurrió un error en la solicitud';
    try {
      const errorJson: ErrorApi = await respuesta.json();
      if (errorJson.mensaje) {
        mensajeError = errorJson.mensaje;
      } else if (errorJson.error) {
        mensajeError = errorJson.error;
      }
    } catch {
      mensajeError = `Error HTTP ${respuesta.status}: ${respuesta.statusText}`;
    }
    throw new Error(mensajeError);
  }

  // Si la respuesta no tiene contenido (p.ej. 204)
  if (respuesta.status === 204) {
    return {} as T;
  }

  return (await respuesta.json()) as T;
}

// -----------------------------------------------------------------------------
// SERVICIOS DE AUTENTICACIÓN
// -----------------------------------------------------------------------------

export async function registrarUsuario(peticion: {
  nombre: string;
  correo: string;
  clave: string;
}): Promise<Usuario> {
  const respuesta = await fetch(`${URL_BASE}/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(peticion),
  });
  return procesarRespuesta<Usuario>(respuesta);
}

export async function autenticarUsuario(peticion: {
  correo: string;
  clave: string;
}): Promise<LoginRespuesta> {
  const respuesta = await fetch(`${URL_BASE}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(peticion),
  });
  return procesarRespuesta<LoginRespuesta>(respuesta);
}

// -----------------------------------------------------------------------------
// SERVICIOS DE CURSOS
// -----------------------------------------------------------------------------

export async function listarCursos(): Promise<Curso[]> {
  const respuesta = await fetch(`${URL_BASE}/courses`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });
  return procesarRespuesta<Curso[]>(respuesta);
}

export async function obtenerCursoPorId(idCurso: number): Promise<Curso> {
  const respuesta = await fetch(`${URL_BASE}/courses/${idCurso}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });
  return procesarRespuesta<Curso>(respuesta);
}

// -----------------------------------------------------------------------------
// SERVICIOS DE RESEÑAS
// -----------------------------------------------------------------------------

export async function listarResenasAprobadasPorCurso(idCurso: number): Promise<Resena[]> {
  const respuesta = await fetch(`${URL_BASE}/courses/${idCurso}/reviews`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });
  return procesarRespuesta<Resena[]>(respuesta);
}

export async function crearResena(
  idCurso: number,
  peticion: { calificacion: number; comentario: string },
  token: string
): Promise<Resena> {
  const respuesta = await fetch(`${URL_BASE}/courses/${idCurso}/reviews`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(peticion),
  });
  return procesarRespuesta<Resena>(respuesta);
}

export async function listarMisResenas(token: string): Promise<Resena[]> {
  const respuesta = await fetch(`${URL_BASE}/me/reviews`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });
  return procesarRespuesta<Resena[]>(respuesta);
}

export async function actualizarResenaPropia(
  idResena: number,
  peticion: { calificacion: number; comentario: string },
  token: string
): Promise<Resena> {
  const respuesta = await fetch(`${URL_BASE}/reviews/${idResena}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(peticion),
  });
  return procesarRespuesta<Resena>(respuesta);
}

// -----------------------------------------------------------------------------
// SERVICIOS DE MODERACIÓN (Rol MODERATOR)
// -----------------------------------------------------------------------------

export async function listarResenasModeracion(
  token: string,
  estadoFiltro?: ReviewStatus
): Promise<Resena[]> {
  const url = estadoFiltro
    ? `${URL_BASE}/moderation/reviews?status=${estadoFiltro}`
    : `${URL_BASE}/moderation/reviews`;

  const respuesta = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });
  return procesarRespuesta<Resena[]>(respuesta);
}

export async function moderarResena(
  idResena: number,
  nuevoEstado: ReviewStatus,
  token: string
): Promise<Resena> {
  const respuesta = await fetch(`${URL_BASE}/moderation/reviews/${idResena}/status`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ estado: nuevoEstado }),
  });
  return procesarRespuesta<Resena>(respuesta);
}
