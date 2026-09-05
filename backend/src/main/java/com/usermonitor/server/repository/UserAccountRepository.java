package com.usermonitor.server.repository;
import com.usermonitor.server.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface UserAccountRepository extends JpaRepository<UserAccount,Long>{ Optional<UserAccount> findByUsernameIgnoreCase(String username); Optional<UserAccount> findByAccountId(String id); long countByDeviceCode(String deviceCode); List<UserAccount> findByUsernameContainingIgnoreCaseOrAccountIdContainingIgnoreCase(String username,String accountId); List<UserAccount> findByDeviceCodeContainingIgnoreCase(String deviceCode); }