package org.example.sipgb28181.controller;

import cn.hutool.core.util.StrUtil;
import gov.nist.javax.sip.SipProviderImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.sipgb28181.sip.SIPSender;
import org.example.sipgb28181.sip.SipInitListen;
import org.example.sipgb28181.common.Cache;
import org.example.sipgb28181.config.SipConfig;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sip")
@Slf4j
public class SipController {

    @Resource
    private SIPSender sender;

    @Resource
    private Cache cache;

    @Resource
    private SipInitListen sipInitListen;

    @Resource
    private SipConfig sipConfig;

    @RequestMapping("/subscribe")
    public void subscribe(@RequestParam int id) throws PeerUnavailableException, URISyntaxException, InvalidArgumentException, ParseException {

        String deviceId = "34020000001320000003";

        StringBuffer catalogXml = new StringBuffer(200);
        catalogXml.append("<?xml version=\"1.0\" ?>\r\n");
        catalogXml.append("<Query>\r\n");
        catalogXml.append("<CmdType>Catalog</CmdType>\r\n");
        catalogXml.append("<SN>" + (int) ((Math.random() * 9 + 1) * 100000) + "</SN>\r\n");
        catalogXml.append("<DeviceID>" + deviceId + "</DeviceID>\r\n");
        catalogXml.append("</Query>\r\n");



        String ip = (String) cache.getCacheObj().get(deviceId);
        MessageFactory messageFactory =  SipFactory.getInstance().createMessageFactory();
        SipURI requestURI = SipFactory.getInstance().createAddressFactory().createSipURI(deviceId, ip);
        CSeqHeader cSeqHeader = SipFactory.getInstance().createHeaderFactory().createCSeqHeader(id, Request.MESSAGE);
        SipURI fromSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(sipConfig.getId(), sipConfig.getSipIp()+":"+sipConfig.getPort());
        Address fromAddress = SipFactory.getInstance().createAddressFactory().createAddress(fromSipURI);
        FromHeader fromHeader = SipFactory.getInstance().createHeaderFactory().createFromHeader(fromAddress, "38937493");

        SipURI toSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(deviceId, ip);
        Address toAddress = SipFactory.getInstance().createAddressFactory().createAddress(toSipURI);
        ToHeader toHeader = SipFactory.getInstance().createHeaderFactory().createToHeader(toAddress, null);
        List<ViaHeader> viaHeaders = new ArrayList<>();
        viaHeaders.add(SipFactory.getInstance().createHeaderFactory().createViaHeader(sipConfig.getIp(), sipConfig.getPort(), "UDP", null));


        Request request = messageFactory.createRequest(
                requestURI,
                Request.MESSAGE,
                getNewCallIdHeader(ip, "UDP"),
                cSeqHeader,
                fromHeader,
                toHeader,
                viaHeaders,
                SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70),
                SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("Application", "MANSCDP+xml"),
                StrUtil.bytes(catalogXml.toString())
        );

        sender.send(sipConfig.getIp(), request);
    }

    public CallIdHeader getNewCallIdHeader(String ip, String transport){
        if (ObjectUtils.isEmpty(transport)) {
            return sipInitListen.getUdpSipProvider().getNewCallId();
        }
        SipProviderImpl sipProvider;
        if (ObjectUtils.isEmpty(ip)) {
            sipProvider = transport.equalsIgnoreCase("TCP") ? sipInitListen.getTcpSipProvider()
                    : sipInitListen.getUdpSipProvider();
        }else {
            sipProvider = transport.equalsIgnoreCase("TCP") ? sipInitListen.getTcpSipProvider(ip)
                    : sipInitListen.getUdpSipProvider(ip);
        }

        if (sipProvider == null) {
            sipProvider = sipInitListen.getUdpSipProvider();
        }

        if (sipProvider != null) {
            return sipProvider.getNewCallId();
        }else {
            log.warn("[新建CallIdHeader失败]， ip={}, transport={}", ip, transport);
            return null;
        }
    }
}
