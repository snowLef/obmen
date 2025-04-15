package org.example.service;

import org.example.model.Deal;

public class DealService {

    public static void save(Deal deal) {
        HibernateUtil.executeInTransaction(session -> {
            session.save(deal);
            return null;
        });
    }

    public static void saveOrUpdate(Deal deal) {
        HibernateUtil.executeInTransaction(session -> {
            session.saveOrUpdate(deal);
            return null;
        });
    }

}
