package com.jr.coin.trade.bot.repository;

import com.jr.coin.trade.bot.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByCurrency(String currency);

}
