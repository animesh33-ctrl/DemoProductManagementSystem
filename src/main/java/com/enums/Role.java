package com.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER(Set.of(Permission.READ)),
    ADMIN(Set.of(Permission.READ, Permission.WRITE, Permission.DELETE,Permission.UPDATE));

    private final Set<Permission> permissions;
}
