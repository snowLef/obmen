package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.model.Deal;
import org.example.model.DealType;
import org.example.model.Money;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.state.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import java.util.List;

@Service
public class UserService {

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
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
            user.setStatus(status);
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

    public void startDeal(Long chatId, Money from, Money to, DealType type) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            Deal deal = new Deal();
            deal.setMoneyFrom(from);
            deal.setMoneyTo(to);
            deal.setDealType(type);
            user.setCurrentDeal(deal);
            userRepository.save(user);
        });
    }

    public List<Integer> getMessageIdsToDelete(Long chatId) {
        User user = getUser(chatId);
        return user.getMessages();
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

}
