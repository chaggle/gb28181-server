package org.example.sipgb28181.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.sipgb28181.common.Cache;
import org.example.sipgb28181.sip.SIPSender;
import org.example.sipgb28181.config.SipConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipFactory;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.FromHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Response;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Map;

@Slf4j
@Service
public class SipEventServiceImpl implements SipEventService {


    @Resource
    private SipConfig sipConfig;

    @Resource
    private SIPSender sipSender;

    @Resource
    private Cache cache;

    private SipFactory sipFactory;

    private MessageFactory messageFactory;

    public SipEventServiceImpl() throws PeerUnavailableException {
        this.sipFactory = SipFactory.getInstance();
        this.messageFactory = sipFactory.createMessageFactory();;

    }

    @Override
    public void requestRegister(RequestEvent requestEvent) throws ParseException, NoSuchAlgorithmException, PeerUnavailableException {
        RequestEventExt evtExt = (RequestEventExt) requestEvent;
        String requestAddress = evtExt.getRemoteIpAddress() + ":" + evtExt.getRemotePort();
        log.info("[注册请求] 开始处理: {}", requestAddress);

        SIPRequest sipRequest = (SIPRequest) requestEvent.getRequest();
        Response response = null;
        //密码是否正确
        boolean passwordCorrect = false;
        // 注册标志
        FromHeader fromHeader = (FromHeader) sipRequest.getHeader(FromHeader.NAME);
        AddressImpl address = (AddressImpl) fromHeader.getAddress();
        SipUri uri = (SipUri) address.getURI();
        //设备ID(保留)
        String deviceId = uri.getUser();
        //是否携带认证信息
        AuthorizationHeader authHead = (AuthorizationHeader) sipRequest.getHeader("Proxy-Authorization");
        String password = sipConfig.getPassword();
        if (authHead == null) {
            log.info("[注册请求] 摄像头未携带认证信息");
            log.info("[注册请求] 回复401: {}", requestAddress);
            response = messageFactory.createResponse(Response.UNAUTHORIZED, sipRequest);
            new DigestServerAuthenticationHelper().generateChallenge(sipFactory.createHeaderFactory(), response, sipConfig.getDomain());
            sipSender.send(sipRequest.getLocalAddress().getHostAddress(), response);
            return;
        }
        passwordCorrect = new DigestServerAuthenticationHelper().doAuthenticatePlainTextPassword(sipRequest, password);
        //密码验证失败
        if (!passwordCorrect) {
            // 注册失败
            log.info("[注册请求] 携带认证信息,但是密码验证错误");
            response = messageFactory.createResponse(Response.FORBIDDEN, sipRequest);
            response.setReasonPhrase("wrong password");
            log.info("[注册请求] 密码/SIP服务器ID错误, 回复403: {}", requestAddress);
            sipSender.send(sipRequest.getLocalAddress().getHostAddress(), response);
            return;
        }
        // 携带授权头并且密码正确
        response = messageFactory.createResponse(Response.OK, sipRequest);
        sipSender.send(sipRequest.getLocalAddress().getHostAddress(), response);

        cache.getCacheObj().put(deviceId, requestAddress);
    }

    @Override
    public void requestMessage(RequestEvent requestEvent) throws ParseException {
        RequestEventExt evtExt = (RequestEventExt) requestEvent;
        Map<String, Object> message = XmlUtil.xmlToMap(StrUtil.str(requestEvent.getRequest().getRawContent(), Charset.defaultCharset()));
        String cmdType = message.get("CmdType").toString();
        switch (cmdType) {
            case "Keepalive":
                log.info("[心跳] 收到心跳请求");
                sipSender.send(evtExt.getRemoteIpAddress(), messageFactory.createResponse(Response.OK, requestEvent.getRequest()));
                break;
            case "Catalog":
                log.info("[目录] 收到目录请求, deviceList:{}", message.get("DeviceList"));
                sipSender.send(evtExt.getRemoteIpAddress(), messageFactory.createResponse(Response.OK, requestEvent.getRequest()));
                break;
        }
    }

    @Override
    public void responseInvite(ResponseEvent responseEvent) {

    }

    @Override
    public void requestBye(RequestEvent requestEvent) {

    }
}
