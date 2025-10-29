package com.zvonok.service.dto;

import lombok.Getter;

@Getter
public enum Permission {
    // Базовые разрешения
    NOTHING(0l),                     // Ничего
    VIEW_CHANNEL(1L << 0),           // Видеть канал
    SEND_MESSAGES(1L << 1),          // Отправлять сообщения
    READ_MESSAGE_HISTORY(1L << 2),   // Читать историю сообщений

    // Управление сообщениями
    MANAGE_MESSAGES(1L << 3),        // Управлять сообщениями (удалять чужие)
    EMBED_LINKS(1L << 4),          // Вставлять ссылки
    ATTACH_FILES(1L << 5),         // Прикреплять файлы
    USE_EXTERNAL_EMOJIS(1L << 6),  // Использовать внешние эмодзи

    // Голосовые каналы
    CONNECT(1L << 7),             // Подключаться к голосовым каналам
    SPEAK(1L << 8),               // Говорить в голосовых каналах
    MUTE_MEMBERS(1L << 9),        // Заглушать участников
    DEAFEN_MEMBERS(1L << 10),     // Оглушать участников
    MOVE_MEMBERS(1L << 11),       // Перемещать участников между каналами

    // Управление каналами и папками
    MANAGE_CHANNELS(1L << 12),    // Управлять каналами (создавать, редактировать, удалять)
    MANAGE_PERMISSIONS(1L << 13), // Управлять разрешениями канала

    // Управление сервером
    KICK_MEMBERS(1L << 14),      // Исключать участников
    BAN_MEMBERS(1L << 15),       // Банить участников
    MANAGE_ROLES(1L << 16),      // Управлять ролями
    MANAGE_SERVER(1L << 17),    // Управлять сервером
    CREATE_INVITE(1L << 18),    // Создавать приглашения

    // Администраторские права
    ADMINISTRATOR(1L << 19);    // Полные права администратора

    private final long value;

    Permission(Long value) {
        this.value = value;
    }

    // Проверить есть ли разрешение в битовой маске
    public static boolean hasPermission(Long permissions, Permission permission) {
        return (permissions & permission.getValue()) != 0;
    }

    // Добавить разрешение к битовой маске
    public static Long addPermission(Long permissions, Permission permission) {
        return permissions | permission.getValue();
    }

    // Удалить разрешение из битовой маски
    public static Long removePermission(Long permissions, Permission permission) {
        return permissions & ~permission.getValue();
    }
}