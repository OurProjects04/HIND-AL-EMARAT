package com.hindalemarat.repository;

import com.hindalemarat.entity.CustomerOrder;
import com.hindalemarat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByUserOrderByCreatedAtDesc(User user);
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
    Optional<CustomerOrder> findByOrderNumber(String orderNumber);
    long countByOrderStatus(String orderStatus);
}
