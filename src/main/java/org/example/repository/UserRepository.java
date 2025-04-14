package org.example.repository;

import org.example.model.User;
import org.example.state.Status;
import org.hibernate.query.Query;
import org.example.util.HibernateUtil;

import java.util.List;

public class UserRepository {

    // Получение пользователя по chatId
    public User getUser(Long chatId) {
        return HibernateUtil.executeInTransaction(session -> {
            String hql = "FROM User u WHERE u.chatId = :chatId";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("chatId", chatId);
            return query.uniqueResult();  // Возвращает пользователя или null
        });
    }

    // Получение статуса пользователя по chatId
    public Status getStatus(Long chatId) {
        User user = getUser(chatId);  // Получаем пользователя
        return (user != null) ? user.getStatus() : Status.IDLE;  // Если пользователь найден, возвращаем его статус, иначе IDLE
    }

    // Сохранение или обновление пользователя
    public void saveOrUpdate(User user) {
        HibernateUtil.executeInTransaction(session -> {
            session.saveOrUpdate(user);  // Сохраняем или обновляем пользователя
            return null;
        });
    }

    // Сохранение статуса пользователя
    public void saveUserStatus(Long chatId, Status status) {
        HibernateUtil.executeInTransaction(session -> {
            User user = getUser(chatId);
            if (user != null) {
                user.setStatus(status);
                session.saveOrUpdate(user);  // Сохраняем изменения пользователя в базе
            }
            return null;
        });
    }

    // Сохранение имени покупателя
    public void saveBuyerName(Long chatId, String name) {
        HibernateUtil.executeInTransaction(session -> {
            User user = getUser(chatId);
            if (user != null && user.getCurrentDeal() != null) {
                user.getCurrentDeal().setBuyerName(name);
                session.saveOrUpdate(user);  // Сохраняем изменения
            }
            return null;
        });
    }

    // Сохранение суммы сделки
    public void saveTransactionAmount(Long chatId, double amount) {
        HibernateUtil.executeInTransaction(session -> {
            User user = getUser(chatId);
            if (user != null && user.getCurrentDeal() != null) {
                user.getCurrentDeal().setAmountTo(amount);
                session.saveOrUpdate(user);  // Сохраняем изменения
            }
            return null;
        });
    }

    // Сохранение курса обмена
    public void saveExchangeRate(Long chatId, double rate) {
        HibernateUtil.executeInTransaction(session -> {
            User user = getUser(chatId);
            if (user != null && user.getCurrentDeal() != null) {
                user.getCurrentDeal().setExchangeRate(rate);
                session.saveOrUpdate(user);  // Сохраняем изменения
            }
            return null;
        });
    }

    // Получить все пользователей (при необходимости)
    public List<User> getAllUsers() {
        return HibernateUtil.executeInTransaction(session -> {
            String hql = "FROM User";
            Query<User> query = session.createQuery(hql, User.class);
            return query.list();  // Возвращает список всех пользователей
        });
    }
}
