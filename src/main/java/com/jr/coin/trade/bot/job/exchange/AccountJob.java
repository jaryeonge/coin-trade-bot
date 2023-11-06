package com.jr.coin.trade.bot.job.exchange;

import com.jr.coin.trade.bot.domain.entity.Account;
import com.jr.coin.trade.bot.domain.response.UpbitAccountResponseDto;
import com.jr.coin.trade.bot.helper.UpbitApiClient;
import com.jr.coin.trade.bot.repository.AccountRepository;
import com.jr.coin.trade.bot.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class AccountJob {

    private final UpbitApiClient upbitApiClient;
    private final AccountRepository accountRepository;

    public AccountJob(UpbitApiClient upbitApiClient, AccountRepository accountRepository) {
        this.upbitApiClient = upbitApiClient;
        this.accountRepository = accountRepository;
    }

    public List<UpbitAccountResponseDto> getAccount() {
        HashMap<String, String> params = new HashMap<>();
        ParameterizedTypeReference<List<UpbitAccountResponseDto>> responseType = new ParameterizedTypeReference<>() {};

        return upbitApiClient.requestGetListToExchange(responseType, "/accounts", params);
    }

    public void saveAccount(List<UpbitAccountResponseDto> upbitAccountResponseDtoList) {
        upbitAccountResponseDtoList
                .forEach(upbitAccountResponseDto -> {
                    Optional<Account> optionalAccount = accountRepository.findByCurrency(upbitAccountResponseDto.getCurrency());
                    Account account;
                    if (optionalAccount.isEmpty()) {
                        account = new Account();
                    } else {
                        account = optionalAccount.get();
                    }

                    account.setCurrency(upbitAccountResponseDto.getCurrency());
                    account.setBalance(upbitAccountResponseDto.getBalance());
                    account.setLocked(upbitAccountResponseDto.getLocked());
                    account.setAvgBuyPrice(upbitAccountResponseDto.getAvg_buy_price());
                    account.setAvgBuyPriceModified(upbitAccountResponseDto.getAvg_buy_price_modified() ? Constants.TRUE_TEXT : Constants.FALSE_TEXT);
                    account.setUnitCurrency(upbitAccountResponseDto.getUnit_currency());

                    accountRepository.save(account);
                });
    }

}
