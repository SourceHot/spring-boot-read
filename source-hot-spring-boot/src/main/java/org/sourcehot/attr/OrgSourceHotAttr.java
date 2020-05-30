package org.sourcehot.attr;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author huifer
 */
@ConfigurationProperties(prefix = "org.sourcehout")
public class OrgSourceHotAttr {

	private String describe;

	private String initiator;

	public OrgSourceHotAttr() {
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getInitiator() {
		return initiator;
	}

	public void setInitiator(String initiator) {
		this.initiator = initiator;
	}

}
