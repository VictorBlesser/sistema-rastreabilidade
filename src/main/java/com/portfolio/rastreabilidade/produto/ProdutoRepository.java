package com.portfolio.rastreabilidade.produto;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);
}
