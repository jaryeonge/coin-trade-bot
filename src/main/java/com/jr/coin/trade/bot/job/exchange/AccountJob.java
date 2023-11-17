package com.jr.coin.trade.bot.job.exchange;

import com.jr.coin.trade.bot.domain.entity.Account;
import com.jr.coin.trade.bot.domain.response.UpbitAccountResponseDto;
import com.jr.coin.trade.bot.helper.UpbitApiClient;
import com.jr.coin.trade.bot.repository.AccountRepository;
import com.jr.coin.trade.bot.util.Constants;
import com.jr.coin.trade.bot.util.JobUtils;
import com.jr.coin.trade.bot.util.ScheduleCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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

    public Tasklet getAccountTasklet() {
        return (stepContribution, chunkContext) -> {
            if (JobUtils.checkSchedule(ScheduleCode.MINUTE)) {
                List<UpbitAccountResponseDto> responseDto = getAccount();
                saveAccount(responseDto);
            }
            return RepeatStatus.FINISHED;
        };
    }

    private List<UpbitAccountResponseDto> getAccount() {
        HashMap<String, String> params = new HashMap<>();
        ParameterizedTypeReference<List<UpbitAccountResponseDto>> responseType = new ParameterizedTypeReference<>() {};
        Mono<List<UpbitAccountResponseDto>> result = upbitApiClient.requestGetToExchange(responseType, "/accounts", params);

        return result.block();
    }

    private void saveAccount(List<UpbitAccountResponseDto> upbitAccountResponseDtoList) {
        upbitAccountResponseDtoList
                .forEach(upbitAccountResponseDto -> {
                    Optional<Account> optionalAccount = accountRepository.findByCurrency(upbitAccountResponseDto.getCurrency());
                    Account account;
                    if (optionalAccount.isEmpty()) {
                        account = new Account();
                        account.setCurrency(upbitAccountResponseDto.getCurrency());
                    } else {
                        account = optionalAccount.get();
                    }

                    account.setBalance(upbitAccountResponseDto.getBalance());
                    account.setLocked(upbitAccountResponseDto.getLocked());
                    account.setAvgBuyPrice(upbitAccountResponseDto.getAvg_buy_price());
                    account.setAvgBuyPriceModified(upbitAccountResponseDto.getAvg_buy_price_modified() ? Constants.TRUE_TEXT : Constants.FALSE_TEXT);
                    account.setUnitCurrency(upbitAccountResponseDto.getUnit_currency());

                    accountRepository.save(account);
                });
    }

}
