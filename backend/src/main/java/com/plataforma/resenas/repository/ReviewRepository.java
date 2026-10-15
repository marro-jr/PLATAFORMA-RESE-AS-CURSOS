package com.plataforma.resenas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.plataforma.resenas.entity.Review;
import com.plataforma.resenas.entity.ReviewStatus;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCourseIdAndStatusOrderByCreatedAtDesc(Long idCurso, ReviewStatus estado);

    List<Review> findByUserEmailOrderByCreatedAtDesc(String correoUsuario);

    List<Review> findByStatusOrderByCreatedAtAsc(ReviewStatus estado);

    List<Review> findAllByOrderByCreatedAtDesc();

    boolean existsByUserIdAndCourseId(Long idUsuario, Long idCurso);

    Optional<Review> findByUserIdAndCourseId(Long idUsuario, Long idCurso);
}
