package com.lsgsma.transaction.repository;

import com.lsgsma.transaction.model.Transaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
