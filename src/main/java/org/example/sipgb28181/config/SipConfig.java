package org.example.sipgb28181.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sip", ignoreInvalidFields = true)
@Order(0)
public class SipConfig {

	private String ip;

	private String showIp;

	private String sipIp;

	private Integer port;

	private String domain;

	private String id;

	private String password;
	
	private Integer ptzSpeed = 50;

	private Integer registerTimeInterval = 120;

	private boolean alarm;

	private boolean alarmNotify = false;

	public String getSipIp() {
		return sipIp;
	}

	public void setSipIp(String sipIp) {
		this.sipIp = sipIp;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPtzSpeed(Integer ptzSpeed) {
		this.ptzSpeed = ptzSpeed;
	}


	public void setRegisterTimeInterval(Integer registerTimeInterval) {
		this.registerTimeInterval = registerTimeInterval;
	}

	public String getIp() {
		return ip;
	}


	public Integer getPort() {
		return port;
	}


	public String getDomain() {
		return domain;
	}


	public String getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}


	public Integer getPtzSpeed() {
		return ptzSpeed;
	}

	public Integer getRegisterTimeInterval() {
		return registerTimeInterval;
	}

	public boolean isAlarm() {
		return alarm;
	}

	public void setAlarm(boolean alarm) {
		this.alarm = alarm;
	}

	public String getShowIp() {
		return showIp;
	}

	public void setShowIp(String showIp) {
		this.showIp = showIp;
	}

	public boolean isAlarmNotify() {
		return alarmNotify;
	}

	public void setAlarmNotify(boolean alarmNotify) {
		this.alarmNotify = alarmNotify;
	}
}
