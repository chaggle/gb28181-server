package org.example.sipgb28181.service;

import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

public interface SipEventService {
    /**
     * 接收请求，注册事件
     * @param requestEvent requestEvent
     */
    void requestRegister(RequestEvent requestEvent) throws ParseException, NoSuchAlgorithmException, PeerUnavailableException;

    /**
     * 接收请求，消息事件
     * @param requestEvent requestEvent
     */
    void requestMessage(RequestEvent requestEvent) throws ParseException;

    /**
     * 响应invite请求
     * @param responseEvent
     */
    void responseInvite(ResponseEvent responseEvent);

    /**
     * 接收请求，bye事件
     * @param requestEvent requestEvent
     */
    void requestBye(RequestEvent requestEvent);
}
