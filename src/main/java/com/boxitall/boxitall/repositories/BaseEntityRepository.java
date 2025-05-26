package com.boxitall.boxitall.repositories;


import com.boxitall.boxitall.entities.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseEntityRepository<E extends BaseEntity, ID extends Serializable> extends JpaRepository<E, ID> {
}
