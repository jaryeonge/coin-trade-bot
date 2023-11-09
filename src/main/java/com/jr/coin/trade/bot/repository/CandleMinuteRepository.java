package com.jr.coin.trade.bot.repository;

import com.jr.coin.trade.bot.domain.entity.CandleMinute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandleMinuteRepository extends JpaRepository<CandleMinute, String> {

    Optional<CandleMinute> findById(String market);

    Page<CandleMinute> findByMarketOrderByCandleDateTimeKstDesc(String market, Pageable pageable);

}
