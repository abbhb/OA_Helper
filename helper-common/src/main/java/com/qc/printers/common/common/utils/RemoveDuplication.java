package com.qc.printers.common.common.utils;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * 去重
 */
public class RemoveDuplication {
    /**
     * 使用HashSet实现List去重(无序)
     *
     * @param list
     */
    public static List removeDuplicationByHashSet(List list) {
        HashSet set = new HashSet(list);
        //把List集合所有元素清空
        list.clear();
        //把HashSet对象添加至List集合
        list.addAll(set);
        return list;
    }

    /**
     * 使用List集合contains方法循环遍历(有序)
     *
     * @param list
     */
    public static List removeDuplicationByContains(List<Integer> list) {
        List<Integer> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            boolean isContains = newList.contains(list.get(i));
            if (!isContains) {
                newList.add(list.get(i));
            }
        }
        list.clear();
        list.addAll(newList);
        return list;
    }

    /**
     * 使用两个for循环实现List去重(有序)
     *
     * @param list
     */
    public static List removeDuplicationBy2For(List<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).equals(list.get(j))) {
                    list.remove(j);
                }
            }
        }
        return list;
    }

    /**
     * 使用TreeSet实现List去重(有序)
     *
     * @param list
     */
    public static List removeDuplicationByTreeSet(List<Integer> list) {
        TreeSet set = new TreeSet(list);
        //把List集合所有元素清空
        list.clear();
        //把HashSet对象添加至List集合
        list.addAll(set);
        return list;
    }
}
