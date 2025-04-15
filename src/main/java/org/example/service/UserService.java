package org.example.service;

import org.example.model.Deal;
import org.example.model.DealType;
import org.example.model.Money;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.state.Status;
import org.example.util.HibernateUtil;
import org.hibernate.Session;

public class UserService {

    private static UserRepository userRepository = new UserRepository();

    // Конструктор, который инициализирует репозиторий
    public UserService(UserRepository userRepository) {
        UserService.userRepository = userRepository;
    }

    // Сохранение или обновление пользователя
    public static void save(User user) {
        userRepository.saveOrUpdate(user);  // Вызов метода репозитория для сохранения или обновления
    }

    // Пример получения пользователя по chatId
    public static User getUser(Long chatId) {
        return userRepository.getUser(chatId);
    }


    // Сохранение или обновление пользователя
    public static void saveOrUpdate(User user) {
        HibernateUtil.executeInTransaction(session -> {
            session.saveOrUpdate(user);  // Используем merge для обновления или вставки
            System.out.println("Обновили пользователя");
            return null;
        });
    }

    // Сохранение статуса пользователя
    public static void saveUserStatus(Long chatId, Status status) {
        HibernateUtil.executeInTransaction(session -> {
            User user = getUser(chatId);
            if (user != null) {
                user.setStatus(status);
                session.saveOrUpdate(user);  // Сохраняем изменения пользователя в базе
            }
            return null;
        });
    }

    public static void saveUserCurrentDeal(Long chatId, Deal deal) {
        HibernateUtil.executeInTransaction(session -> {
            User user = getUser(chatId);
            if (user != null) {
                user.setCurrentDeal(deal);
                session.saveOrUpdate(user);  // Сохраняем изменения пользователя в базе
            }
            return null;
        });
    }

    public static void addMessageToDel(Long chatId, Integer msgId) {
        HibernateUtil.executeInTransaction(session -> {
            User user = getUser(chatId);
            if (user != null) {
                user.addMessage(msgId);
                session.saveOrUpdate(user);  // Сохраняем изменения пользователя в базе
            }
            return null;
        });
    }

    public static void addMessageToEdit(Long chatId, Integer msgId) {
        HibernateUtil.executeInTransaction(session -> {
            User user = getUser(chatId);
            if (user != null) {
                user.setMessageToEdit(msgId);
                session.saveOrUpdate(user);  // Сохраняем изменения пользователя в базе
            }
            return null;
        });
    }

    // Сохранение имени покупателя
    public static void saveBuyerName(Long chatId, String name) {
        HibernateUtil.executeInTransaction(session -> {
            User user = getUser(chatId);
            if (user != null && user.getCurrentDeal() != null) {
                user.getCurrentDeal().setBuyerName(name);
                session.saveOrUpdate(user);  // Сохраняем изменения
            }
            return null;
        });
    }

    // Запуск новой сделки
    public static void startDeal(Long chatId, Money from, Money to, DealType type) {
        HibernateUtil.executeInTransaction(session -> {
            Deal deal = new Deal();
            deal.setMoneyFrom(from);
            deal.setMoneyTo(to);
            deal.setDealType(type);

            saveOrUpdateDeal(chatId, deal, session);
            return null;
        });
    }

    // Сохранение или обновление сделки
    private static void saveOrUpdateDeal(Long chatId, Deal deal, Session session) {
        session.saveOrUpdate(deal);  // Сохраняем или обновляем сделку
        User user = getUser(chatId);  // Получаем пользователя
        if (user != null) {
            user.setCurrentDeal(deal);  // Привязываем сделку к пользователю
            session.saveOrUpdate(user);  // Сохраняем изменения пользователя
        }
    }
}
