package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.handler.state.*;
import org.example.model.enums.Status;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class StateHandlersConfig {

    private final IdleStateHandler idleHandler;
    private final AwaitingBuyerNameHandler awaitingBuyerNameHandler;
    private final PlusMinusTypeHandler plusMinusTypeHandler;
    private final AwaitingDealAmountHandler awaitingDealAmountHandler;
    private final AwaitingCurrencyTypeHandler awaitingCurrencyTypeHandler;
    private final AwaitingExchangeRateHandler awaitingExchangeRateHandler;
    private final AwaitingApproveHandler awaitingApproveHandler;
    private final AwaitingCityNameHandler awaitingCityNameHandler;
    private final AwaitingCommentHandler awaitingCommentHandler;
    private final AwaitingEachCurrencyAmountHandlerFrom awaitingEachCurrencyAmountHandlerFrom;
    private final AwaitingEachCurrencyAmountHandlerTo awaitingEachCurrencyAmountHandlerTo;
    private final ChangeBalanceTypeHandler changeBalanceTypeHandler;

    @Bean
    public Map<Status, UserStateHandler> userStateHandlers() {
        Map<Status, UserStateHandler> handlers = new EnumMap<>(Status.class);
        handlers.put(Status.IDLE, idleHandler);
        handlers.put(Status.AWAITING_BUYER_NAME, awaitingBuyerNameHandler);
        handlers.put(Status.AWAITING_PLUS_MINUS_TYPE, plusMinusTypeHandler);
        handlers.put(Status.AWAITING_CHANGE_BALANCE_TYPE, changeBalanceTypeHandler);
        handlers.put(Status.AWAITING_DEAL_AMOUNT, awaitingDealAmountHandler);
        handlers.put(Status.AWAITING_CURRENCY_TYPE, awaitingCurrencyTypeHandler);
        handlers.put(Status.AWAITING_COMMENT, awaitingCommentHandler);
        handlers.put(Status.AWAITING_EXCHANGE_RATE_TYPE, awaitingCurrencyTypeHandler);
        handlers.put(Status.AWAITING_EXCHANGE_RATE, awaitingExchangeRateHandler);
        handlers.put(Status.AWAITING_APPROVE, awaitingApproveHandler);
        handlers.put(Status.AWAITING_CITY_NAME, awaitingCityNameHandler);
        handlers.put(Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_FROM, awaitingEachCurrencyAmountHandlerFrom);
        handlers.put(Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_TO, awaitingEachCurrencyAmountHandlerTo);
        return handlers;
    }
}
