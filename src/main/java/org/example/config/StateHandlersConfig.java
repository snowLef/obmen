package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.handler.state.*;
import org.example.model.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class StateHandlersConfig {

//    @Autowired
//    private IdleStateHandler idleStateHandler;
//    @Autowired
//    private ChangeBalanceTypeHandler changeBalanceTypeHandler;
    private final IdleStateHandler idleHandler;
    private final AwaitingBuyerNameHandler awaitingBuyerNameHandler;
    private final ChangeBalanceTypeHandler changeBalanceTypeHandler;
//    private final AwaitingFirstCurrencyHandler awaitingFirstCurrencyHandler;
    private final AwaitingSecondCurrencyHandler awaitingSecondCurrencyHandler;
    private final AwaitingSelectAmountHandler awaitingSelectAmountHandler;
    private final AwaitingDealAmountHandler awaitingDealAmountHandler;
    private final AwaitingCurrencyTypeHandler awaitingCurrencyTypeHandler;
    private final AwaitingExchangeRateHandler awaitingExchangeRateHandler;
    private final AwaitingApproveHandler awaitingApproveHandler;
//    private final AwaitingCustomApproveHandler awaitingCustomApproveHandler;

    @Bean
    public Map<Status, UserStateHandler> userStateHandlers() {
        Map<Status, UserStateHandler> handlers = new EnumMap<>(Status.class);
        handlers.put(Status.IDLE, idleHandler);
        handlers.put(Status.AWAITING_BUYER_NAME, awaitingBuyerNameHandler);
        handlers.put(Status.AWAITING_CHANGE_BALANCE_TYPE, changeBalanceTypeHandler);
        //handlers.put(Status.AWAITING_FIRST_CURRENCY, awaitingFirstCurrencyHandler);
        handlers.put(Status.AWAITING_SECOND_CURRENCY, awaitingSecondCurrencyHandler);
        handlers.put(Status.AWAITING_SELECT_AMOUNT, awaitingSelectAmountHandler);
        handlers.put(Status.AWAITING_DEAL_AMOUNT, awaitingDealAmountHandler);
        handlers.put(Status.AWAITING_CURRENCY_TYPE, awaitingCurrencyTypeHandler);
        handlers.put(Status.AWAITING_EXCHANGE_RATE_TYPE, awaitingCurrencyTypeHandler);
        //handlers.put(Status.AWAITING_CUSTOM_APPROVE, awaitingCustomApproveHandler);
        handlers.put(Status.AWAITING_EXCHANGE_RATE, awaitingExchangeRateHandler);
        handlers.put(Status.AWAITING_APPROVE, awaitingApproveHandler);
        return handlers;
    }
}
