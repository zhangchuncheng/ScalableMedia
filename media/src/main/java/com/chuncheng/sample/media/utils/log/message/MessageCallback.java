package com.chuncheng.sample.media.utils.log.message;

/**
 * Description:MessageCallback
 *
 * @author: zhangchuncheng
 * @date: 2017/12/29
 */

public interface MessageCallback<T> {

    T getParams();

    void setParams(T t);
}
