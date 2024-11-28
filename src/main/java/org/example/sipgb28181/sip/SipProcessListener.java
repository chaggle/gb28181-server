package org.example.sipgb28181.sip;

import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.sipgb28181.service.SipEventService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sip.*;
import java.nio.charset.Charset;

@Slf4j
@Component
public class SipProcessListener implements SipListener {
    @Resource
    private SipEventService sipEventService;



    @SneakyThrows
    @Override
    public void processRequest(RequestEvent requestEvent) {
        log.info("收到摄像机服务请求" + requestEvent.getRequest().getMethod());
        String method = requestEvent.getRequest().getMethod();
        switch (method){
            case "REGISTER":
                sipEventService.requestRegister(requestEvent);
                break;
            case "MESSAGE":
                log.info("收到摄像机服务请求" + StrUtil.str(requestEvent.getRequest().getRawContent(), Charset.defaultCharset()));
                sipEventService.requestMessage(requestEvent);
                break;
            case "BYE":
                break;
            case "OPTIONS":
                break;
            case "NOTIFY":
                break;
            case "SUBSCRIBE":
                break;
            case "INVITE":
                log.info("发起邀请请求" + StrUtil.str(requestEvent.getRequest().getRawContent(), Charset.defaultCharset()));
                sipEventService.requestMessage(requestEvent);
                break;
            default:
                log.info("不支持的请求类型");
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
    }

    @Override
    public void processIOException(IOExceptionEvent ioExceptionEvent) {
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }
}
