package org.example.util;

import org.example.model.Currency;
import org.example.model.Deal;
import org.example.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.function.Function;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(Currency.class)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Deal.class)
                    .buildSessionFactory();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to create SessionFactory object." + e);
        }
    }

    // Новый метод для работы с транзакциями
    public static <T> T executeInTransaction(Function<Session, T> action) {
        // Открываем новую сессию
        Session session = sessionFactory.openSession();  // Вместо getCurrentSession
        Transaction transaction = null;
        T result = null;

        try {
            // Начинаем транзакцию
            transaction = session.beginTransaction();

            // Выполняем переданное действие
            result = action.apply(session);

            // Завершаем транзакцию
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback(); // Откатываем транзакцию в случае ошибки
            e.printStackTrace();  // Логирование ошибки
        } finally {
            // Закрываем сессию
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

    public static void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
