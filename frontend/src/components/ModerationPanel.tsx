import React, { useEffect, useState, useCallback } from 'react';
import { Resena, ReviewStatus } from '../types';
import { listarResenasModeracion, moderarResena } from '../services/api';
import { usarAutenticacion } from '../context/AuthContext';
import { CalificacionEstrellas } from './StarRating';

export const PanelModeracion: React.FC = () => {
  const { token, esModerador } = usarAutenticacion();
  const [resenas, setResenas] = useState<Resena[]>([]);
  const [cargando, setCargando] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [mensajeExito, setMensajeExito] = useState<string | null>(null);
  const [filtroEstado, setFiltroEstado] = useState<'PENDING' | 'TODAS' | 'APPROVED' | 'REJECTED'>('PENDING');
  const [procesandoId, setProcesandoId] = useState<number | null>(null);

  const cargarResenas = useCallback(async () => {
    if (!token) return;
    setCargando(true);
    setError(null);
    try {
      const estadoParam = filtroEstado === 'TODAS' ? undefined : (filtroEstado as ReviewStatus);
      const datos = await listarResenasModeracion(token, estadoParam);
      setResenas(datos);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Error al consultar reseñas de moderación');
      }
    } finally {
      setCargando(false);
    }
  }, [token, filtroEstado]);

  useEffect(() => {
    cargarResenas();
  }, [cargarResenas]);

  const manejarCambioEstado = async (idResena: number, nuevoEstado: ReviewStatus) => {
    if (!token) return;
    setProcesandoId(idResena);
    setError(null);
    setMensajeExito(null);
    try {
      await moderarResena(idResena, nuevoEstado, token);
      const accion = nuevoEstado === 'APPROVED' ? 'aprobada' : 'rechazada';
      setMensajeExito(`Reseña #${idResena} ha sido ${accion} con éxito.`);
      await cargarResenas();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Ocurrió un error al moderar la reseña');
      }
    } finally {
      setProcesandoId(null);
    }
  };

  const obtenerInsigniaEstado = (estado: string) => {
    switch (estado) {
      case 'APPROVED':
        return <span className="badge bg-success">APPROVED</span>;
      case 'PENDING':
        return <span className="badge bg-warning text-dark">PENDING</span>;
      case 'REJECTED':
        return <span className="badge bg-danger">REJECTED</span>;
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

  if (!esModerador) {
    return (
      <div className="alert alert-danger" role="alert">
        Acceso no autorizado. Este panel es exclusivo para usuarios con rol MODERATOR.
      </div>
    );
  }

  return (
    <div>
      <div className="bg-white p-4 rounded-3 shadow-sm mb-4">
        <div className="d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <h2 className="h4 fw-bold mb-1">Panel de Moderación de Reseñas</h2>
            <p className="text-muted small mb-0">
              Supervisa las opiniones enviadas por los usuarios antes de su publicación general.
            </p>
          </div>
          <button
            type="button"
            onClick={cargarResenas}
            className="btn btn-outline-secondary btn-sm"
          >
            Actualizar listado
          </button>
        </div>

        {/* Filtros de Pestaña */}
        <div className="btn-group mt-3" role="group">
          <button
            type="button"
            onClick={() => setFiltroEstado('PENDING')}
            className={`btn btn-sm ${
              filtroEstado === 'PENDING' ? 'btn-warning text-dark fw-bold' : 'btn-outline-secondary'
            }`}
          >
            Pendientes (PENDING)
          </button>
          <button
            type="button"
            onClick={() => setFiltroEstado('TODAS')}
            className={`btn btn-sm ${
              filtroEstado === 'TODAS' ? 'btn-dark fw-bold' : 'btn-outline-secondary'
            }`}
          >
            Historial Completo
          </button>
          <button
            type="button"
            onClick={() => setFiltroEstado('APPROVED')}
            className={`btn btn-sm ${
              filtroEstado === 'APPROVED' ? 'btn-success fw-bold' : 'btn-outline-secondary'
            }`}
          >
            Aprobadas (APPROVED)
          </button>
          <button
            type="button"
            onClick={() => setFiltroEstado('REJECTED')}
            className={`btn btn-sm ${
              filtroEstado === 'REJECTED' ? 'btn-danger fw-bold' : 'btn-outline-secondary'
            }`}
          >
            Rechazadas (REJECTED)
          </button>
        </div>
      </div>

      {mensajeExito && (
        <div className="alert alert-success alert-dismissible fade show" role="alert">
          {mensajeExito}
          <button
            type="button"
            className="btn-close"
            onClick={() => setMensajeExito(null)}
            aria-label="Cerrar"
          ></button>
        </div>
      )}

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {cargando && (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando reseñas...</span>
          </div>
        </div>
      )}

      {!cargando && resenas.length === 0 && (
        <div className="bg-white p-5 rounded-3 text-center shadow-sm text-muted">
          <h5 className="text-dark">No hay reseñas para este filtro</h5>
          <p className="small mb-0">
            {filtroEstado === 'PENDING'
              ? 'Todas las reseñas recibidas ya han sido moderadas.'
              : 'No se encontraron registros bajo el criterio seleccionado.'}
          </p>
        </div>
      )}

      {!cargando && resenas.length > 0 && (
        <div className="row g-3">
          {resenas.map((resena) => {
            const esPendiente = resena.estado === 'PENDING';
            const enProceso = procesandoId === resena.id;

            return (
              <div key={resena.id} className="col-12">
                <div
                  className={`card shadow-sm border-0 ${
                    esPendiente ? 'border-start border-warning border-4' : ''
                  }`}
                >
                  <div className="card-body p-4">
                    <div className="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                      <div>
                        <span className="badge bg-secondary-subtle text-dark me-2">
                          ID: #{resena.id}
                        </span>
                        <span className="fw-bold text-primary">{resena.tituloCurso}</span>
                        <div className="small text-muted mt-1">
                          Autor: <strong>{resena.nombreUsuario}</strong> ({resena.correoUsuario})
                          <span className="mx-2">•</span>
                          Fecha: {formatearFecha(resena.fechaCreacion)}
                        </div>
                      </div>

                      <div className="d-flex align-items-center gap-2">
                        {obtenerInsigniaEstado(resena.estado)}
                        <CalificacionEstrellas calificacion={resena.calificacion} soloLectura />
                      </div>
                    </div>

                    <div className="bg-light p-3 rounded-2 border my-2 text-dark">
                      "{resena.comentario}"
                    </div>

                    <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 pt-2 border-top">
                      <small className="text-muted">
                        Última actualización: {formatearFecha(resena.fechaActualizacion)}
                      </small>

                      {esPendiente ? (
                        <div className="d-flex gap-2">
                          <button
                            type="button"
                            disabled={enProceso}
                            onClick={() => manejarCambioEstado(resena.id, 'APPROVED')}
                            className="btn btn-success btn-sm px-3"
                          >
                            {enProceso ? 'Procesando...' : 'Aprobar Reseña'}
                          </button>
                          <button
                            type="button"
                            disabled={enProceso}
                            onClick={() => manejarCambioEstado(resena.id, 'REJECTED')}
                            className="btn btn-outline-danger btn-sm px-3"
                          >
                            {enProceso ? 'Procesando...' : 'Rechazar Reseña'}
                          </button>
                        </div>
                      ) : (
                        <span className="small text-muted fst-italic">
                          Esta reseña ya fue moderada (estado fijo).
                        </span>
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
