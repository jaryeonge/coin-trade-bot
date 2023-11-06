package com.jr.coin.trade.bot.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ErrorCode {
    SUCCESS("0000", "성공"),

    UPBIT_CREATE_TRADE_ERROR("5000", "Upbit: 주문 요청 정보 에러"),
    UPBIT_INSUFFICIENT_FUNDS_ERROR("5001", "Upbit: 잔고 부족 에러"),
    UPBIT_UNDER_MIN_TOTAL_ERROR("5002", "Upbit: 최소주문금액 미만 에러"),
    UPBIT_WITHDRAW_ADDRESS_NOT_REGISTERED_ERROR("5003", "Upbit: 허용되지 않은 출금 주소 에러"),
    UPBIT_VALIDATION_ERROR("5004", "Upbit: 잘못된 API 요청 에러"),

    UPBIT_INVALID_QUERY_PAYLOAD_ERROR("6000", "Upbit: JWT 헤더 페이로드 에러"),
    UPBIT_JWT_VERIFICATION_ERROR("6001", "Upbit: JWT 헤더 검증 실패 에러"),
    UPBIT_EXPIRED_ACCESS_KEY_ERROR("6002", "Upbit: API 키 만료 에러"),
    UPBIT_NONCE_USED_ERROR("6003", "Upbit: 이미 요청된 nonce 값 사용 에러"),
    UPBIT_NO_AUTHORIZATION_IP_ERROR("6004", "Upbit: 허용되지 않은 IP 에러"),
    UPBIT_OUT_OF_SCOPE_ERROR("6005", "Upbit: 허용되지 않은 기능 에러"),
    UPBIT_ERROR("6999", "Upbit 에러"),

    INTERNAL_ERROR("9999", "내부 서버 에러")
    ;

    public String code;
    public String msg;
}
