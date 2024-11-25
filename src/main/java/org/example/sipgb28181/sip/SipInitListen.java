package org.example.sipgb28181.sip;

import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.sipgb28181.common.DefaultProperties;
import org.example.sipgb28181.config.SipConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.sip.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SipInitListen implements CommandLineRunner {

    @Resource
    private SipConfig sipConfig;

    /**
     * sip通信，消息监听处理
     */
    @Resource
    private SipProcessListener sipProcessListener;

    private SipFactory sipFactory;

    /**
     * tcp-sip提供
     */
    private final Map<String, SipProviderImpl> tcpSipProviderMap = new ConcurrentHashMap<>();

    /**
     * udp-sip提供
     */
    private final Map<String, SipProviderImpl> udpSipProviderMap = new ConcurrentHashMap<>();


    @Override
    public void run(String... args) {
        List<String> monitorIps = new ArrayList<>();
        // 使用逗号分割多个ip
        String separator = ",";
        if (sipConfig.getIp().indexOf(separator) > 0) {
            String[] split = sipConfig.getIp().split(separator);
            monitorIps.addAll(Arrays.asList(split));
        }else {
            monitorIps.add(sipConfig.getIp());
        }

        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        if (monitorIps.size() > 0) {
            for (String monitorIp : monitorIps) {
                addListeningPoint(monitorIp, sipConfig.getPort());
            }
            if (udpSipProviderMap.size() + tcpSipProviderMap.size() == 0) {
                System.exit(1);
            }
        }
    }

    /**
     * 添加 监听ip
     * @param monitorIp 监听ip
     * @param port      端口
     */
    private void addListeningPoint(String monitorIp, int port){
        //sip协议栈
        SipStackImpl sipStack;
        try {
            sipStack = (SipStackImpl)sipFactory.createSipStack(DefaultProperties.getProperties(monitorIp, Boolean.FALSE));
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
            log.error("[Sip Server] SIP服务启动失败， 监听地址{}失败,请检查ip是否正确", monitorIp);
            return;
        }
        try {
            //创建 TCP传输监听
            ListeningPoint tcpListeningPoint = sipStack.createListeningPoint(monitorIp, port, "TCP");
            //tcp 消息处理实现
            SipProviderImpl tcpSipProvider = (SipProviderImpl)sipStack.createSipProvider(tcpListeningPoint);
            tcpSipProvider.setDialogErrorsAutomaticallyHandled();
            tcpSipProvider.addSipListener(sipProcessListener);
            tcpSipProviderMap.put(monitorIp, tcpSipProvider);
            log.info("[Sip Server] tcp://{}:{} 启动成功", monitorIp, port);
        } catch (TransportNotSupportedException
                 | TooManyListenersException
                 | ObjectInUseException
                 | InvalidArgumentException e) {
            log.error("[Sip Server] tcp://{}:{} SIP服务启动失败,请检查端口是否被占用或者ip是否正确"
                    , monitorIp, port);
        }
        try {
            //创建 UDP传输监听
            ListeningPoint udpListeningPoint = sipStack.createListeningPoint(monitorIp, port, "UDP");
            //udp 消息处理实现
            SipProviderImpl udpSipProvider = (SipProviderImpl)sipStack.createSipProvider(udpListeningPoint);
            udpSipProvider.addSipListener(sipProcessListener);
            udpSipProviderMap.put(monitorIp, udpSipProvider);

            log.info("[Sip Server] udp://{}:{} 启动成功", monitorIp, port);
        } catch (TransportNotSupportedException
                 | TooManyListenersException
                 | ObjectInUseException
                 | InvalidArgumentException e) {
            log.error("[Sip Server] udp://{}:{} SIP服务启动失败,请检查端口是否被占用或者ip是否正确"
                    , monitorIp, port);
        }
    }

    public SipFactory getSipFactory() {
        return sipFactory;
    }

    public SipProviderImpl getUdpSipProvider(String ip) {
        if (ObjectUtils.isEmpty(ip)) {
            return null;
        }
        return udpSipProviderMap.get(ip);
    }

    public SipProviderImpl getUdpSipProvider() {
        if (udpSipProviderMap.size() != 1) {
            return null;
        }
        return udpSipProviderMap.values().stream().findFirst().get();
    }

    public SipProviderImpl getTcpSipProvider() {
        if (tcpSipProviderMap.size() != 1) {
            return null;
        }
        return tcpSipProviderMap.values().stream().findFirst().get();
    }

    public SipProviderImpl getTcpSipProvider(String ip) {
        if (ObjectUtils.isEmpty(ip)) {
            return null;
        }
        return tcpSipProviderMap.get(ip);
    }

    public String getLocalIp(String deviceLocalIp) {
        if (!ObjectUtils.isEmpty(deviceLocalIp)) {
            return deviceLocalIp;
        }
        return getUdpSipProvider().getListeningPoint().getIPAddress();
    }
}
