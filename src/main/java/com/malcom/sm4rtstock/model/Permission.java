package com.malcom.sm4rtstock.model;

import java.util.EnumSet;
import java.util.Set;

public enum Permission {
    PRODUCT_VIEW,
    PRODUCT_CREATE,
    PRODUCT_STOCK_EDIT,
    PRODUCT_DELETE,
    CATEGORY_VIEW,
    CATEGORY_MANAGE,
    HISTORY_VIEW,
    DASHBOARD_VIEW,
    DATA_EXPORT;

    public static Set<Permission> defaultsForRole(Role role) {
        if (role == Role.ADMIN) {
            return EnumSet.allOf(Permission.class);
        }
        return EnumSet.of(
                PRODUCT_VIEW,
                CATEGORY_VIEW,
                HISTORY_VIEW,
                DASHBOARD_VIEW
        );
    }
}
