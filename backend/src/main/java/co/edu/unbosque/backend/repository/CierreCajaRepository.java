package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.CierreCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {

    List<CierreCaja> findByFechaCierreBetweenOrderByFechaCierreDesc(LocalDateTime desde, LocalDateTime hasta);

    List<CierreCaja> findAllByOrderByFechaCierreDesc();
}
