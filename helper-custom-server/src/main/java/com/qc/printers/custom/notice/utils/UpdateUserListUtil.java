package com.qc.printers.custom.notice.utils;

public class UpdateUserListUtil {
    public static String getUpdateUserList(String oldList, Long newUserId) {
        if (oldList == null || oldList.equals("")) {
            return newUserId.toString();
        } else if (oldList.contains(",")) {
            return oldList + "," + newUserId.toString();
        } else {
            return newUserId.toString();
        }
    }
}
