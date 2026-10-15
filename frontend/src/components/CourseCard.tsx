import React from 'react';
import { Curso } from '../types';
import { CalificacionEstrellas } from './StarRating';

interface PropiedadesTarjetaCurso {
  curso: Curso;
  alSeleccionarCurso: (idCurso: number) => void;
}

export const TarjetaCurso: React.FC<PropiedadesTarjetaCurso> = ({
  curso,
  alSeleccionarCurso,
}) => {
  return (
    <div className="card h-100 shadow-sm border-0 transition-hover">
      <div className="card-body d-flex flex-column">
        <div className="d-flex justify-content-between align-items-start mb-2">
          <span className="badge bg-secondary-subtle text-secondary-emphasis rounded-pill">
            {curso.categoria}
          </span>
          <CalificacionEstrellas calificacion={curso.promedioCalificacion} soloLectura />
        </div>

        <h5 className="card-title fw-bold text-dark mb-2">{curso.titulo}</h5>
        <p className="card-text text-muted flex-grow-1 small line-clamp-3">
          {curso.descripcion}
        </p>

        <div className="pt-3 border-top d-flex justify-content-between align-items-center mt-auto">
          <small className="text-secondary">
            {curso.totalResenas === 1
              ? '1 reseña aprobada'
              : `${curso.totalResenas} reseñas aprobadas`}
          </small>
          <button
            type="button"
            onClick={() => alSeleccionarCurso(curso.id)}
            className="btn btn-outline-primary btn-sm"
          >
            Ver Detalle
          </button>
        </div>
      </div>
    </div>
  );
};
