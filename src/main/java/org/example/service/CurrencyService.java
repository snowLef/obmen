package org.example.service;

import org.example.model.Currency;
import org.example.model.Money;
import org.example.model.User;
import org.example.util.HibernateUtil;
import org.hibernate.query.Query;

import java.util.List;

public class CurrencyService {

    public static void save(Currency currency) {
        HibernateUtil.executeInTransaction(session -> {
            session.save(currency);
            return null;
        });
    }

    public static void saveOrUpdate(Currency currency) {
        HibernateUtil.executeInTransaction(session -> {
            session.saveOrUpdate(currency);
            return null;
        });
    }

    public static void updateBalance(Money money, double newBalance) {
        Currency currency = getCurrency(money);
        currency.setBalance(newBalance);
        saveOrUpdate(currency);
    }

    public static double getBalance(Money money) {
        Currency currency = getCurrency(money);
        return currency != null ? currency.getBalance() : 0.0;
    }

    public static Currency getCurrency(Money name) {
        return HibernateUtil.executeInTransaction(session -> {
            String hql = "FROM Currency c WHERE c.name = :name";
            Query<Currency> query = session.createQuery(hql, Currency.class);
            query.setParameter("name", name.name());

            return query.uniqueResult();
        });
    }

    public static List<Currency> getAllCurrency() {
        return HibernateUtil.executeInTransaction(session -> {
            String hql = "FROM Currency c";
            Query<Currency> query = session.createQuery(hql, Currency.class);

            return query.getResultList();
        });
    }

}
