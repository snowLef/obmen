package org.example.repository;

import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByChatId(Long chatId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.messages WHERE u.chatId = :chatId")
    Optional<User> findByChatIdWithMessages(@Param("chatId") Long chatId);

}
