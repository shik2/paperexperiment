package com.shik2.utils;

import com.shik2.ga.MEC;

import java.util.List;

/**
 * @author shik2
 * @date 2021/03/12
 * <p>
 * Description:
 **/
public class ListUtil {


    /**
     * 资源阈值theta减小
     *
     * @param beforeMECList
     * @param afterMECList
     * @param elementNum
     */
    public static void moveHeadToTail(List<MEC> beforeMECList, List<MEC> afterMECList, int elementNum) {
        while (elementNum-- > 0) {
            beforeMECList.add(afterMECList.remove(0));
        }
    }


    /**
     * 资源阈值theta增大
     *
     * @param beforeMECList
     * @param afterMECList
     * @param elementNum
     */
    public static void moveTailToHead(List<MEC> beforeMECList, List<MEC> afterMECList, int elementNum) {
        while (elementNum-- > 0) {
            afterMECList.add(beforeMECList.remove(beforeMECList.size()-1));
        }
    }

}
