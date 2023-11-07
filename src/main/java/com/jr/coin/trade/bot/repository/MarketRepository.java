package com.jr.coin.trade.bot.repository;

import com.jr.coin.trade.bot.domain.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<Market, String> {

    Optional<Market> findByMarket(String market);
    Optional<Market> findByKoreanName(String market);

}
