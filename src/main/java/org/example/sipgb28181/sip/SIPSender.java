package org.example.sipgb28181.sip;

import gov.nist.javax.sip.SipProviderImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.sip.SipException;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Lazy
public class SIPSender {

    @Autowired
    private SipInitListen sipInitListen;

    /**
     * 发送消息
     * @param ip      目标ip
     * @param message 消息体
     */
    @SneakyThrows
    public void send(String ip, Message message) {
        ViaHeader viaHeader = (ViaHeader)message.getHeader(ViaHeader.NAME);
        String transport = "UDP";
        if (viaHeader == null) {
            log.warn("[消息头缺失]： ViaHeader， 使用默认的UDP方式处理数据");
        }else {
            transport = viaHeader.getTransport();
        }
        if (message.getHeader(UserAgentHeader.NAME) == null) {
            List<String> agentParam = new ArrayList<>();
            agentParam.add("video ");
            message.addHeader(sipInitListen.getSipFactory().createHeaderFactory().createUserAgentHeader(agentParam));
        }
        switch (transport){
            case "TCP":
                sendTCP(ip,message);
                return;
            case "UDP":
                sendUDP(ip,message);
                return;
            default:
                sendTCP(ip,message);
        }
    }

    private boolean sendUDP(String ip, Message message) throws SipException {
        SipProviderImpl sipProvider = sipInitListen.getUdpSipProvider(ip);
        if (sipProvider == null) {
            log.error("[发送信息失败] 未找到udp://{}的监听信息", ip);
            return true;
        }
        if (message instanceof Request) {
            sipProvider.sendRequest((Request) message);
        }else if (message instanceof Response) {
            sipProvider.sendResponse((Response) message);
        }
        return false;
    }

    private boolean sendTCP(String ip, Message message) throws SipException {
        SipProviderImpl tcpSipProvider = sipInitListen.getTcpSipProvider(ip);
        if (tcpSipProvider == null) {
            log.error("[发送信息失败] 未找到tcp://{}的监听信息", ip);
            return true;
        }
        if (message instanceof Request) {
            tcpSipProvider.sendRequest((Request) message);
        }else if (message instanceof Response) {
            tcpSipProvider.sendResponse((Response) message);
        }
        return false;
    }
}
