package com.jefferson.jsonview.repository;

import com.jefferson.jsonview.model.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {
}
