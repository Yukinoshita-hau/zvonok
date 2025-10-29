package com.zvonok.exception_handler.enumeration;

import lombok.Getter;

@Getter
public enum HttpResponseMessage {
    // Room
    HTTP_ROOM_NOT_FOUND_RESPONSE_MESSAGE("Room was not found"),
    HTTP_ROOM_SIZE_MAX_TEN_MEMBERS_RESPONSE_MESSAGE("Maximum of 10 members in group chat"),

    // User
    HTTP_USER_NOT_FOUND_RESPONSE_MESSAGE("User was not found"),
    HTTP_USER_NOT_MEMBER_ROOM_RESPONSE_MESSAGE("User is not a member of the room"),
    HTTP_USER_WITH_THIS_USERNAME_ALREADY_EXIST_RESPONSE_MESSAGE("User with this username already exist"),
    HTTP_USER_WITH_THIS_EMAIL_ALREADY_EXIST_RESPONSE_MESSAGE("User with this email already exist"),
    HTTP_OWNER_CAN_NOT_LEAVE_SERVER_RESPONSE_MESSAGE("The owner cannot leave the server. First pass the ownership."),
    HTTP_YOU_NOT_MEMBER_THIS_SERVER_RESPONSE_MESSAGE("You are not a member of this server"),
    // Channel
    HTTP_CHANNEL_NOT_FOUND_RESPONSE_MESSAGE("Channel was not found"),

    // Server
    HTTP_SERVER_NOT_FOUND_RESPONSE_MESSAGE("Server was not found"),
    HTTP_SERVER_NOT_ACTIVE_RESPONSE_MESSAGE("Server is not active now"),

    // ServerRole
    HTTP_SERVER_ROLE_NOT_FOUND_RESPONSE_MESSAGE("ServerRole was not found"),

    // ServerMemberRole
    HTTP_SERVER_MEMBER_ROLE_NOT_FOUND_RESPONSE_MESSAGE("ServerMemberRole was not found"),

    // ServerMember
    HTTP_SERVER_MEMBER_NOT_FOUND_RESPONSE_MESSAGE("ServerMember was not found"),
    HTTP_SERVER_MAXIMUM_NUMBER_OF_SERVER_MEMBERS_RESPONSE_MESSAGE("Maximum number of server members reached"),

    // ChannelFolder
    HTTP_CHANNEL_FOLDER_NOT_FOUND_RESPONSE_MESSAGE("Channel folder was not found"),

    // Permissions
    HTTP_INSUFFICIENT_PERMISSIONS_RESPONSE_MESSAGE("Not enough rights to manage the server"),

    // Authorization and validation data
    HTTP_INVALID_JWT_RESPONSE_MESSAGE("JWT token not valid or missing!"),
    HTTP_INVALID_USER_OR_PASSWORD_RESPONSE_MESSAGE("Invalid user or password"),
    HTTP_REDEFINITION_RESPONSE_MESSAGE("Override can be either for the role or for the user") ;

    private final String message;

    HttpResponseMessage(String message) {
        this.message = message;
    }
}
