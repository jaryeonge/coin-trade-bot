package com.jr.coin.trade.bot.exception;

import com.jr.coin.trade.bot.domain.response.UpbitErrorResponseDto;
import com.jr.coin.trade.bot.util.ErrorCode;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class UpbitApiException extends RuntimeException {

    private ErrorCode errorCode;
    private String extraExceptionMsg;

    public static UpbitApiException create(ErrorCode errorCode) {
        return UpbitApiException.builder().errorCode(errorCode).extraExceptionMsg("").build();
    }

    public static UpbitApiException create(UpbitErrorResponseDto upbitErrorResponseDto) {
        ErrorCode errorCode = switch (upbitErrorResponseDto.getError().getName()) {
            case "create_ask_error", "create_bid_error" -> ErrorCode.UPBIT_CREATE_TRADE_ERROR;
            case "insufficient_funds_ask", "insufficient_funds_bid" -> ErrorCode.UPBIT_INSUFFICIENT_FUNDS_ERROR;
            case "under_min_total_ask", "under_min_total_bid" -> ErrorCode.UPBIT_UNDER_MIN_TOTAL_ERROR;
            case "withdraw_address_not_registerd" -> ErrorCode.UPBIT_WITHDRAW_ADDRESS_NOT_REGISTERED_ERROR;
            case "validation_error" -> ErrorCode.UPBIT_VALIDATION_ERROR;
            case "invalid_query_payload" -> ErrorCode.UPBIT_INVALID_QUERY_PAYLOAD_ERROR;
            case "jwt_verification" -> ErrorCode.UPBIT_JWT_VERIFICATION_ERROR;
            case "expired_access_key" -> ErrorCode.UPBIT_EXPIRED_ACCESS_KEY_ERROR;
            case "nonce_used" -> ErrorCode.UPBIT_NONCE_USED_ERROR;
            case "no_authorization_i_p" -> ErrorCode.UPBIT_NO_AUTHORIZATION_IP_ERROR;
            case "out_of_scope" -> ErrorCode.UPBIT_OUT_OF_SCOPE_ERROR;
            default -> ErrorCode.UPBIT_ERROR;
        };

        return UpbitApiException.builder()
                .errorCode(errorCode)
                .extraExceptionMsg(upbitErrorResponseDto.getError().getMessage())
                .build();
    }

    public String getFullMessage() {
        if (StringUtils.isEmpty(extraExceptionMsg) == true) {
            extraExceptionMsg = StringUtils.EMPTY;
        }
        return errorCode.getMsg() + " " + extraExceptionMsg;
    }
}
