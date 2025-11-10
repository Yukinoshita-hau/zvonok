package com.zvonok.exception_handler.enumeration;

import lombok.Getter;

@Getter
public enum BusinessRuleMessage {
    BUSINESS_CANNOT_BAN_SELF_MESSAGE("You cannot ban yourself"),
    BUSINESS_CANNOT_BAN_SERVER_OWNER_MESSAGE("You cannot ban the server owner"),
    BUSINESS_SERVER_MEMBER_LIMIT_REACHED_MESSAGE("Maximum number of server members reached"),
    BUSINESS_CANNOT_KICK_SERVER_OWNER_MESSAGE("You cannot kick the server owner"),
    BUSINESS_CANNOT_KICK_SELF_MESSAGE("You cannot kick yourself from the server"),
    BUSINESS_CANNOT_DISABLE_EVERYONE_ROLE_MESSAGE("Cannot disable the @everyone role"),
    BUSINESS_CANNOT_DELETE_EVERYONE_ROLE_MESSAGE("Cannot delete the @everyone role"),
    BUSINESS_CANNOT_EDIT_DELETED_MESSAGE("Cannot edit a deleted message"),
    BUSINESS_AUTHENTICATED_PRINCIPAL_REQUIRED_MESSAGE("Authenticated principal is required to send messages"),
    BUSINESS_MESSAGE_TARGET_VALIDATION_FAILED_MESSAGE("Message must reference either a room or a channel, but not both"),
    BUSINESS_PERMISSION_OVERRIDE_TARGET_REQUIRED_MESSAGE("Either role or user must be provided for permission override"),
    BUSINESS_USER_NOT_MEMBER_PRIVATE_ROOM_MESSAGE("User is not a member of the private room"),
    BUSINESS_USER_NOT_MEMBER_GROUP_ROOM_MESSAGE("User is not a member of the group room"),
    BUSINESS_ONLY_SENDER_CAN_EDIT_MESSAGE("Only the sender can edit the message");

    private final String message;

    BusinessRuleMessage(String message) {
        this.message = message;
    }
}


