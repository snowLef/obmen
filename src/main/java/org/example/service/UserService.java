package org.example.service;

import jakarta.transaction.Transactional;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.enums.BalanceType;
import org.example.model.enums.DealType;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.model.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Service
public class UserService {

    private UserRepository userRepository;
    private DealService dealService;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void resetUserState(User user) {
        user.pushStatus(Status.IDLE);
        user.setMessages(null);
        user.setMessageToEdit(null);
        user.setCurrentDeal(null);
        save(user);
    }

    public User getUser(Long chatId) {
        return userRepository.findByChatId(chatId).orElse(null);
    }

    public User getOrCreate(Long chatId) {
        return userRepository.findByChatId(chatId)
                .orElseGet(() -> userRepository.save(new User(chatId, Status.IDLE)));
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void saveUserStatus(Long chatId, Status status) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            user.pushStatus(status);
            userRepository.save(user);
        });
    }

    public void saveUserCurrentDeal(Long chatId, Deal deal) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            user.setCurrentDeal(deal);
            userRepository.save(user);
        });
    }

    public void addMessageToEdit(Long chatId, Integer msgId) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            user.setMessageToEdit(msgId);
            userRepository.save(user);
        });
    }

    public void saveBuyerName(Long chatId, String name) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            if (user.getCurrentDeal() != null) {
                user.getCurrentDeal().setBuyerName(name);
                userRepository.save(user);
            }
        });
    }

    public void saveCityName(Long chatId, String cityName) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            if (user.getCurrentDeal() != null) {
                user.getCurrentDeal().setCityFromTo(cityName);
                userRepository.save(user);
            }
        });
    }

    public void startDeal(Long chatId, CurrencyAmount from, CurrencyAmount to, DealType type, Message message) {
        if (from == null && to == null) {
            userRepository.findByChatId(chatId).ifPresent(user -> {
                Deal deal = new Deal();
                deal.setDealType(type);
                deal.setBalanceTypeFrom(BalanceType.OWN);
                deal.setBalanceTypeTo(BalanceType.OWN);
                deal.setCreatedBy("%s %s %s".formatted(message.getFrom().getFirstName(), message.getFrom().getLastName(), message.getFrom().getUserName()));
                user.setCurrentDeal(deal);
                userRepository.save(user);
            });
        } else {
            userRepository.findByChatId(chatId).ifPresent(user -> {
                Deal deal = new Deal();
                deal.setMoneyFrom(List.of(from));
                deal.setMoneyTo(List.of(to));
                deal.setCreatedBy("%s %s %s".formatted(message.getFrom().getFirstName(), message.getFrom().getLastName(), message.getFrom().getUserName()));
                deal.setDealType(type);
                deal.setBalanceTypeFrom(BalanceType.OWN);
                deal.setBalanceTypeTo(BalanceType.OWN);
                user.setCurrentDeal(deal);
                userRepository.save(user);
            });
        }
    }

    @Transactional
    public void addMessageToDel(Long chatId, Integer msgId) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            user.addMessage(msgId);
        });
    }

    public List<Integer> getMessageIdsToDeleteWithInit(Long chatId) {
        return userRepository.findByChatIdWithMessages(chatId)
                .map(User::getMessages)
                .orElse(List.of());
    }

    @Autowired
    public void setDealService(DealService dealService) {
        this.dealService = dealService;
    }
}
