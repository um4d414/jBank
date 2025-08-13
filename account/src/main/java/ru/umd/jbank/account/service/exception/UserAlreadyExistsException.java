package ru.umd.jbank.account.service.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username) {
        super("Пользователь с логином '" + username + "' уже существует");
    }
}
