import React, { useState, useEffect } from 'react';
import { Curso, Resena } from '../types';
import { crearResena, actualizarResenaPropia } from '../services/api';
import { usarAutenticacion } from '../context/AuthContext';
import { CalificacionEstrellas } from './StarRating';

interface PropiedadesModalResena {
  visible: boolean;
  alCerrar: () => void;
  curso: Curso | null;
  resenaParaEditar: Resena | null;
  alGuardarExitoso: () => void;
}

export const ModalResena: React.FC<PropiedadesModalResena> = ({
  visible,
  alCerrar,
  curso,
  resenaParaEditar,
  alGuardarExitoso,
}) => {
  const { token } = usarAutenticacion();
  const [calificacion, setCalificacion] = useState<number>(5);
  const [comentario, setComentario] = useState<string>('');
  const [cargando, setCargando] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const esEdicion = resenaParaEditar !== null;

  useEffect(() => {
    if (resenaParaEditar) {
      setCalificacion(resenaParaEditar.calificacion);
      setComentario(resenaParaEditar.comentario);
    } else {
      setCalificacion(5);
      setComentario('');
    }
    setError(null);
  }, [resenaParaEditar, visible]);

  if (!visible) return null;

  const manejarEnvio = async (evento: React.FormEvent) => {
    evento.preventDefault();
    if (!token) {
      setError('Debes iniciar sesión para publicar o editar una reseña');
      return;
    }

    if (calificacion < 1 || calificacion > 5) {
      setError('La calificación debe estar entre 1 y 5 estrellas');
      return;
    }

    if (!comentario.trim()) {
      setError('El comentario es obligatorio');
      return;
    }

    setCargando(true);
    setError(null);

    try {
      if (esEdicion && resenaParaEditar) {
        await actualizarResenaPropia(
          resenaParaEditar.id,
          { calificacion, comentario: comentario.trim() },
          token
        );
      } else if (curso) {
        await crearResena(
          curso.id,
          { calificacion, comentario: comentario.trim() },
          token
        );
      }

      alGuardarExitoso();
      alCerrar();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Ocurrió un error al guardar la reseña');
      }
    } finally {
      setCargando(false);
    }
  };

  const tituloCurso = resenaParaEditar ? resenaParaEditar.tituloCurso : curso?.titulo;

  return (
    <div
      className="modal fade show d-block"
      tabIndex={-1}
      style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}
    >
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 shadow">
          <div className="modal-header">
            <h5 className="modal-title fw-bold">
              {esEdicion ? 'Editar Reseña' : 'Publicar Reseña'}
            </h5>
            <button
              type="button"
              className="btn-close"
              onClick={alCerrar}
              disabled={cargando}
              aria-label="Cerrar"
            ></button>
          </div>

          <form onSubmit={manejarEnvio}>
            <div className="modal-body">
              {tituloCurso && (
                <div className="mb-3 text-muted small">
                  Curso: <strong className="text-dark">{tituloCurso}</strong>
                </div>
              )}

              {error && (
                <div className="alert alert-danger py-2 small" role="alert">
                  {error}
                </div>
              )}

              {esEdicion && resenaParaEditar?.estado === 'REJECTED' && (
                <div className="alert alert-warning py-2 small" role="alert">
                  Al editar esta reseña rechazada, regresará automáticamente a estado PENDING para ser re-evaluada por un moderador.
                </div>
              )}

              <div className="mb-3">
                <label className="form-label fw-semibold small">Calificación (1 a 5 estrellas)</label>
                <div>
                  <CalificacionEstrellas
                    calificacion={calificacion}
                    alCambiar={(nueva) => setCalificacion(nueva)}
                  />
                </div>
              </div>

              <div className="mb-3">
                <label htmlFor="comentario" className="form-label fw-semibold small">
                  Comentario u opinión
                </label>
                <textarea
                  id="comentario"
                  rows={4}
                  className="form-control"
                  placeholder="Describe los puntos fuertes del curso, nivel de exigencia, claridad de las explicaciones..."
                  value={comentario}
                  maxLength={1500}
                  onChange={(e) => setComentario(e.target.value)}
                  required
                ></textarea>
                <div className="form-text text-end small">
                  {comentario.length} / 1500 caracteres
                </div>
              </div>

              <div className="alert alert-light border small text-muted mb-0">
                Las reseñas enviadas pasan por un proceso de moderación antes de hacerse públicas.
              </div>
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-outline-secondary btn-sm"
                onClick={alCerrar}
                disabled={cargando}
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="btn btn-primary btn-sm px-4"
                disabled={cargando}
              >
                {cargando ? 'Guardando...' : esEdicion ? 'Actualizar Reseña' : 'Publicar Reseña'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
