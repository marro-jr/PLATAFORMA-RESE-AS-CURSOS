import React, { useEffect, useState, useCallback } from 'react';
import { Resena } from '../types';
import { listarMisResenas } from '../services/api';
import { usarAutenticacion } from '../context/AuthContext';
import { CalificacionEstrellas } from './StarRating';

interface PropiedadesMisResenas {
  alEditarResena: (resena: Resena) => void;
  alIrACursos: () => void;
}

export const MisResenas: React.FC<PropiedadesMisResenas> = ({
  alEditarResena,
  alIrACursos,
}) => {
  const { token } = usarAutenticacion();
  const [resenas, setResenas] = useState<Resena[]>([]);
  const [cargando, setCargando] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const cargarResenas = useCallback(async () => {
    if (!token) return;
    setCargando(true);
    setError(null);
    try {
      const datos = await listarMisResenas(token);
      setResenas(datos);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Error al consultar tus reseñas');
      }
    } finally {
      setCargando(false);
    }
  }, [token]);

  useEffect(() => {
    cargarResenas();
  }, [cargarResenas]);

  const obtenerInsigniaEstado = (estado: string) => {
    switch (estado) {
      case 'APPROVED':
        return <span className="badge bg-success">Aprobada (Pública)</span>;
      case 'PENDING':
        return <span className="badge bg-warning text-dark">Pendiente de Moderación</span>;
      case 'REJECTED':
        return <span className="badge bg-danger">Rechazada</span>;
      default:
        return <span className="badge bg-secondary">{estado}</span>;
    }
  };

  const formatearFecha = (cadenaIso: string) => {
    try {
      const fecha = new Date(cadenaIso);
      return fecha.toLocaleString('es-ES', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return cadenaIso;
    }
  };

  return (
    <div>
      <div className="bg-white p-4 rounded-3 shadow-sm mb-4">
        <div className="d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <h2 className="h4 fw-bold mb-1">Mis Reseñas Publicadas</h2>
            <p className="text-muted small mb-0">
              Historial de tus valoraciones y estado de moderación asignado.
            </p>
          </div>
          <button
            type="button"
            onClick={alIrACursos}
            className="btn btn-outline-primary btn-sm"
          >
            Explorar más cursos
          </button>
        </div>
      </div>

      {cargando && (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando tus reseñas...</span>
          </div>
        </div>
      )}

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {!cargando && !error && resenas.length === 0 && (
        <div className="bg-white p-5 rounded-3 text-center shadow-sm text-muted">
          <h5 className="text-dark">Aún no has publicado ninguna reseña</h5>
          <p className="small mb-3">
            Elige un curso del catálogo y comparte tu experiencia con otros estudiantes.
          </p>
          <button
            type="button"
            onClick={alIrACursos}
            className="btn btn-primary btn-sm"
          >
            Ver Catálogo de Cursos
          </button>
        </div>
      )}

      {!cargando && !error && resenas.length > 0 && (
        <div className="row g-3">
          {resenas.map((resena) => {
            const puedeEditar = resena.estado === 'PENDING' || resena.estado === 'REJECTED';

            return (
              <div key={resena.id} className="col-12">
                <div className="card shadow-sm border-0">
                  <div className="card-body p-4">
                    <div className="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                      <div>
                        <h5 className="card-title fw-bold text-dark mb-1">
                          {resena.tituloCurso}
                        </h5>
                        <div className="text-muted small">
                          Enviada el: {formatearFecha(resena.fechaCreacion)}
                          {resena.fechaActualizacion !== resena.fechaCreacion && (
                            <span className="ms-2 fst-italic">
                              (Actualizada: {formatearFecha(resena.fechaActualizacion)})
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="d-flex align-items-center gap-2">
                        {obtenerInsigniaEstado(resena.estado)}
                        <CalificacionEstrellas calificacion={resena.calificacion} soloLectura />
                      </div>
                    </div>

                    <p className="card-text text-secondary bg-light p-3 rounded-2 border">
                      {resena.comentario}
                    </p>

                    <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mt-3 pt-2 border-top">
                      <div className="small text-muted">
                        {resena.estado === 'APPROVED' && (
                          <span className="text-success">
                            Esta reseña ya es pública y no puede ser modificada en esta versión.
                          </span>
                        )}
                        {resena.estado === 'PENDING' && (
                          <span className="text-warning-emphasis">
                            Tu reseña está en cola de revisión por el equipo moderador.
                          </span>
                        )}
                        {resena.estado === 'REJECTED' && (
                          <span className="text-danger">
                            Tu reseña fue rechazada. Puedes editarla para enviarla nuevamente a revisión.
                          </span>
                        )}
                      </div>

                      {puedeEditar ? (
                        <button
                          type="button"
                          onClick={() => alEditarResena(resena)}
                          className="btn btn-outline-primary btn-sm"
                        >
                          Editar Reseña
                        </button>
                      ) : (
                        <button
                          type="button"
                          disabled
                          className="btn btn-outline-secondary btn-sm"
                          title="Las reseñas aprobadas no admiten cambios"
                        >
                          Edición Bloqueada
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
