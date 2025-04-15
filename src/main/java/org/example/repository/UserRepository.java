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

    // Сохранение или обновление пользователя
    public void saveOrUpdate(User user) {
        HibernateUtil.executeInTransaction(session -> {
            session.saveOrUpdate(user);  // Сохраняем или обновляем пользователя
            return null;
        });
    }
}
