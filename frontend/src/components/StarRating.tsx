import React from 'react';

interface PropiedadesCalificacionEstrellas {
  calificacion: number;
  alCambiar?: (nuevaCalificacion: number) => void;
  soloLectura?: boolean;
}

export const CalificacionEstrellas: React.FC<PropiedadesCalificacionEstrellas> = ({
  calificacion,
  alCambiar,
  soloLectura = false,
}) => {
  const estrellas = [1, 2, 3, 4, 5];

  return (
    <div className="d-inline-flex align-items-center gap-1">
      {estrellas.map((estrella) => {
        const estaActiva = estrella <= Math.round(calificacion);
        return (
          <button
            key={estrella}
            type="button"
            disabled={soloLectura}
            onClick={() => alCambiar && alCambiar(estrella)}
            className="btn btn-link p-0 text-decoration-none border-0"
            style={{
              cursor: soloLectura ? 'default' : 'pointer',
              fontSize: '1.25rem',
              color: estaActiva ? '#ffc107' : '#e0e0e0',
              lineHeight: 1,
            }}
            title={soloLectura ? `${calificacion} de 5 estrellas` : `${estrella} estrellas`}
          >
            ★
          </button>
        );
      })}
      {soloLectura && (
        <span className="ms-1 small fw-semibold text-secondary">
          {calificacion > 0 ? calificacion.toFixed(1) : 'Sin reseñas'}
        </span>
      )}
    </div>
  );
};
