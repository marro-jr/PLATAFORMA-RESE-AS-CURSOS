import React, { useEffect, useState, useCallback } from 'react';
import { Curso, Resena } from '../types';
import { listarResenasAprobadasPorCurso, obtenerCursoPorId } from '../services/api';
import { CalificacionEstrellas } from './StarRating';
import { usarAutenticacion } from '../context/AuthContext';

interface PropiedadesDetalleCurso {
  idCurso: number;
  alVolver: () => void;
  alAbrirNuevaResena: (curso: Curso) => void;
  alAbrirLogin: () => void;
}

export const DetalleCurso: React.FC<PropiedadesDetalleCurso> = ({
  idCurso,
  alVolver,
  alAbrirNuevaResena,
  alAbrirLogin,
}) => {
  const { estaAutenticado, esModerador } = usarAutenticacion();
  const [curso, setCurso] = useState<Curso | null>(null);
  const [resenas, setResenas] = useState<Resena[]>([]);
  const [cargando, setCargando] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const cargarDatos = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const [datosCurso, datosResenas] = await Promise.all([
        obtenerCursoPorId(idCurso),
        listarResenasAprobadasPorCurso(idCurso),
      ]);
      setCurso(datosCurso);
      setResenas(datosResenas);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('No se pudo cargar la información del curso');
      }
    } finally {
      setCargando(false);
    }
  }, [idCurso]);

  useEffect(() => {
    cargarDatos();
  }, [cargarDatos]);

  const formatearFecha = (cadenaIso: string) => {
    try {
      const fecha = new Date(cadenaIso);
      return fecha.toLocaleDateString('es-ES', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      });
    } catch {
      return cadenaIso;
    }
  };

  return (
    <div>
      <button
        type="button"
        onClick={alVolver}
        className="btn btn-outline-secondary btn-sm mb-3 d-inline-flex align-items-center gap-1"
      >
        ← Volver al catálogo
      </button>

      {cargando && (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando curso...</span>
          </div>
        </div>
      )}

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {!cargando && curso && (
        <>
          {/* Encabezado del Curso */}
          <div className="card shadow-sm border-0 mb-4 bg-white">
            <div className="card-body p-4">
              <div className="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                <span className="badge bg-primary-subtle text-primary-emphasis px-3 py-2 rounded-pill">
                  {curso.categoria}
                </span>
                <CalificacionEstrellas calificacion={curso.promedioCalificacion} soloLectura />
              </div>

              <h1 className="h3 fw-bold text-dark mb-3">{curso.titulo}</h1>
              <p className="lead text-secondary fs-6 mb-4">{curso.descripcion}</p>

              <div className="d-flex justify-content-between align-items-center flex-wrap gap-3 pt-3 border-top">
                <div className="small text-muted">
                  <strong>{curso.totalResenas}</strong> reseñas verificadas por moderación
                </div>

                <div>
                  {!estaAutenticado && (
                    <button
                      type="button"
                      onClick={alAbrirLogin}
                      className="btn btn-primary"
                    >
                      Inicia sesión para opinar
                    </button>
                  )}

                  {estaAutenticado && !esModerador && (
                    <button
                      type="button"
                      onClick={() => alAbrirNuevaResena(curso)}
                      className="btn btn-primary"
                    >
                      + Publicar Reseña
                    </button>
                  )}

                  {estaAutenticado && esModerador && (
                    <span className="badge bg-warning text-dark py-2 px-3">
                      Modo Moderador: Gestiona las opiniones en el panel de moderación
                    </span>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Sección de Reseñas Aprobadas */}
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2 className="h4 fw-bold mb-0">Reseñas Aprobadas de la Comunidad</h2>
            <span className="badge bg-success-subtle text-success-emphasis border border-success-subtle">
              Solo estado APPROVED
            </span>
          </div>

          {resenas.length === 0 ? (
            <div className="bg-white p-4 rounded-3 text-center shadow-sm text-muted">
              <p className="mb-1 fw-semibold">Aún no hay reseñas aprobadas para este curso.</p>
              <p className="small mb-0">
                Las nuevas opiniones enviadas son revisadas previamente por el equipo de moderación.
              </p>
            </div>
          ) : (
            <div className="row g-3">
              {resenas.map((resena) => (
                <div key={resena.id} className="col-12">
                  <div className="card border-0 shadow-sm">
                    <div className="card-body">
                      <div className="d-flex justify-content-between align-items-center mb-2">
                        <div className="d-flex align-items-center gap-2">
                          <div
                            className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold small"
                            style={{ width: '34px', height: '34px' }}
                          >
                            {resena.nombreUsuario.charAt(0).toUpperCase()}
                          </div>
                          <div>
                            <div className="fw-semibold text-dark">{resena.nombreUsuario}</div>
                            <div className="text-muted small">
                              {formatearFecha(resena.fechaCreacion)}
                            </div>
                          </div>
                        </div>
                        <CalificacionEstrellas calificacion={resena.calificacion} soloLectura />
                      </div>
                      <p className="card-text text-secondary mb-0">{resena.comentario}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
};
