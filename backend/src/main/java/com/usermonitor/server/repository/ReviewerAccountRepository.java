package com.usermonitor.server.repository;
import com.usermonitor.server.model.ReviewerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ReviewerAccountRepository extends JpaRepository<ReviewerAccount,Long>{Optional<ReviewerAccount> findByUsername(String username);}